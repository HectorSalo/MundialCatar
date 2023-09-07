package com.skysam.hchirinos.mundialcatar.ui.results

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.Team
import com.skysam.hchirinos.mundialcatar.repositories.GamesRepository
import com.skysam.hchirinos.mundialcatar.repositories.TeamsRespository

class ResultsViewModel : ViewModel() {
    val games: LiveData<MutableList<Game>> = GamesRepository.getGamesBefore().asLiveData()
    val teams: LiveData<List<Team>> = TeamsRespository.getAllTeams().asLiveData()
}