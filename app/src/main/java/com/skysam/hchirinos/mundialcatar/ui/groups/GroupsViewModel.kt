package com.skysam.hchirinos.mundialcatar.ui.groups

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.Team
import com.skysam.hchirinos.mundialcatar.repositories.GamesRepository
import com.skysam.hchirinos.mundialcatar.repositories.TeamsRespository

class GroupsViewModel : ViewModel() {
    val games: LiveData<MutableList<Game>> = GamesRepository.getAllGames().asLiveData()
    val teams: LiveData<MutableList<Team>> = TeamsRespository.getAllTeams().asLiveData()

    private val _index = MutableLiveData<Int>()
    val index: LiveData<Int> = _index

    fun setIndex(index: Int) {
        _index.value = index
    }
}