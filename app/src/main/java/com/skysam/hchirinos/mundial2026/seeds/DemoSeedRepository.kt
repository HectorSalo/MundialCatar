package com.skysam.hchirinos.mundial2026.seeds

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.skysam.hchirinos.mundial2026.BuildConfig
import com.skysam.hchirinos.mundial2026.common.Constants
import com.skysam.hchirinos.mundial2026.common.WorldCup2026Teams
import com.skysam.hchirinos.mundial2026.dataclass.MatchStatus
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Crea datos demo en Firestore (mismas colecciones, distinto tournamentId).
 * Idempotente: valida Firestore antes de escribir.
 */
@Singleton
class DemoSeedRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val functions: FirebaseFunctions
) {
    companion object {
        private const val TAG = "DemoSeedRepository"
        @Volatile private var seeded = false
    }

    suspend fun ensureDemoDataIfNeeded() {
        if (BuildConfig.TOURNAMENT_ID != BuildConfig.DEMO_TOURNAMENT_ID) return
        if (seeded) return

        val demo = BuildConfig.DEMO_TOURNAMENT_ID
        val real = BuildConfig.REAL_TOURNAMENT_ID

        try {
            val teamsExist = firestore.collection(Constants.TEAMS)
                .whereEqualTo(Constants.TOURNAMENT_ID, demo).limit(1).get().await().size() > 0
            val gamesExist = firestore.collection(Constants.GAMES)
                .whereEqualTo(Constants.TOURNAMENT_ID, demo).limit(1).get().await().size() > 0

            var dataCreated = false

            if (!teamsExist) {
                seedDemoTeams(demo)
                dataCreated = true
            }
            if (!gamesExist) {
                cloneDemoGames(real, demo)
                dataCreated = true
            }
            if (ensureDemoInfoApp(real, demo)) dataCreated = true
            if (dataCreated) recomputeStandings(demo)

            seeded = true
            Log.i(TAG, "Demo seed check complete (created=$dataCreated)")
        } catch (e: Exception) {
            Log.e(TAG, "Error seeding demo data", e)
        }
    }

    // ---- Teams ----

    private suspend fun seedDemoTeams(demoId: String) {
        val batch = firestore.batch()
        for (t in WorldCup2026Teams.qualifiedTeams) {
            val ref = firestore.collection(Constants.TEAMS).document("${demoId}_${t.code}")
            batch.set(ref, hashMapOf(
                Constants.TOURNAMENT_ID to demoId,
                "code" to t.code,
                Constants.NAME to t.name,
                "shortName" to t.shortName,
                "confederation" to t.confederation,
                "flagCode" to t.flagCode,
                Constants.GROUP to t.group,
                Constants.CREATED_AT to Timestamp.Companion.now(),
                Constants.UPDATED_AT to Timestamp.Companion.now()
            ))
        }
        batch.commit().await()
        Log.i(TAG, "Seeded ${WorldCup2026Teams.qualifiedTeams.size} demo teams")
    }

    // ---- Games ----

    private suspend fun cloneDemoGames(realId: String, demoId: String) {
        val realTeams = firestore.collection(Constants.TEAMS)
            .whereEqualTo(Constants.TOURNAMENT_ID, realId)
            .get()
            .await()

        val realIdToCode = realTeams.documents.associate { doc ->
            doc.id to (doc.getString("code") ?: doc.id)
        }

        val realGames = firestore.collection(Constants.GAMES)
            .whereEqualTo(Constants.TOURNAMENT_ID, realId)
            .get()
            .await()

        if (realGames.isEmpty) {
            Log.w(TAG, "No real games found to clone")
            return
        }

        val batch = firestore.batch()

        for (doc in realGames.documents) {
            val original = doc.data ?: continue
            val matchNum = (original[Constants.MATCH_NUMBER] as? Long)?.toInt() ?: continue

            val originalHomeTeamId = original["homeTeamId"] as? String
            val originalAwayTeamId = original["awayTeamId"] as? String

            val clone = HashMap<String, Any?>(original)

            // Mantener mismo shape, pero apuntando al torneo demo
            clone[Constants.TOURNAMENT_ID] = demoId

            // Remapear ids de equipos reales al espacio demo; placeholders se conservan tal cual
            clone["homeTeamId"] = mapTeamIdForDemo(
                originalTeamId = originalHomeTeamId,
                realIdToCode = realIdToCode,
                demoId = demoId
            )

            clone["awayTeamId"] = mapTeamIdForDemo(
                originalTeamId = originalAwayTeamId,
                realIdToCode = realIdToCode,
                demoId = demoId
            )

            // Reiniciar estado del partido, pero conservando los campos
            clone[Constants.STATUS] = MatchStatus.SCHEDULED.name
            clone[Constants.START] = false

            clone[Constants.HOME_GOALS] = null
            clone[Constants.AWAY_GOALS] = null
            clone[Constants.WENT_TO_PENALTIES] = false
            clone[Constants.HOME_PENALTIES] = null
            clone[Constants.AWAY_PENALTIES] = null

            // Campos derivados/eventuales
            clone.remove("resultNotifiedAt")
            clone.remove("winnerTeamId")
            clone.remove("loserTeamId")

            // Metadata técnica
            clone[Constants.UPDATED_AT] = Timestamp.Companion.now()
            clone[Constants.CREATED_AT] = Timestamp.Companion.now()

            val ref = firestore.collection(Constants.GAMES)
                .document("${demoId}_match_$matchNum")

            @Suppress("UNCHECKED_CAST")
            batch.set(ref, clone as Map<String, Any>)
        }

        batch.commit().await()
        Log.i(TAG, "Cloned ${realGames.size()} games to demo")
    }

    private fun mapTeamIdForDemo(
        originalTeamId: String?,
        realIdToCode: Map<String, String>,
        demoId: String
    ): String {
        if (originalTeamId.isNullOrBlank()) return ""

        val realCode = realIdToCode[originalTeamId]
        return if (realCode != null) {
            "${demoId}_$realCode"
        } else {
            originalTeamId
        }
    }

    // ---- InfoApp ----

    /** @return true si se creó el doc */
    private suspend fun ensureDemoInfoApp(realId: String, demoId: String): Boolean {
        val demoRef = firestore.collection(Constants.INFO_APP).document(demoId)
        if (demoRef.get().await().exists()) return false

        val realSnap = firestore.collection(Constants.INFO_APP).document(realId).get().await()
        val data = if (realSnap.exists()) hashMapOf<String, Any>(
            Constants.VERSION_CODE to (realSnap.getLong(Constants.VERSION_CODE) ?: 1L),
            Constants.VERSION_NAME to (realSnap.getString(Constants.VERSION_NAME) ?: "1.0")
        ) else hashMapOf(Constants.VERSION_CODE to 1L, Constants.VERSION_NAME to "1.0")

        demoRef.set(data).await()
        Log.i(TAG, "Created demo infoApp doc")
        return true
    }

    // ---- Standings ----

    private suspend fun recomputeStandings(tournamentId: String) {
        try {
            functions.getHttpsCallable("recomputeStandingsForTournament")
                .call(hashMapOf("tournamentId" to tournamentId)).await()
            Log.i(TAG, "Standings recomputed for $tournamentId")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to recompute standings: ${e.message}")
        }
    }

    suspend fun reseedDemoGames() {
        if (BuildConfig.TOURNAMENT_ID != BuildConfig.DEMO_TOURNAMENT_ID) {
            Log.w(TAG, "reseedDemoGames() ignored: current build is not demo")
            return
        }

        val demo = BuildConfig.DEMO_TOURNAMENT_ID
        val real = BuildConfig.REAL_TOURNAMENT_ID

        try {
            Log.i(TAG, "Starting demo games reseed...")

            deleteDemoGames(demo)
            deleteDemoStandings(demo)
            cloneDemoGames(real, demo)
            recomputeStandings(demo)

            seeded = false
            Log.i(TAG, "Demo games reseed completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error reseeding demo games", e)
        }
    }

    private suspend fun deleteDemoGames(demoId: String) {
        val snapshot = firestore.collection(Constants.GAMES)
            .whereEqualTo(Constants.TOURNAMENT_ID, demoId)
            .get()
            .await()

        if (snapshot.isEmpty) {
            Log.i(TAG, "No demo games found to delete")
            return
        }

        snapshot.documents.chunked(400).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()
        }

        Log.i(TAG, "Deleted ${snapshot.size()} demo games")
    }

    suspend fun deleteDemoStandings(demoId: String) {
        val snapshot = firestore.collection(Constants.GAMES)
            .whereEqualTo(Constants.TOURNAMENT_ID, demoId)
            .get()
            .await()

        if (snapshot.isEmpty) {
            Log.i(TAG, "No demo standings found to delete")
            return
        }

        snapshot.documents.chunked(400).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()
        }

        Log.i(TAG, "Deleted ${snapshot.size()} demo standings docs")
    }
}