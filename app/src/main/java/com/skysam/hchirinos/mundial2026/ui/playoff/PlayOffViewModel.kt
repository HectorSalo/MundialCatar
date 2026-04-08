package com.skysam.hchirinos.mundial2026.ui.playoff

import androidx.lifecycle.*
import com.skysam.hchirinos.mundial2026.dataclass.Game
import com.skysam.hchirinos.mundial2026.dataclass.Team
import com.skysam.hchirinos.mundial2026.repositories.GamesRepository
import com.skysam.hchirinos.mundial2026.repositories.TeamsRespository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlayOffViewModel @Inject constructor(
    private val gamesRepository: GamesRepository,
    private val teamsRespository: TeamsRespository
) : ViewModel() {
    val games: LiveData<List<Game>> = gamesRepository.getAllGames().asLiveData()
    val teams: LiveData<List<Team>> = teamsRespository.getAllTeams().asLiveData()

    private val _index = MutableLiveData(0)
    val index: LiveData<Int> = _index

    fun setIndex(index: Int) {
        _index.value = index
    }
}