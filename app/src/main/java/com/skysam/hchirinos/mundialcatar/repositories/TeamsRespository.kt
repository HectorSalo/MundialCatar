package com.skysam.hchirinos.mundialcatar.repositories

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.skysam.hchirinos.mundialcatar.BuildConfig
import com.skysam.hchirinos.mundialcatar.common.Constants
import com.skysam.hchirinos.mundialcatar.dataclass.Team
import com.skysam.hchirinos.mundialcatar.dataclass.TeamEntity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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
            .whereEqualTo(Constants.TOURNAMENT_ID, BuildConfig.TOURNAMENT_ID)
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

}