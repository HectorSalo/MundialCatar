package com.skysam.hchirinos.mundialcatar.ui.gameday

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.User
import com.skysam.hchirinos.mundialcatar.repositories.GamesRepository
import com.skysam.hchirinos.mundialcatar.repositories.UsersRespository

class GamedayViewModel : ViewModel() {
    val games: LiveData<MutableList<Game>> = GamesRepository.getGamesAfter().asLiveData()
    val users: LiveData<MutableList<User>> = UsersRespository.getUsersByPoints().asLiveData()

    private val _game = MutableLiveData<Game?>()
    val game: LiveData<Game?> get() = _game

    fun editGame(game: Game?) {
        _game.value = game
    }

    fun updateResultGame(game: Game, users: MutableList<User>) {
        if (game.number in 1..48) GamesRepository.updateResultGameGroups(game, users)
        if (game.number in 49..64) GamesRepository.updateResultGamePlayOff(game, users)
    }

    fun starsGame(game: Game) {
        GamesRepository.startsGame(game)
    }
}