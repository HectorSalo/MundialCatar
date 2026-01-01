import * as admin from 'firebase-admin';
import { onDocumentUpdated } from 'firebase-functions/v2/firestore';
import { logger } from 'firebase-functions';
import { onCall, HttpsError } from 'firebase-functions/v2/https';

admin.initializeApp();

const db = admin.firestore();

// OJO: estos nombres deben coincidir con tus colecciones reales.
const GAMES_COLLECTION = 'games';
const PREDICTIONS_COLLECTION = 'gamesUsers';
const USERS_COLLECTION = 'users';
const TEAMS_COLLECTION = 'teams'; // ajusta si tu colección real se llama distinto
const STANDINGS_COLLECTION = 'standings'; // nueva colección donde guardaremos las tablas

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

// ------------------------------------------------------
// Estructuras para llaves de eliminación (KO)
// ------------------------------------------------------

export type MatchStage =
  | 'GROUP'
  | 'ROUND_OF_32'
  | 'ROUND_OF_16'
  | 'QUARTER_FINAL'
  | 'SEMI_FINAL'
  | 'THIRD_PLACE'
  | 'FINAL';

export type SeedDescriptor =
  | { type: 'GROUP_POSITION'; group: string; position: number } // ej: 1° del grupo A
  | { type: 'BEST_THIRD'; rank: number } // ej: 3er mejor tercero global
  | { type: 'WINNER_OF_MATCH'; stage: MatchStage; matchNumber: number }; // ej: ganador del partido 73 de R32

export interface KnockoutSlot {
  stage: MatchStage; // ROUND_OF_32, ROUND_OF_16, etc.
  matchNumber: number; // 73, 74, 75...
  homeSource: SeedDescriptor;
  awaySource: SeedDescriptor;
}

// ------------------------------------------------------
// Cálculo de standings de grupos + mejores terceros
// (puro, sin lecturas de Firestore)
// ------------------------------------------------------

export interface TeamDoc {
  id: string; // teamId (ej: "ARG")
  name: string; // "Argentina"
  group: string; // "A", "B", "C", ...
  flagCode?: string; // opcional: código/URL de bandera
}

export interface GameDoc {
  tournamentId: string;
  homeTeamId: string;
  awayTeamId: string;
  stage: MatchStage;
  group?: string; // solo en fase de grupos
  homeGoals?: number | null;
  awayGoals?: number | null;
  status?: string; // "SCHEDULED", "FINISHED", etc.
}

export interface GroupStanding {
  teamId: string;
  teamName: string;
  flagUrl: string;
  group: string;

  played: number;
  wins: number;
  draws: number;
  losses: number;
  goalsFor: number;
  goalsAgainst: number;
  goalDiff: number;
  points: number;

  position: number; // 1, 2, 3...
}

// Estructura interna mutable para sumar estadísticas
interface MutableTeamStats {
  teamId: string;
  teamName: string;
  flagUrl: string;
  group: string;
  played: number;
  wins: number;
  draws: number;
  losses: number;
  goalsFor: number;
  goalsAgainst: number;
  points: number;
}

/**
 * Dado un listado de equipos y partidos:
 *  - Calcula la tabla de posiciones por grupo (standingsByGroup)
 *  - Calcula la lista global de terceros (bestThirdGlobal), ordenada
 *    por puntos, diferencia de goles y goles a favor (criterios básicos).
 *
 * NOTA: aquí aún no aplicamos todos los criterios FIFA (head-to-head, fair play),
 *       pero la estructura está preparada para extenderlo más adelante.
 */
