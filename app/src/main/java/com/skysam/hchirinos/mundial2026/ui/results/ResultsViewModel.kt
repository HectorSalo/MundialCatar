package com.skysam.hchirinos.mundial2026.ui.results

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.skysam.hchirinos.mundial2026.dataclass.Game
import com.skysam.hchirinos.mundial2026.dataclass.Team
import com.skysam.hchirinos.mundial2026.repositories.GamesRepository
import com.skysam.hchirinos.mundial2026.repositories.TeamsRespository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ResultsViewModel @Inject constructor(
    private val gamesRepository: GamesRepository,
    private val teamsRespository: TeamsRespository
) : ViewModel() {
    val games: LiveData<List<Game>> = gamesRepository.getGamesBefore().asLiveData()
    val teams: LiveData<List<Team>> = teamsRespository.getAllTeams().asLiveData()
}