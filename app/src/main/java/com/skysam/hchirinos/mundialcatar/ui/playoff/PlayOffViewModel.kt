package com.skysam.hchirinos.mundialcatar.ui.playoff

import androidx.lifecycle.*
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.repositories.GamesRepository

class PlayOffViewModel : ViewModel() {
    val games: LiveData<MutableList<Game>> = GamesRepository.getAllGames().asLiveData()

    private val _index = MutableLiveData<Int>()
    val index: LiveData<Int> = _index

    fun setIndex(index: Int) {
        _index.value = index
    }
}