export function computeStandingsAndBestThirds(
  teams: TeamDoc[],
  games: GameDoc[]
): {
  standingsByGroup: Record<string, GroupStanding[]>;
  bestThirdGlobal: GroupStanding[];
} {
  const statsByGroup: Record<string, Record<string, MutableTeamStats>> = {};
  const teamsById: Record<string, TeamDoc> = {};

  teams.forEach((t) => {
    teamsById[t.id] = t;
  });

  // 1) Inicializar stats para todos los equipos por grupo
  for (const team of teams) {
    const group = team.group;
    if (!statsByGroup[group]) {
      statsByGroup[group] = {};
    }
    statsByGroup[group][team.id] = {
      teamId: team.id,
      teamName: team.name,
      flagUrl: team.flagCode || '',
      group,
      played: 0,
      wins: 0,
      draws: 0,
      losses: 0,
      goalsFor: 0,
      goalsAgainst: 0,
      points: 0,
    };
  }

  // 2) Procesar solo partidos de fase de grupos con marcador válido
  for (const game of games) {
    if (game.stage !== 'GROUP') continue;
    if (!game.group) continue;

    const { homeGoals, awayGoals, status } = game;

    // Solo tomamos partidos terminados y con goles numéricos
    if (
      status !== 'FINISHED' ||
      typeof homeGoals !== 'number' ||
      typeof awayGoals !== 'number'
    ) {
      continue;
    }

    const group = game.group;
    if (!statsByGroup[group]) {
      statsByGroup[group] = {};
    }

    const homeTeamInfo = teamsById[game.homeTeamId];
    const awayTeamInfo = teamsById[game.awayTeamId];

    if (!statsByGroup[group][game.homeTeamId]) {
      statsByGroup[group][game.homeTeamId] = {
        teamId: game.homeTeamId,
        teamName: homeTeamInfo?.name || game.homeTeamId,
        flagUrl: homeTeamInfo?.flagCode || '',
        group,
        played: 0,
        wins: 0,
        draws: 0,
        losses: 0,
        goalsFor: 0,
        goalsAgainst: 0,
        points: 0,
      };
    }
    if (!statsByGroup[group][game.awayTeamId]) {
      statsByGroup[group][game.awayTeamId] = {
        teamId: game.awayTeamId,
        teamName: awayTeamInfo?.name || game.awayTeamId,
        flagUrl: awayTeamInfo?.flagCode || '',
        group,
        played: 0,
        wins: 0,
        draws: 0,
        losses: 0,
        goalsFor: 0,
        goalsAgainst: 0,
        points: 0,
      };
    }

    const homeStats = statsByGroup[group][game.homeTeamId];
    const awayStats = statsByGroup[group][game.awayTeamId];

    // Partidos jugados
    homeStats.played++;
    awayStats.played++;

    // Goles
    homeStats.goalsFor += homeGoals;
    homeStats.goalsAgainst += awayGoals;

    awayStats.goalsFor += awayGoals;
    awayStats.goalsAgainst += homeGoals;

    // Resultado
    if (homeGoals > awayGoals) {
      homeStats.wins++;
      homeStats.points += 3;
      awayStats.losses++;
    } else if (homeGoals < awayGoals) {
      awayStats.wins++;
      awayStats.points += 3;
      homeStats.losses++;
    } else {
      homeStats.draws++;
      awayStats.draws++;
      homeStats.points += 1;
      awayStats.points += 1;
    }
  }

  // 3) Convertir statsByGroup a standingsByGroup (ordenados)
  const standingsByGroup: Record<string, GroupStanding[]> = {};
  const comparator = (a: GroupStanding, b: GroupStanding): number => {
    // Puntos DESC
    if (a.points !== b.points) return b.points - a.points;
    // Diferencia de goles DESC
    if (a.goalDiff !== b.goalDiff) return b.goalDiff - a.goalDiff;
    // Goles a favor DESC
    if (a.goalsFor !== b.goalsFor) return b.goalsFor - a.goalsFor;
    // TODO: head-to-head, fair play, ranking FIFA, etc.
    return 0;
  };

  for (const group of Object.keys(statsByGroup)) {
    const statsMap = statsByGroup[group];
    const rows: GroupStanding[] = Object.values(statsMap).map((s) => ({
      teamId: s.teamId,
      teamName: s.teamName,
      flagUrl: s.flagUrl,
      group: s.group,
      played: s.played,
      wins: s.wins,
      draws: s.draws,
      losses: s.losses,
      goalsFor: s.goalsFor,
      goalsAgainst: s.goalsAgainst,
      goalDiff: s.goalsFor - s.goalsAgainst,
      points: s.points,
      position: 0,
    }));

    rows.sort(comparator);
    rows.forEach((row, idx) => {
      row.position = idx + 1;
    });

    standingsByGroup[group] = rows;
  }

  // 4) Construir lista de terceros y ordenarlos globalmente
  const thirdCandidates: GroupStanding[] = [];

  for (const group of Object.keys(standingsByGroup)) {
    const rows = standingsByGroup[group];
    const third = rows[2];
    if (third) thirdCandidates.push(third);
  }

  thirdCandidates.sort(comparator);

  // Top 8 terceros (formato 2026)
  const bestThirdGlobal = thirdCandidates.slice(0, 8);

  return { standingsByGroup, bestThirdGlobal };
}

