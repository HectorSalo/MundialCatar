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
import com.skysam.hchirinos.mundialcatar.repositories.UsersRepository

class GamedayViewModel : ViewModel() {
    val games: LiveData<MutableList<Game>> = GamesRepository.getGamesAfter().asLiveData()
    val users: LiveData<List<User>> = UsersRepository.getUsersByPoints().asLiveData()
    val teams: LiveData<List<Team>> = TeamsRespository.getAllTeams().asLiveData()

    private val _game = MutableLiveData<Game?>()
    val game: LiveData<Game?> get() = _game

    fun editGame(game: Game?) {
        _game.value = game
    }

    fun getTeamById(id: String): LiveData<Team> {
        return TeamsRespository.getTeamById(id).asLiveData()
    }

    fun starsGame(game: Game) {
        GamesRepository.startsGame(game)
    }

    fun updatePOff(users: MutableList<User>, games: MutableList<Game>) {
        UsersRepository.updateTimeGame(games, users)
    }
}