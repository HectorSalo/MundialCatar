import * as admin from 'firebase-admin';
import { onDocumentUpdated } from 'firebase-functions/v2/firestore';
import { logger } from 'firebase-functions';

admin.initializeApp();

const db = admin.firestore();

// OJO: estos nombres deben coincidir con tus colecciones reales.
// "games" es la colección donde guardas los partidos reales.
// "games_users" debe coincidir con tu R.string.path_games_users.
// "users" debe coincidir con tu R.string.path_users.
const GAMES_COLLECTION = 'games';
const PREDICTIONS_COLLECTION = 'gamesUsers';
const USERS_COLLECTION = 'users';

type ResultSign = 'HOME_WIN' | 'DRAW' | 'AWAY_WIN';

function toResultSign(homeGoals: number, awayGoals: number): ResultSign {
  if (homeGoals > awayGoals) return 'HOME_WIN';
  if (homeGoals < awayGoals) return 'AWAY_WIN';
  return 'DRAW';
}

/**
 * Algoritmo de puntos (igual al de Kotlin):
 *
 * - 5 puntos si acierta el marcador exacto.
 * - 3 puntos si acierta solo el signo (local/empate/visita).
 * - -2 si invierte el marcador exacto (2-1 vs 1-2).
 * - -1 para cualquier otro error.
 */
function computePoints(
  actualHome: number,
  actualAway: number,
  predictedHome: number,
  predictedAway: number
): number {
  const actualSign = toResultSign(actualHome, actualAway);
  const predictedSign = toResultSign(predictedHome, predictedAway);

  // 1) Marcador exacto
  if (predictedHome === actualHome && predictedAway === actualAway) {
    return 5;
  }

  // 2) Signo correcto (gana local / empate / gana visita)
  if (predictedSign === actualSign) {
    return 3;
  }

  // 3) Signo incorrecto → penalización
  //    3.1) Resultado exactamente invertido (2-1 vs 1-2) → -2
  if (predictedHome === actualAway && predictedAway === actualHome) {
    return -2;
  }

  //    3.2) Cualquier otro error → -1
  return -1;
}

/**
 * Trigger: cuando se actualiza un partido en "games/{gameId}".
 * Recalcula los puntos de todas las predicciones de ese partido
 * y actualiza el total en la colección "users".
 */
export const onGameResultUpdated = onDocumentUpdated(
{
	document: `${GAMES_COLLECTION}/{gameId}`,
	region: 'us-central1',
},  
  async (event) => {
    const beforeSnap = event.data?.before;
    const afterSnap = event.data?.after;

    if (!afterSnap) {
      return;
    }

    const before = beforeSnap?.data() ?? {};
    const after = afterSnap.data() ?? {};

    const actualHome = after.homeGoals;
    const actualAway = after.awayGoals;

    // Si no hay marcador válido, no hacemos nada.
    if (typeof actualHome !== 'number' || typeof actualAway !== 'number') {
      logger.info('Game without valid score, skipping.');
      return;
    }

    const statusBefore = before.status;
    const statusAfter = after.status;

    const homeBefore = before.homeGoals;
    const awayBefore = before.awayGoals;

    // Detectar si:
    // - El partido acaba de pasar a FINISHED, o
    // - Cambiaron los goles (corrección de resultado)
    const finishedNow =
      statusAfter === 'FINISHED' && statusBefore !== 'FINISHED';
    const scoreChanged =
      homeBefore !== actualHome || awayBefore !== actualAway;

    if (!finishedNow && !scoreChanged) {
      logger.info('No finished status nor score change, skipping.');
      return;
    }

    const gameId = event.params.gameId as string;
    const matchNumber = after.matchNumber;
    //const tournamentId = after.tournamentId;

    if (typeof matchNumber !== 'number') {
      logger.warn(`Game ${gameId} without matchNumber, skipping.`);
      return;
    }

    logger.info(
      `Recomputing points for gameId=${gameId}, matchNumber=${matchNumber}`
    );

    // 1) Buscar todas las predicciones de este partido.
    // En tu app estás guardando el número del partido en el campo "number".
    let predictionsSnap = await db
      .collection(PREDICTIONS_COLLECTION)
      .where('number', '==', matchNumber)
      // .where('tournamentId', '==', tournamentId) // opcional si quieres filtrar por torneo
      .get();

    if (predictionsSnap.empty) {
      logger.info('No predictions for this game, nothing to do.');
      return;
    }

    const batch = db.batch();

    predictionsSnap.forEach((docSnap) => {
      const data = docSnap.data();

      const userId: string = (data.idUser || data.userId) as string;
      const predictedHome: number = (data.predictedHomeGoals ?? data.goals1) as number;
      const predictedAway: number = (data.predictedAwayGoals ?? data.goals2) as number;
      const oldPoints: number = (data.points ?? 0) as number;

      if (
        !userId ||
        typeof predictedHome !== 'number' ||
        typeof predictedAway !== 'number'
      ) {
        logger.warn(`Invalid prediction doc ${docSnap.id}, skipping.`);
        return;
      }

      const newPoints = computePoints(
        actualHome,
        actualAway,
        predictedHome,
        predictedAway
      );
      const delta = newPoints - oldPoints;

      // 2) Actualizar puntos de la predicción
      batch.update(docSnap.ref, {
        points: newPoints,
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      // 3) Actualizar puntos del usuario (incremental)
      const userRef = db.collection(USERS_COLLECTION).doc(userId);
      batch.update(userRef, {
        points: admin.firestore.FieldValue.increment(delta),
      });
    });

    await batch.commit();
    logger.info('Points recomputed successfully for all predictions.');
  }
);