// ------------------------------------------------------
// Plantilla Round of 32 (basada en formato 2026)
// ------------------------------------------------------

export const ROUND_OF_32_SLOTS: KnockoutSlot[] = [
  {
    stage: 'ROUND_OF_32',
    matchNumber: 73,
    homeSource: { type: 'GROUP_POSITION', group: 'A', position: 2 },
    awaySource: { type: 'GROUP_POSITION', group: 'B', position: 2 },
  },
  {
    stage: 'ROUND_OF_32',
    matchNumber: 74,
    homeSource: { type: 'GROUP_POSITION', group: 'E', position: 1 },
    awaySource: { type: 'BEST_THIRD', rank: 1 },
  },
  {
    stage: 'ROUND_OF_32',
    matchNumber: 75,
    homeSource: { type: 'GROUP_POSITION', group: 'F', position: 1 },
    awaySource: { type: 'GROUP_POSITION', group: 'C', position: 2 },
  },
  {
    stage: 'ROUND_OF_32',
    matchNumber: 76,
    homeSource: { type: 'GROUP_POSITION', group: 'C', position: 1 },
    awaySource: { type: 'GROUP_POSITION', group: 'F', position: 2 },
  },
  {
    stage: 'ROUND_OF_32',
    matchNumber: 77,
    homeSource: { type: 'GROUP_POSITION', group: 'I', position: 1 },
    awaySource: { type: 'BEST_THIRD', rank: 2 },
  },
  {
    stage: 'ROUND_OF_32',
    matchNumber: 78,
    homeSource: { type: 'GROUP_POSITION', group: 'E', position: 2 },
    awaySource: { type: 'GROUP_POSITION', group: 'I', position: 2 },
  },
  {
    stage: 'ROUND_OF_32',
    matchNumber: 79,
    homeSource: { type: 'GROUP_POSITION', group: 'A', position: 1 },
    awaySource: { type: 'BEST_THIRD', rank: 3 },
  },
  {
    stage: 'ROUND_OF_32',
    matchNumber: 80,
    homeSource: { type: 'GROUP_POSITION', group: 'L', position: 1 },
    awaySource: { type: 'BEST_THIRD', rank: 4 },
  },
  {
    stage: 'ROUND_OF_32',
    matchNumber: 81,
    homeSource: { type: 'GROUP_POSITION', group: 'D', position: 1 },
    awaySource: { type: 'BEST_THIRD', rank: 5 },
  },
  {
    stage: 'ROUND_OF_32',
    matchNumber: 82,
    homeSource: { type: 'GROUP_POSITION', group: 'G', position: 1 },
    awaySource: { type: 'BEST_THIRD', rank: 6 },
  },
  {
    stage: 'ROUND_OF_32',
    matchNumber: 83,
    homeSource: { type: 'GROUP_POSITION', group: 'K', position: 2 },
    awaySource: { type: 'GROUP_POSITION', group: 'L', position: 2 },
  },
  {
    stage: 'ROUND_OF_32',
    matchNumber: 84,
    homeSource: { type: 'GROUP_POSITION', group: 'H', position: 1 },
    awaySource: { type: 'GROUP_POSITION', group: 'J', position: 2 },
  },
  {
    stage: 'ROUND_OF_32',
    matchNumber: 85,
    homeSource: { type: 'GROUP_POSITION', group: 'B', position: 1 },
    awaySource: { type: 'BEST_THIRD', rank: 7 },
  },
  {
    stage: 'ROUND_OF_32',
    matchNumber: 86,
    homeSource: { type: 'GROUP_POSITION', group: 'J', position: 1 },
    awaySource: { type: 'GROUP_POSITION', group: 'H', position: 2 },
  },
  {
    stage: 'ROUND_OF_32',
    matchNumber: 87,
    homeSource: { type: 'GROUP_POSITION', group: 'K', position: 1 },
    awaySource: { type: 'BEST_THIRD', rank: 8 },
  },
  {
    stage: 'ROUND_OF_32',
    matchNumber: 88,
    homeSource: { type: 'GROUP_POSITION', group: 'D', position: 2 },
    awaySource: { type: 'GROUP_POSITION', group: 'G', position: 2 },
  },
];

