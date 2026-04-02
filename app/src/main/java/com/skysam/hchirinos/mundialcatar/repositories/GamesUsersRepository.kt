package com.skysam.hchirinos.mundialcatar.repositories

import android.content.Context
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.skysam.hchirinos.mundialcatar.R
import com.skysam.hchirinos.mundialcatar.BuildConfig
import com.skysam.hchirinos.mundialcatar.common.Constants
import com.skysam.hchirinos.mundialcatar.dataclass.GamePredictionEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Hector Chirinos on 28/09/2023.
 */
@Singleton
class GamesUsersRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: Auth
) {

    companion object {
        private const val TAG = "GamesUsersRepository"
    }

    private fun collection(): CollectionReference =
        firestore.collection(Constants.GAMES_USERS)

    private fun buildDocId(userId: String, matchNumber: Int): String =
        "${userId}_$matchNumber"

    suspend fun upsertPredict(entity: GamePredictionEntity) {
        val currentUserId = auth.getCurrentUser()?.uid ?: error("No session")

        val docId = entity.id.ifBlank { buildDocId(currentUserId, entity.matchNumber) }

        val data = hashMapOf(
            Constants.ID_USER to currentUserId,
            Constants.NUMBER to entity.matchNumber,
            Constants.GOALS1 to entity.predictedHomeGoals,
            Constants.GOALS2 to entity.predictedAwayGoals,
            Constants.POINTS to entity.points,
            Constants.GAME_ID to entity.gameId,
            Constants.TOURNAMENT_ID to entity.tournamentId,
            Constants.UPDATED_AT to Timestamp.now()
        )

        entity.createdAt?.let { data[Constants.CREATED_AT] = Timestamp(it) }

        collection()
            .document(docId)
            .set(data, SetOptions.merge())
            .await()
    }

    /**
     * Listener con metadata para distinguir cache/pendiente.
     * Por ahora mantenemos la firma igual: Flow<List<GamePredictionEntity>>.
     * Luego (si quieres), podemos exponer otro Flow con pendingWrites por match.
     */
    fun getGamesByUser(): Flow<List<GamePredictionEntity>> = callbackFlow {
        val currentUserId = auth.getCurrentUser()?.uid
        if (currentUserId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = collection()
            .whereEqualTo(Constants.ID_USER, currentUserId)
            .whereEqualTo(Constants.TOURNAMENT_ID, BuildConfig.TOURNAMENT_ID)
            .orderBy(Constants.NUMBER, Query.Direction.ASCENDING)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null || snapshot == null) {
                    Log.w(TAG, "Listen failed.", error)
                    return@addSnapshotListener
                }

                val fromCache = snapshot.metadata.isFromCache
                Log.d(TAG, "snapshot: fromCache=$fromCache size=${snapshot.size()}")

                val gamesUser = snapshot.documents.map { doc ->
                    val match = doc.getLong(Constants.NUMBER)?.toInt() ?: 0
                    val g1 = doc.getLong(Constants.GOALS1)?.toInt() ?: 0
                    val g2 = doc.getLong(Constants.GOALS2)?.toInt() ?: 0
                    val pending = doc.metadata.hasPendingWrites()

                    Log.d(TAG, "doc match=$match $g1-$g2 pending=$pending")

                    GamePredictionEntity(
                        id = doc.id,
                        userId = doc.getString(Constants.ID_USER) ?: "",
                        gameId = doc.getString(Constants.GAME_ID) ?: "",
                        tournamentId = doc.getString(Constants.TOURNAMENT_ID) ?: "",
                        matchNumber = match,
                        predictedHomeGoals = g1,
                        predictedAwayGoals = g2,
                        points = doc.getLong(Constants.POINTS)?.toInt() ?: 0,
                        createdAt = doc.getTimestamp(Constants.CREATED_AT)?.toDate(),
                        updatedAt = doc.getTimestamp(Constants.UPDATED_AT)?.toDate(),
                        hasPendingWrites = pending,
                        isFromCache = fromCache
                    )
                }

                trySend(gamesUser)
            }

        awaitClose { registration.remove() }
    }
}