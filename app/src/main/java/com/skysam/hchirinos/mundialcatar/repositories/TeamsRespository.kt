package com.skysam.hchirinos.mundialcatar.repositories

import android.content.ContentValues
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.skysam.hchirinos.mundialcatar.common.Constants
import com.skysam.hchirinos.mundialcatar.common.WorldCup2026Teams
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.Team
import com.skysam.hchirinos.mundialcatar.dataclass.TeamEntity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Hector Chirinos on 07/05/2022.
 */

@Singleton
class TeamsRespository @Inject constructor(private val firestore: FirebaseFirestore) {

    private fun collection(): CollectionReference =
        firestore.collection(Constants.TEAMS)

    fun getAllTeams(): Flow<List<Team>> = callbackFlow {
        val registration = collection()
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val teams = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(TeamEntity::class.java)?.toDomain(doc.id)
                }

                trySend(teams)
            }

        awaitClose { registration.remove() }
    }

    fun TeamEntity.toDomain(id: String): Team =
        Team(
            id = id,
            tournamentId = tournamentId,
            code = code,
            name = name,
            shortName = shortName,
            group = group,
            confederation = confederation,
            flagCode = flagCode
        )

    suspend fun seedWorldCup2026IfNeeded() {
        // ¿Ya hay equipos para este torneo?
        val snapshot = collection()
            .whereEqualTo("tournamentId", WorldCup2026Teams.TOURNAMENT_ID)
            .limit(1)
            .get()
            .await()

        if (!snapshot.isEmpty) {
            // Ya está sembrado, no hacemos nada
            return
        }

        val batch = firestore.batch()

        WorldCup2026Teams.qualifiedTeams.forEach { seed ->
            // Usamos el código FIFA como ID del documento
            val docRef = collection().document(seed.code)
            batch.set(docRef, seed.toEntity())
        }

        batch.commit().await()
    }

    fun WorldCup2026Teams.TeamSeed.toEntity(
        tournamentId: String = WorldCup2026Teams.TOURNAMENT_ID
    ): TeamEntity =
        TeamEntity(
            tournamentId = tournamentId,
            code = code,
            name = name,
            shortName = shortName,
            group = group,
            confederation = confederation,
            flagCode = flagCode
        )
}