// ------------------------------------------------------
// Callable: recalcular standings de un torneo
// ------------------------------------------------------

export const recomputeStandingsForTournament = onCall(
  { region: 'us-central1' },
  async (request) => {
    const tournamentId = request.data?.tournamentId as string | undefined;

    if (!tournamentId) {
      throw new HttpsError('invalid-argument', 'Debe enviar tournamentId en el payload.');
    }

    logger.info(`Recomputando standings para torneo ${tournamentId}`);

    const teamsSnap = await db
      .collection(TEAMS_COLLECTION)
      .where('tournamentId', '==', tournamentId)
      .get();

    if (teamsSnap.empty) {
      logger.warn(`No hay teams para tournamentId=${tournamentId}`);
    }

    const teams: TeamDoc[] = teamsSnap.docs.map((doc) => {
      const data = doc.data();
      return {
        id: doc.id,
        name: (data.name ?? data.teamName ?? doc.id) as string,
        group: (data.group ?? '') as string,
        flagCode: (data.flagCode ?? data.flag ?? '') as string,
      };
    });

    const gamesSnap = await db
      .collection(GAMES_COLLECTION)
      .where('tournamentId', '==', tournamentId)
      .get();

    const games: GameDoc[] = gamesSnap.docs.map((doc) => {
      const data = doc.data();
      return {
        tournamentId,
        homeTeamId: (data.homeTeamId ?? data.team1) as string,
        awayTeamId: (data.awayTeamId ?? data.team2) as string,
        stage: (data.stage ?? 'GROUP') as MatchStage,
        group: (data.group ?? data.round ?? undefined) as string | undefined,
        homeGoals: data.homeGoals as number | undefined,
        awayGoals: data.awayGoals as number | undefined,
        status: (data.status ?? 'SCHEDULED') as string,
      };
    });

    const { standingsByGroup, bestThirdGlobal } = computeStandingsAndBestThirds(teams, games);

    const batch = db.batch();

    for (const group of Object.keys(standingsByGroup)) {
      const standings = standingsByGroup[group];
      const docId = `${tournamentId}_${group}`;
      const ref = db.collection(STANDINGS_COLLECTION).doc(docId);

      batch.set(
        ref,
        {
          tournamentId,
          group,
          table: standings,
          updatedAt: admin.firestore.FieldValue.serverTimestamp(),
        },
        { merge: true }
      );
    }

    const thirdsRef = db.collection(STANDINGS_COLLECTION).doc(`${tournamentId}_bestThirds`);

    batch.set(
      thirdsRef,
      {
        tournamentId,
        bestThirds: bestThirdGlobal,
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      },
      { merge: true }
    );

    await batch.commit();

    logger.info(`Standings actualizados para torneo ${tournamentId} en ${STANDINGS_COLLECTION}.`);

    return {
      ok: true,
      groups: Object.keys(standingsByGroup),
      thirdCount: bestThirdGlobal.length,
    };
  }
);

// ------------------------------------------------------
// Notificación: juego terminado (por topic)
// ------------------------------------------------------

async function sendGameFinishedNotification(params: {
  gameId: string;
  tournamentId: string;
  matchNumber: number;
  homeTeamId: string;
  awayTeamId: string;
  homeGoals: number;
  awayGoals: number;
  wentToPenalties?: boolean;
  homePenalties?: number | null;
  awayPenalties?: number | null;
}) {
  const topic = params.tournamentId ? `results_${params.tournamentId}` : 'results_all';

  const title = 'Resultado final';
  let body = `${params.homeTeamId} ${params.homeGoals}-${params.awayGoals} ${params.awayTeamId}`;

  const wentToPenalties = !!params.wentToPenalties;
  if (
    wentToPenalties &&
    typeof params.homePenalties === 'number' &&
    typeof params.awayPenalties === 'number'
  ) {
    body += ` (Penales ${params.homePenalties}-${params.awayPenalties})`;
  }

  await admin.messaging().send({
    topic,
    notification: { title, body },
    data: {
      type: 'GAME_FINISHED',
      gameId: params.gameId,
      matchNumber: String(params.matchNumber),
      tournamentId: params.tournamentId,
    },
  });

  logger.info(`Push sent: topic=${topic}, gameId=${params.gameId}, match=${params.matchNumber}`);
}

