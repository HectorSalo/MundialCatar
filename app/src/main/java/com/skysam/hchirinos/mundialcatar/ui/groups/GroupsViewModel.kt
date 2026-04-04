package com.skysam.hchirinos.mundialcatar.ui.groups

import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.skysam.hchirinos.mundialcatar.BuildConfig
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.GroupStandingUi
import com.skysam.hchirinos.mundialcatar.dataclass.Team
import com.skysam.hchirinos.mundialcatar.repositories.DemoSeedRepository
import com.skysam.hchirinos.mundialcatar.repositories.GamesRepository
import com.skysam.hchirinos.mundialcatar.repositories.StandingsRepository
import com.skysam.hchirinos.mundialcatar.repositories.TeamsRespository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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