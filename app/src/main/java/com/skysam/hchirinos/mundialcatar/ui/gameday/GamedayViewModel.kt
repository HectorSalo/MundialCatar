package com.skysam.hchirinos.mundialcatar.ui.gameday

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.GameScore
import com.skysam.hchirinos.mundialcatar.dataclass.InfoApp
import com.skysam.hchirinos.mundialcatar.dataclass.Team
import com.skysam.hchirinos.mundialcatar.repositories.GamesRepository
import com.skysam.hchirinos.mundialcatar.repositories.InfoAppRepository
import com.skysam.hchirinos.mundialcatar.repositories.TeamsRespository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GamedayViewModel @Inject constructor(
    private val gamesRepository: GamesRepository,
    private val infoAppRepository: InfoAppRepository,
    private val teamsRespository: TeamsRespository
) : ViewModel() {
    val infoApp: LiveData<InfoApp> = infoAppRepository.getInfoApp().asLiveData()
    val games: LiveData<List<Game>> = gamesRepository.getGamesAfter().asLiveData()
    val teams: LiveData<List<Team>> = teamsRespository.getAllTeams().asLiveData()

    private val _game = MutableLiveData<Game>()
    val game: LiveData<Game> get() = _game

    fun setGame(game: Game) {
        _game.value = game
    }

    fun starsGame(game: Game) {
        gamesRepository.markGameStarted(game.id)
    }

    fun setResultGame(gameId: String, score: GameScore) {
        viewModelScope.launch {
            gamesRepository.setResultGame(gameId, score)
        }
    }
}