/**
 * Trigger: cuando se actualiza un partido en "games/{gameId}".
 * 1) Si pasa a FINISHED, envía una notificación (una sola vez) por topic.
 * 2) Recalcula puntos de predicciones si hay predicciones para ese partido.
 */
export const onGameResultUpdated = onDocumentUpdated(
  {
    document: `${GAMES_COLLECTION}/{gameId}`,
    region: 'us-central1',
  },
  async (event) => {
    const beforeSnap = event.data?.before;
    const afterSnap = event.data?.after;

    if (!afterSnap) return;

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
    const finishedNow = statusAfter === 'FINISHED' && statusBefore !== 'FINISHED';
    const scoreChanged = homeBefore !== actualHome || awayBefore !== actualAway;

    if (!finishedNow && !scoreChanged) {
      logger.info('No finished status nor score change, skipping.');
      return;
    }

    const gameId = event.params.gameId as string;
    const matchNumber = after.matchNumber;

    if (typeof matchNumber !== 'number') {
      logger.warn(`Game ${gameId} without matchNumber, skipping.`);
      return;
    }

    // 1) NOTIFICAR SOLO CUANDO PASA A FINISHED (independiente de predicciones)
    if (finishedNow) {
      if (after.resultNotifiedAt) {
        logger.info(`Game ${gameId} already notified, skipping push.`);
      } else {
        const tournamentId = String(after.tournamentId ?? '');
        const homeTeamId = String(after.homeTeamId ?? '');
        const awayTeamId = String(after.awayTeamId ?? '');

        await sendGameFinishedNotification({
          gameId,
          tournamentId,
          matchNumber,
          homeTeamId,
          awayTeamId,
          homeGoals: actualHome,
          awayGoals: actualAway,
          wentToPenalties: !!after.wentToPenalties,
          homePenalties: after.homePenalties ?? null,
          awayPenalties: after.awayPenalties ?? null,
        });

        // Marcar como notificado (merge para no romper tu doc)
        await afterSnap.ref.set(
          { resultNotifiedAt: admin.firestore.FieldValue.serverTimestamp() },
          { merge: true }
        );
      }
    }

    // 2) RECÁLCULO DE PUNTOS:
    // Importante: recalcular puntos solo cuando el juego esté FINISHED
    if (statusAfter !== 'FINISHED') {
      logger.info(
        `Game ${gameId} is not FINISHED (status=${String(statusAfter)}), skipping points recompute.`
      );
      return;
    }

    logger.info(`Recomputing points for gameId=${gameId}, matchNumber=${matchNumber}`);

    const predictionsSnap = await db
      .collection(PREDICTIONS_COLLECTION)
      .where('matchNumber', '==', matchNumber)
      .get();

    if (predictionsSnap.empty) {
      logger.info('No predictions for this game, skipping points recompute.');
      return;
    }

    const batch = db.batch();

    predictionsSnap.forEach((docSnap) => {
      const data = docSnap.data();

      const userId: string = String(data.userId ?? '');
      const predictedHome: number = Number(data.predictedHomeGoals);
      const predictedAway: number = Number(data.predictedAwayGoals);
      const oldPoints: number = Number(data.points ?? 0);

      if (!userId || !Number.isFinite(predictedHome) || !Number.isFinite(predictedAway)) {
        logger.warn(`Invalid prediction doc ${docSnap.id}, skipping.`);
        return;
      }

      const newPoints = computePoints(actualHome, actualAway, predictedHome, predictedAway);
      const delta = newPoints - oldPoints;

      batch.update(docSnap.ref, {
        points: newPoints,
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      // Importante: update() falla si el doc no existe. Usamos set(..., merge:true) para upsert seguro.
      const userRef = db.collection(USERS_COLLECTION).doc(userId);
      batch.set(
        userRef,
        { points: admin.firestore.FieldValue.increment(delta) },
        { merge: true }
      );
    });

    await batch.commit();
    logger.info('Points recomputed successfully for all predictions.');
  }
);
