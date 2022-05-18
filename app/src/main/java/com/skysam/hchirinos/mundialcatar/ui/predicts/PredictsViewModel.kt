package com.skysam.hchirinos.mundialcatar.ui.predicts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.GameUser
import com.skysam.hchirinos.mundialcatar.dataclass.Team
import com.skysam.hchirinos.mundialcatar.repositories.GamesRepository
import com.skysam.hchirinos.mundialcatar.repositories.TeamsRespository
import com.skysam.hchirinos.mundialcatar.repositories.UsersRespository

class PredictsViewModel : ViewModel() {
    val gamesUser: LiveData<MutableList<GameUser>> = UsersRespository.getAllGames().asLiveData()
    val games: LiveData<MutableList<Game>> = GamesRepository.getGamesAfter().asLiveData()
    val teams: LiveData<MutableList<Team>> = TeamsRespository.getAllTeams().asLiveData()

    private val _gameUser = MutableLiveData<GameUser?>()
    val gameUser: LiveData<GameUser?> get() = _gameUser

    fun editPredict(game: GameUser?) {
        _gameUser.value = game
    }

    fun updatePredict(game: GameUser) {
        if (game.number in 1..48) UsersRespository.updatePredictGroups(game)
        if (game.number in 49..64) UsersRespository.updatePredictPlayOff(game)
    }
}