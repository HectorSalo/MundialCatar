package com.skysam.hchirinos.mundial2026.repositories

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.skysam.hchirinos.mundial2026.BuildConfig
import com.skysam.hchirinos.mundial2026.common.Constants
import com.skysam.hchirinos.mundial2026.common.FlagsMapper
import com.skysam.hchirinos.mundial2026.dataclass.GroupStandingUi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lee la colección "standings" de Firestore (fuente oficial calculada por Functions).
 * Docs esperados: {tournamentId}_A … {tournamentId}_L  y  {tournamentId}_bestThirds
 */
@Singleton
class StandingsRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val TAG = "StandingsRepository"
    }

    fun getStandingsByGroup(): Flow<Map<String, List<GroupStandingUi>>> = callbackFlow {
        val registration = firestore.collection(Constants.STANDINGS)
            .whereEqualTo(Constants.TOURNAMENT_ID, BuildConfig.TOURNAMENT_ID)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Log.w(TAG, "Listen failed.", error)
                    return@addSnapshotListener
                }

                // 1) Extraer set de bestThird teamIds del doc especial
                val bestThirdIds = mutableSetOf<String>()
                val groupDocs = mutableListOf<com.google.firebase.firestore.DocumentSnapshot>()

                for (doc in snapshot.documents) {
                    if (doc.id.endsWith(Constants.SUFFIX_BEST_THIRDS)) {
                        parseBestThirds(doc)?.let { bestThirdIds.addAll(it) }
                    } else {
                        groupDocs.add(doc)
                    }
                }

                // 2) Mapear cada doc de grupo → List<GroupStandingUi>
                val result = mutableMapOf<String, List<GroupStandingUi>>()
                for (doc in groupDocs) {
                    val group = doc.getString(Constants.GROUP) ?: continue
                    val table = parseTable(doc, group, bestThirdIds)
                    if (table.isNotEmpty()) {
                        result[group] = table
                    }
                }

                trySend(result)
            }

        awaitClose { registration.remove() }
    }

    // ------------------------------------------------------------------
    // Parsers internos
    // ------------------------------------------------------------------

    @Suppress("UNCHECKED_CAST")
    private fun parseTable(
        doc: com.google.firebase.firestore.DocumentSnapshot,
        group: String,
        bestThirdIds: Set<String>
    ): List<GroupStandingUi> {
        val table = doc.get(Constants.TABLE) as? List<Map<String, Any?>> ?: return emptyList()

        return table.map { row ->
            val teamId = row[Constants.TEAM_ID] as? String ?: ""
            val position = (row[Constants.POSITION] as? Long)?.toInt() ?: 0

            GroupStandingUi(
                teamId = teamId,
                teamName = row[Constants.TEAM_NAME] as? String ?: teamId,
                flagUrl = FlagsMapper.from(row[Constants.FLAG_URL] as? String),
                group = group,
                played = (row[Constants.PLAYED] as? Long)?.toInt() ?: 0,
                wins = (row[Constants.WINS] as? Long)?.toInt() ?: 0,
                draws = (row[Constants.DRAWS] as? Long)?.toInt() ?: 0,
                losses = (row[Constants.LOSSES] as? Long)?.toInt() ?: 0,
                goalsFor = (row[Constants.GOALS_FOR] as? Long)?.toInt() ?: 0,
                goalsAgainst = (row[Constants.GOALS_AGAINST] as? Long)?.toInt() ?: 0,
                goalDiff = (row[Constants.GOAL_DIFF] as? Long)?.toInt() ?: 0,
                points = (row[Constants.POINTS] as? Long)?.toInt() ?: 0,
                position = position,
                qualifiesAsTopTwo = position in 1..2,
                qualifiesAsBestThird = bestThirdIds.contains(teamId)
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseBestThirds(
        doc: com.google.firebase.firestore.DocumentSnapshot
    ): Set<String>? {
        val list = doc.get(Constants.SUFFIX_BEST_THIRDS) as? List<Map<String, Any?>> ?: return null
        return list.mapNotNull { it[Constants.TEAM_ID] as? String }.toSet()
    }
}
