package com.skysam.hchirinos.mundialcatar.ui.predicts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.skysam.hchirinos.mundialcatar.dataclass.GameUser
import com.skysam.hchirinos.mundialcatar.repositories.UsersRespository

class PredictsViewModel : ViewModel() {
    val games: LiveData<MutableList<GameUser>> = UsersRespository.getAllGames().asLiveData()

    private val _game = MutableLiveData<GameUser>()
    val game: LiveData<GameUser> get() = _game

    fun updatePredict(game: GameUser) {
        _game.value = game
    }
}