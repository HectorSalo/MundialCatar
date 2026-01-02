package com.skysam.hchirinos.mundialcatar.ui.predicts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.GamePredictionEntity
import com.skysam.hchirinos.mundialcatar.dataclass.Team
import com.skysam.hchirinos.mundialcatar.repositories.GamesRepository
import com.skysam.hchirinos.mundialcatar.repositories.GamesUsersRepository
import com.skysam.hchirinos.mundialcatar.repositories.TeamsRespository
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

    /**
     * Draft de marcador por partido: matchNumber -> (homeGoals, awayGoals)
     * Nota: Si un match NO está en el map, significa "sin predicción" ( - vs - ) cuando aplica.
     */
    private val _draftScores = MutableLiveData<Map<Int, Pair<Int, Int>>>(emptyMap())
    val draftScores: LiveData<Map<Int, Pair<Int, Int>>> get() = _draftScores

    /**
     * Mensaje UI simple (Snackbar).
     */
    private val _uiMessage = MutableLiveData<String?>()
    val uiMessage: LiveData<String?> get() = _uiMessage

    /**
     * Predicciones persistidas (para decidir create/update).
     * Se carga desde el Fragment al recibir gamesUser.
     */
    private var persistedByMatch: Map<Int, GamePredictionEntity> = emptyMap()

    fun setPersistedPredictions(list: List<GamePredictionEntity>) {
        persistedByMatch = list.associateBy { it.matchNumber }
    }

    // -----------------------
    // Draft ops
    // -----------------------

    fun setDraft(matchNumber: Int, homeGoals: Int, awayGoals: Int) {
        val safeHome = homeGoals.coerceAtLeast(0)
        val safeAway = awayGoals.coerceAtLeast(0)

        val current = _draftScores.value.orEmpty().toMutableMap()
        current[matchNumber] = safeHome to safeAway
        _draftScores.value = current
    }

    fun clearDraft(matchNumber: Int) {
        val current = _draftScores.value.orEmpty().toMutableMap()
        current.remove(matchNumber)
        _draftScores.value = current
    }

    fun getDraft(matchNumber: Int): Pair<Int, Int>? =
        _draftScores.value.orEmpty()[matchNumber]

    fun consumeMessage() {
        _uiMessage.value = null
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

        // Regla docId: "userId_matchNumber"
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

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    if (existing != null) {
                        gamesUsersRepository.updatePredict(entity)
                    } else {
                        gamesUsersRepository.createPredict(entity)
                    }
                }

                // Actualiza cache persistido y limpia draft
                persistedByMatch = persistedByMatch.toMutableMap().apply {
                    put(matchNumber, entity)
                }
                clearDraft(matchNumber)

                _uiMessage.value = "Predicción guardada"
            } catch (e: Exception) {
                _uiMessage.value = "No se pudo guardar la predicción"
            }
        }
    }
}
