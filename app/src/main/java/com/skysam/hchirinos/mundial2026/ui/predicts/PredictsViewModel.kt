package com.skysam.hchirinos.mundial2026.ui.predicts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.skysam.hchirinos.mundial2026.dataclass.Game
import com.skysam.hchirinos.mundial2026.dataclass.GamePredictionEntity
import com.skysam.hchirinos.mundial2026.dataclass.Team
import com.skysam.hchirinos.mundial2026.repositories.GamesRepository
import com.skysam.hchirinos.mundial2026.repositories.GamesUsersRepository
import com.skysam.hchirinos.mundial2026.repositories.TeamsRespository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class PredictsViewModel @Inject constructor(
    private val gamesUsersRepository: GamesUsersRepository,
    private val gamesRepository: GamesRepository,
    private val teamsRespository: TeamsRespository
) : ViewModel() {

    val gamesUser: LiveData<List<GamePredictionEntity>> =
        gamesUsersRepository.getGamesByUser().asLiveData()

    val games: LiveData<List<Game>> =
        gamesRepository.getAllGames().asLiveData()

    val teams: LiveData<List<Team>> =
        teamsRespository.getAllTeams().asLiveData()

    private val _draftScores = MutableLiveData<Map<Int, Pair<Int, Int>>>(emptyMap())
    val draftScores: LiveData<Map<Int, Pair<Int, Int>>> get() = _draftScores

    private val _syncStates =
        MutableLiveData<Map<Int, PredictsAdapter.SyncUiState>>(emptyMap())
    val syncStates: LiveData<Map<Int, PredictsAdapter.SyncUiState>> get() = _syncStates
    private val _uiMessage = MutableLiveData<String?>()
    val uiMessage: LiveData<String?> get() = _uiMessage
    private var persistedByMatch: Map<Int, GamePredictionEntity> = emptyMap()
    private var expectedByMatch: Map<Int, Pair<Int, Int>> = emptyMap()


    fun setPersistedPredictions(list: List<GamePredictionEntity>) {
        persistedByMatch = list.associateBy { it.matchNumber }

        val syncMap = _syncStates.value.orEmpty()
        if (syncMap.isEmpty()) return

        val toUpdate = syncMap.toMutableMap()

        toUpdate.keys.toList().forEach { matchNumber ->
            val state = toUpdate[matchNumber] ?: return@forEach
            if (state !is PredictsAdapter.SyncUiState.Saving &&
                state !is PredictsAdapter.SyncUiState.Pending &&
                state !is PredictsAdapter.SyncUiState.Error) return@forEach

            val persisted = persistedByMatch[matchNumber] ?: return@forEach
            val expected = expectedByMatch[matchNumber]

            val pending = persisted.hasPendingWrites
            val persistedScore = persisted.predictedHomeGoals to persisted.predictedAwayGoals
            val expectedOk = (expected == null) || (expected == persistedScore)

            android.util.Log.d(
                "PredictsVM",
                "reconcile match=$matchNumber state=$state pending=$pending expected=$expected persisted=$persistedScore expectedOk=$expectedOk"
            )

            if (pending && expectedOk) {
                // Sigue pendiente en cola / no confirmado
                toUpdate[matchNumber] = PredictsAdapter.SyncUiState.Pending
            } else if (!pending && expectedOk) {
                android.util.Log.d("PredictsVM", "CONFIRMED match=$matchNumber, clearing state")
                clearDraft(matchNumber)
                expectedByMatch = expectedByMatch.toMutableMap().apply { remove(matchNumber) }
                toUpdate.remove(matchNumber)
            } else {
                // Caso raro: llegó data que no coincide con lo esperado.
                // No tocamos estado para no ocultar problemas.
            }
        }

        _syncStates.value = toUpdate
    }

    fun setDraft(matchNumber: Int, homeGoals: Int, awayGoals: Int) {
        val safeHome = homeGoals.coerceAtLeast(0)
        val safeAway = awayGoals.coerceAtLeast(0)

        val current = _draftScores.value.orEmpty().toMutableMap()
        current[matchNumber] = safeHome to safeAway
        _draftScores.value = current
    }

    fun clearDraft(matchNumber: Int) {
        val current = _draftScores.value.orEmpty().toMutableMap()
        if (current.remove(matchNumber) != null) {
            _draftScores.value = current
        }
    }

    fun getDraft(matchNumber: Int): Pair<Int, Int>? =
        _draftScores.value.orEmpty()[matchNumber]

    fun consumeMessage() {
        _uiMessage.value = null
    }

    // -----------------------
    // Sync state ops
    // -----------------------

    private fun setSyncState(matchNumber: Int, state: PredictsAdapter.SyncUiState) {
        val current = _syncStates.value.orEmpty().toMutableMap()
        current[matchNumber] = state
        _syncStates.value = current
    }

    // -----------------------
    // Commit (Guardar)
    // -----------------------

    fun saveDraft(
        matchNumber: Int,
        userId: String,
        gameId: String,
        tournamentId: String
    ) {
        val draft = getDraft(matchNumber)
        if (draft == null) {
            _uiMessage.value = "No hay cambios para guardar"
            return
        }

        val (home, away) = draft
        val now = Date()

        val existing = persistedByMatch[matchNumber]
        val docId = existing?.id ?: "${userId}_${matchNumber}"

        val entity = existing?.copy(
            predictedHomeGoals = home,
            predictedAwayGoals = away,
            updatedAt = now
        ) ?: GamePredictionEntity(
            id = docId,
            userId = userId,
            gameId = gameId,
            tournamentId = tournamentId,
            matchNumber = matchNumber,
            predictedHomeGoals = home,
            predictedAwayGoals = away,
            points = 0,
            createdAt = now,
            updatedAt = now
        )

        // Guardamos "qué esperamos" para reconciliar cuando llegue snapshot
        expectedByMatch = expectedByMatch.toMutableMap().apply {
            put(matchNumber, home to away)
        }

        setSyncState(matchNumber, PredictsAdapter.SyncUiState.Saving)

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    gamesUsersRepository.upsertPredict(entity)
                }

                // OJO: aquí NO intentamos “confirmar” leyendo persistedByMatch,
                // porque depende del listener. Solo marcamos Pending.
                setSyncState(matchNumber, PredictsAdapter.SyncUiState.Pending)

                android.util.Log.d(
                    "PredictsVM",
                    "saveDraft OK match=$matchNumber -> Pending expected=${home to away}"
                )

            } catch (e: Exception) {
                setSyncState(matchNumber, PredictsAdapter.SyncUiState.Error(e.message))
                _uiMessage.value = "No se pudo guardar la predicción"
                android.util.Log.e("PredictsVM", "saveDraft FAIL match=$matchNumber", e)
            }
        }
    }
}
