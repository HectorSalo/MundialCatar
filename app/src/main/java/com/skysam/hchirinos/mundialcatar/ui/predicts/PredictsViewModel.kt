package com.skysam.hchirinos.mundialcatar.ui.predicts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.GameToView
import com.skysam.hchirinos.mundialcatar.dataclass.GameUser
import com.skysam.hchirinos.mundialcatar.dataclass.Team
import com.skysam.hchirinos.mundialcatar.repositories.GamesRepository
import com.skysam.hchirinos.mundialcatar.repositories.GamesUsersRepository
import com.skysam.hchirinos.mundialcatar.repositories.TeamsRespository

class PredictsViewModel : ViewModel() {
    val gamesUser: LiveData<List<GameUser>> = GamesUsersRepository.getGamesByUser().asLiveData()
    val games: LiveData<MutableList<Game>> = GamesRepository.getAllGames().asLiveData()
    val teams: LiveData<MutableList<Team>> = TeamsRespository.getAllTeams().asLiveData()

    private val _gameUser = MutableLiveData<GameToView>()
    val gameUser: LiveData<GameToView> get() = _gameUser

    fun editPredict(gameToView: GameToView) {
        _gameUser.value = gameToView
    }

    fun updatePredict(gameUser: GameUser) {
        GamesUsersRepository.updatePredict(gameUser)
    }

    fun createPredict(gameUser: GameUser) {
        GamesUsersRepository.createPredict(gameUser)
    }
}