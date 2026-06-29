package com.skysam.hchirinos.mundial2026.ui.gameday

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.skysam.hchirinos.mundial2026.BuildConfig
import com.skysam.hchirinos.mundial2026.dataclass.Game
import com.skysam.hchirinos.mundial2026.dataclass.GameScore
import com.skysam.hchirinos.mundial2026.dataclass.InfoApp
import com.skysam.hchirinos.mundial2026.dataclass.Team
import com.skysam.hchirinos.mundial2026.seeds.DemoSeedRepository
import com.skysam.hchirinos.mundial2026.repositories.GamesRepository
import com.skysam.hchirinos.mundial2026.repositories.InfoAppRepository
import com.skysam.hchirinos.mundial2026.repositories.TeamsRespository
import com.skysam.hchirinos.mundial2026.seeds.DemoGameResultSeeds
import com.skysam.hchirinos.mundial2026.seeds.DemoPredictionSeeds
import com.skysam.hchirinos.mundial2026.seeds.DemoPredictionsSeedRepository
import com.skysam.hchirinos.mundial2026.seeds.DemoResultsSimulationRepository
import com.skysam.hchirinos.mundial2026.seeds.MatchScheduleSlotsSeedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GamedayViewModel @Inject constructor(
    private val gamesRepository: GamesRepository,
    private val infoAppRepository: InfoAppRepository,
    private val teamsRespository: TeamsRespository,
    private val demoSeedRepository: DemoSeedRepository,
    private val demoPredictionsSeedRepository: DemoPredictionsSeedRepository,
    private val demoResultsSimulationRepository: DemoResultsSimulationRepository,
    private val matchScheduleSlotsSeedRepository: MatchScheduleSlotsSeedRepository
) : ViewModel() {
    val infoApp: LiveData<InfoApp> = infoAppRepository.getInfoApp().asLiveData()
    val games: LiveData<List<Game>> = gamesRepository.getGamesAfter().asLiveData()
    val teams: LiveData<List<Team>> = teamsRespository.getAllTeams().asLiveData()

    private val _game = MutableLiveData<Game>()
    val game: LiveData<Game> get() = _game

    fun setGame(game: Game) {
        _game.value = game
    }

    fun setResultGame(gameId: String, score: GameScore) {
        viewModelScope.launch {
            gamesRepository.setResultGame(gameId, score)
        }
    }

    init {
        viewModelScope.launch {
            //demoSeedRepository.deleteDemoStandings(BuildConfig.DEMO_TOURNAMENT_ID)
            //demoSeedRepository.ensureDemoDataIfNeeded()
            //demoPredictionsSeedRepository.deletePredictionsForUser()
            //demoPredictionsSeedRepository.resetUsersPointsForTournament()
            //demoPredictionsSeedRepository.seedFirst20PredictionsForUser(DemoPredictionSeeds.fourth5)
            //demoResultsSimulationRepository.simulateResults(DemoGameResultSeeds.fifth5)
            //matchScheduleSlotsSeedRepository.seedSlots()
            //gamesRepository.applyRoundOf32SchedulePatchIfNeeded()
        }
    }
}