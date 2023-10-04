package com.skysam.hchirinos.mundialcatar.ui.gameday

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.InfoApp
import com.skysam.hchirinos.mundialcatar.dataclass.Team
import com.skysam.hchirinos.mundialcatar.repositories.GamesRepository
import com.skysam.hchirinos.mundialcatar.repositories.InfoAppRepository
import com.skysam.hchirinos.mundialcatar.repositories.TeamsRespository

class GamedayViewModel : ViewModel() {
    val infoApp: LiveData<InfoApp> = InfoAppRepository.getInfoApp().asLiveData()
    val games: LiveData<MutableList<Game>> = GamesRepository.getGamesAfter().asLiveData()
    val teams: LiveData<List<Team>> = TeamsRespository.getAllTeams().asLiveData()

    private val _game = MutableLiveData<Game>()
    val game: LiveData<Game> get() = _game

    fun setGame(game: Game) {
        _game.value = game
    }

    fun starsGame(game: Game) {
        GamesRepository.startsGame(game)
    }

    fun setResultGame(game: Game) {
        GamesRepository.setResultGame(game)
    }
}