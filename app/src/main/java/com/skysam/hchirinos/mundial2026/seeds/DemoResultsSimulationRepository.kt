package com.skysam.hchirinos.mundial2026.seeds

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.skysam.hchirinos.mundial2026.BuildConfig
import com.skysam.hchirinos.mundial2026.common.Constants
import com.skysam.hchirinos.mundial2026.dataclass.MatchStatus
import com.skysam.hchirinos.mundial2026.seeds.DemoGameResultSeed
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Hector Chirinos in the home office on 10 abr. 2026
 */
@Singleton
class DemoResultsSimulationRepository @Inject constructor (
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val TAG = "DemoResultsSimulation"
    }

    suspend fun simulateResults(results: List<DemoGameResultSeed>) {
        val tournamentId = BuildConfig.DEMO_TOURNAMENT_ID

        if (BuildConfig.TOURNAMENT_ID != tournamentId) {
            Log.w(TAG, "Simulación ignorada: la build actual no es demo")
            return
        }

        results.forEach { result ->
            simulateSingleResult(result, tournamentId)
        }

        Log.i(TAG, "Simulación completada. Juegos procesados=${results.size}")
    }

    private suspend fun simulateSingleResult(
        result: DemoGameResultSeed,
        tournamentId: String
    ) {
        val docId = "${tournamentId}_match_${result.matchNumber}"
        val ref = firestore.collection(Constants.GAMES).document(docId)

        val snapshot = ref.get().await()
        if (!snapshot.exists()) {
            Log.w(TAG, "No existe juego demo para matchNumber=${result.matchNumber}, docId=$docId")
            return
        }

        val payload = hashMapOf<String, Any?>(
            Constants.TOURNAMENT_ID to tournamentId,
            Constants.MATCH_NUMBER to result.matchNumber,
            Constants.STATUS to MatchStatus.FINISHED.name,
            Constants.HOME_GOALS to result.homeGoals,
            Constants.AWAY_GOALS to result.awayGoals,
            Constants.WENT_TO_PENALTIES to result.wentToPenalties,
            Constants.UPDATED_AT to Timestamp.now(),

            // limpiar marca de notificación previa por si se está rejugando el partido
            "resultNotifiedAt" to FieldValue.delete()
        )

        if (result.wentToPenalties) {
            payload[Constants.HOME_PENALTIES] = result.homePenalties
            payload[Constants.AWAY_PENALTIES] = result.awayPenalties
        } else {
            payload[Constants.HOME_PENALTIES] = null
            payload[Constants.AWAY_PENALTIES] = null
        }

        ref.set(payload, SetOptions.merge()).await()

        Log.i(
            TAG,
            "Resultado simulado match=${result.matchNumber} score=${result.homeGoals}-${result.awayGoals}"
        )
    }

    suspend fun resetMatch(matchNumber: Int) {
        val tournamentId = BuildConfig.DEMO_TOURNAMENT_ID

        if (BuildConfig.TOURNAMENT_ID != tournamentId) {
            Log.w(TAG, "Reset ignorado: la build actual no es demo")
            return
        }

        val docId = "${tournamentId}_match_$matchNumber"
        val ref = firestore.collection(Constants.GAMES).document(docId)

        val snapshot = ref.get().await()
        if (!snapshot.exists()) {
            Log.w(TAG, "No existe juego demo para reset. matchNumber=$matchNumber")
            return
        }

        val payload = mapOf(
            Constants.STATUS to MatchStatus.SCHEDULED.name,
            Constants.HOME_GOALS to null,
            Constants.AWAY_GOALS to null,
            Constants.WENT_TO_PENALTIES to false,
            Constants.HOME_PENALTIES to null,
            Constants.AWAY_PENALTIES to null,
            Constants.UPDATED_AT to Timestamp.now(),
            "resultNotifiedAt" to FieldValue.delete()
        )

        ref.set(payload, SetOptions.merge()).await()
        Log.i(TAG, "Juego reseteado match=$matchNumber")
    }
}