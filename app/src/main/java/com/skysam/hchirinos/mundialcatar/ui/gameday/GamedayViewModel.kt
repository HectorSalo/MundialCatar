package com.skysam.hchirinos.mundialcatar.ui.gameday

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.repositories.GamesRepository

class GamedayViewModel : ViewModel() {
    val games: LiveData<MutableList<Game>> = GamesRepository.getGamesAfter().asLiveData()

    private val _game = MutableLiveData<Game?>()
    val game: LiveData<Game?> get() = _game

    fun editGame(game: Game?) {
        _game.value = game
    }

    fun updateResultGame(game: Game) {
        if (game.number in 1..48) GamesRepository.updateResultGameGroups(game)
        if (game.number in 49..64) GamesRepository.updateResultGamePlayOff(game)
    }

    fun starsGame(game: Game) {
        GamesRepository.startsGame(game)
    }
}