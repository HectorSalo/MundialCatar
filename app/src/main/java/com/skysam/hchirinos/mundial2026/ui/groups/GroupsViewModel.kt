package com.skysam.hchirinos.mundial2026.ui.groups

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.skysam.hchirinos.mundial2026.dataclass.Game
import com.skysam.hchirinos.mundial2026.dataclass.GroupStandingUi
import com.skysam.hchirinos.mundial2026.dataclass.Team
import com.skysam.hchirinos.mundial2026.repositories.GamesRepository
import com.skysam.hchirinos.mundial2026.repositories.StandingsRepository
import com.skysam.hchirinos.mundial2026.repositories.TeamsRespository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val gamesRepository: GamesRepository,
    private val teamsRespository: TeamsRespository,
    private val standingsRepository: StandingsRepository
) : ViewModel() {
    val games: LiveData<List<Game>> = gamesRepository.getAllGames().asLiveData()
    val teams: LiveData<List<Team>> = teamsRespository.getAllTeams().asLiveData()

    /** Standings desde Firestore (fuente oficial, calculados por Functions) */
    val standings: LiveData<Map<String, List<GroupStandingUi>>> =
        standingsRepository.getStandingsByGroup().asLiveData()

    private val _index = MutableLiveData<Int>().apply { value = 0 }
    val index: LiveData<Int> = _index

    fun setIndex(index: Int) {
        _index.value = index
    }
}