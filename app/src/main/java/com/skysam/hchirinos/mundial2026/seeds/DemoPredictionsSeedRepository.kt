package com.skysam.hchirinos.mundial2026.seeds

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.skysam.hchirinos.mundial2026.BuildConfig
import com.skysam.hchirinos.mundial2026.common.Constants
import com.skysam.hchirinos.mundial2026.repositories.Auth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class DemoPredictionsSeedRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: Auth
) {
    companion object {
        private const val TAG = "DemoPredictionsSeedRepo"
    }

    suspend fun seedFirst20PredictionsForUser() {
        val userId = auth.getCurrentUser()?.uid ?: error("No session")
        require(userId.isNotBlank()) { "userId no puede estar vacío" }

        val tournamentId = BuildConfig.DEMO_TOURNAMENT_ID

        if (BuildConfig.TOURNAMENT_ID != tournamentId) {
            Log.w(TAG, "Seed ignorado: la build actual no es demo")
            return
        }

        val gamesSnapshot = firestore.collection(Constants.GAMES)
            .whereEqualTo(Constants.TOURNAMENT_ID, tournamentId)
            .get()
            .await()

        val gamesByMatchNumber = gamesSnapshot.documents.associateBy {
            it.getLong(Constants.MATCH_NUMBER)?.toInt() ?: -1
        }

        val batch = firestore.batch()
        var createdCount = 0

        DemoPredictionSeeds.first20.forEach { seed ->
            val gameDoc = gamesByMatchNumber[seed.matchNumber]

            if (gameDoc == null) {
                Log.w(TAG, "No se encontró juego demo para matchNumber=${seed.matchNumber}")
                return@forEach
            }

            val docId = "${userId}_${seed.matchNumber}"
            val now = Timestamp.now()

            val data = hashMapOf(
                Constants.USER_ID to userId,
                Constants.GAME_ID to gameDoc.id,
                Constants.TOURNAMENT_ID to tournamentId,
                Constants.MATCH_NUMBER to seed.matchNumber,
                Constants.PREDICTED_HOME_GOALS to seed.predictedHomeGoals,
                Constants.PREDICTED_AWAY_GOALS to seed.predictedAwayGoals,
                Constants.POINTS to 0,
                Constants.CREATED_AT to now,
                Constants.UPDATED_AT to now
            )

            val ref = firestore.collection(Constants.GAMES_USERS).document(docId)
            batch.set(ref, data, SetOptions.merge())
            createdCount++
        }

        batch.commit().await()
        Log.i(TAG, "Predicciones demo sembradas: $createdCount para userId=$userId")
    }

    suspend fun deletePredictionsForUser() {
        val userId = auth.getCurrentUser()?.uid ?: error("No session")
        require(userId.isNotBlank()) { "userId no puede estar vacío" }

        val tournamentId = BuildConfig.DEMO_TOURNAMENT_ID

        val snapshot = firestore.collection(Constants.GAMES_USERS)
            .whereEqualTo(Constants.USER_ID, userId)
            .whereEqualTo(Constants.TOURNAMENT_ID, tournamentId)
            .get()
            .await()

        if (snapshot.isEmpty) {
            Log.i(TAG, "No hay predicciones demo para borrar del userId=$userId")
            return
        }

        snapshot.documents.chunked(400).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { doc -> batch.delete(doc.reference) }
            batch.commit().await()
        }

        Log.i(TAG, "Predicciones demo eliminadas: ${snapshot.size()} para userId=$userId")
    }
}