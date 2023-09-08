package com.skysam.hchirinos.mundialcatar.ui.gameday

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.Team
import com.skysam.hchirinos.mundialcatar.dataclass.User
import com.skysam.hchirinos.mundialcatar.repositories.GamesRepository
import com.skysam.hchirinos.mundialcatar.repositories.TeamsRespository

class GamedayViewModel : ViewModel() {
    val games: LiveData<MutableList<Game>> = GamesRepository.getGamesAfter().asLiveData()
    val teams: LiveData<List<Team>> = TeamsRespository.getAllTeams().asLiveData()

    private val _game = MutableLiveData<Game?>()
    val game: LiveData<Game?> get() = _game

    fun starsGame(game: Game) {
        GamesRepository.startsGame(game)
    }

    fun setResultGame(game: Game, users: List<User>) {
        GamesRepository.setResultGame(game, users)
    }
}