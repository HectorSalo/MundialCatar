package com.skysam.hchirinos.mundialcatar.ui.groups

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.GroupStandingUi
import com.skysam.hchirinos.mundialcatar.dataclass.MatchStage
import com.skysam.hchirinos.mundialcatar.dataclass.MutableTeamStats
import com.skysam.hchirinos.mundialcatar.dataclass.Team
import com.skysam.hchirinos.mundialcatar.repositories.GamesRepository
import com.skysam.hchirinos.mundialcatar.repositories.TeamsRespository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val gamesRepository: GamesRepository,
    private val teamsRespository: TeamsRespository
) : ViewModel() {
    val games: LiveData<List<Game>> = gamesRepository.getAllGames().asLiveData()
    val teams: LiveData<List<Team>> = teamsRespository.getAllTeams().asLiveData()

    private val _index = MutableLiveData<Int>()
    val index: LiveData<Int> = _index

    fun setIndex(index: Int) {
        _index.value = index
    }

    private val _standings = MediatorLiveData<Map<String, List<GroupStandingUi>>>()
    val standings: LiveData<Map<String, List<GroupStandingUi>>> = _standings

    init {
        _standings.addSource(games) { recomputeStandings() }
        _standings.addSource(teams) { recomputeStandings() }
    }

    private fun recomputeStandings() {
        val gamesList = games.value ?: return
        val teamsList = teams.value ?: return

        _standings.value = computeStandingsByGroup(teamsList, gamesList)
    }

    // --------------------------------------------------
    // Helpers de cálculo
    // --------------------------------------------------

    private fun computeStandingsByGroup(
        teams: List<Team>,
        games: List<Game>
    ): Map<String, List<GroupStandingUi>> {

        // 1) Inicializar stats por grupo y team
        val statsByGroup: MutableMap<String, MutableMap<String, MutableTeamStats>> = mutableMapOf()
        val teamsById = teams.associateBy { it.id }

        teams.forEach { team ->
            val group = team.group
            val groupMap = statsByGroup.getOrPut(group!!) { mutableMapOf() }
            groupMap[team.id] = MutableTeamStats(
                teamId = team.id,
                teamName = team.name,
                flagUrl = team.flagCode,
                group = group
            )
        }

        // 2) Procesar partidos de fase de grupos con score
        games
            .filter { it.stage == MatchStage.GROUP && it.score != null }
            .forEach { game ->
                val group = game.group ?: return@forEach
                val score = game.score ?: return@forEach

                val groupMap = statsByGroup.getOrPut(group) { mutableMapOf() }

                val homeTeamInfo = teamsById[game.homeTeamId]
                val awayTeamInfo = teamsById[game.awayTeamId]

                val homeStats = groupMap.getOrPut(game.homeTeamId) {
                    MutableTeamStats(
                        teamId = game.homeTeamId,
                        teamName = homeTeamInfo?.name ?: game.homeTeamId,
                        flagUrl = homeTeamInfo?.flagCode ?: "",
                        group = group
                    )
                }

                val awayStats = groupMap.getOrPut(game.awayTeamId) {
                    MutableTeamStats(
                        teamId = game.awayTeamId,
                        teamName = awayTeamInfo?.name ?: game.awayTeamId,
                        flagUrl = awayTeamInfo?.flagCode ?: "",
                        group = group
                    )
                }

                // Actualizar stats
                homeStats.played++
                awayStats.played++

                homeStats.goalsFor += score.homeGoals
                homeStats.goalsAgainst += score.awayGoals

                awayStats.goalsFor += score.awayGoals
                awayStats.goalsAgainst += score.homeGoals

                when {
                    score.homeGoals > score.awayGoals -> {
                        homeStats.wins++
                        homeStats.points += 3
                        awayStats.losses++
                    }
                    score.homeGoals < score.awayGoals -> {
                        awayStats.wins++
                        awayStats.points += 3
                        homeStats.losses++
                    }
                    else -> {
                        homeStats.draws++
                        awayStats.draws++
                        homeStats.points += 1
                        awayStats.points += 1
                    }
                }
            }

        // Comparator básico: puntos, DG, GF
        val comparator = compareByDescending<GroupStandingUi> { it.points }
            .thenByDescending { it.goalDiff }
            .thenByDescending { it.goalsFor }

        // 3) Convertir a UI y ordenar dentro de cada grupo
        val rawStandingsByGroup: Map<String, List<GroupStandingUi>> =
            statsByGroup.mapValues { (_, groupMap) ->
                groupMap.values
                    .map { stats ->
                        GroupStandingUi(
                            teamId = stats.teamId,
                            teamName = stats.teamName,
                            flagUrl = stats.flagUrl,
                            group = stats.group,
                            played = stats.played,
                            wins = stats.wins,
                            draws = stats.draws,
                            losses = stats.losses,
                            goalsFor = stats.goalsFor,
                            goalsAgainst = stats.goalsAgainst,
                            goalDiff = stats.goalsFor - stats.goalsAgainst,
                            points = stats.points,
                            position = 0,
                            qualifiesAsTopTwo = false,
                            qualifiesAsBestThird = false
                        )
                    }
                    .sortedWith(comparator)
                    .mapIndexed { index, row ->
                        row.copy(position = index + 1)
                    }
            }

        // 4) Candidatos a mejores terceros (posición 3 de cada grupo)
        val thirdCandidates = rawStandingsByGroup.values
            .mapNotNull { it.getOrNull(2) } // index 2 -> posición 3
            .sortedWith(comparator)

        val bestThirdIds: Set<String> = thirdCandidates
            .take(8) // 8 mejores terceros
            .map { it.teamId }
            .toSet()

        // 5) Marcar clasificados (top 2 y mejores terceros)
        return rawStandingsByGroup.mapValues { (_, list) ->
            list.map { row ->
                row.copy(
                    qualifiesAsTopTwo = row.position <= 2,
                    qualifiesAsBestThird = bestThirdIds.contains(row.teamId)
                )
            }
        }
    }
}