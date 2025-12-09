package com.skysam.hchirinos.mundialcatar.ui.groups

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.skysam.hchirinos.mundialcatar.common.Constants
import com.skysam.hchirinos.mundialcatar.databinding.FragmentGroupsBinding
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.GameToView
import com.skysam.hchirinos.mundialcatar.dataclass.GroupStandingUi
import com.skysam.hchirinos.mundialcatar.dataclass.MatchStage
import com.skysam.hchirinos.mundialcatar.dataclass.Team
import com.skysam.hchirinos.mundialcatar.ui.gameday.GamedayAdapter

class GroupsFragment : Fragment() {

    private val viewModel: GroupsViewModel by activityViewModels()
    private var _binding: FragmentGroupsBinding? = null
    private val binding get() = _binding!!
    private lateinit var groupsAdapter: GroupsAdapter
    private lateinit var gamedayAdapter: GamedayAdapter
    private var teams: List<Team> = emptyList()
    private var games: List<Game> = emptyList()
    private var standingsByGroup: Map<String, List<GroupStandingUi>> = emptyMap()
    private var currentGroup: String = "A"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroupsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        groupsAdapter = GroupsAdapter()
        gamedayAdapter = GamedayAdapter(false) {}
        binding.rvGroup.apply {
            setHasFixedSize(true)
            adapter = groupsAdapter
        }
        binding.rvGames.apply {
            setHasFixedSize(true)
            adapter = gamedayAdapter
        }
        loadViewModel()
    }

    companion object {
        @JvmStatic
        fun newInstance(): GroupsFragment {
            return GroupsFragment()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadViewModel() {
        viewModel.index.observe(viewLifecycleOwner) { idx ->
            if (_binding == null) return@observe
            currentGroup = groupCodeFromIndex(idx)
            showGroup()
        }
        viewModel.standings.observe(viewLifecycleOwner) { map ->
            if (_binding == null) return@observe

            standingsByGroup = map ?: emptyMap()
            showGroup()
        }
        viewModel.games.observe(viewLifecycleOwner) { list ->
            if (_binding == null) return@observe
            games = list
            showGroup()
        }
        viewModel.teams.observe(viewLifecycleOwner) { list ->
            if (_binding == null) return@observe
            teams = list
            showGroup()
        }
    }

    private fun groupCodeFromIndex(index: Int): String {
        // Ya pensando en 12 grupos (A..L)
        val groups = listOf(
            Constants.GROUP_A,
            Constants.GROUP_B,
            Constants.GROUP_C,
            Constants.GROUP_D,
            Constants.GROUP_E,
            Constants.GROUP_F,
            Constants.GROUP_G,
            Constants.GROUP_H,
            Constants.GROUP_I,
            Constants.GROUP_J,
            Constants.GROUP_K,
            Constants.GROUP_L
        )
        return groups.getOrElse(index) { Constants.GROUP_A }
    }

    private fun showGroup() {
        if (_binding == null) return

        // 1) Tabla de posiciones
        val groupStandings = standingsByGroup[currentGroup].orEmpty()

        // Insertamos fila de header en posición 0
        val header = GroupStandingUi(
            teamId = "",
            teamName = "",
            flagUrl = "",
            group = currentGroup,
            played = 0,
            wins = 0,
            draws = 0,
            losses = 0,
            goalsFor = 0,
            goalsAgainst = 0,
            goalDiff = 0,
            points = 0,
            position = 0,
            qualifiesAsTopTwo = false,
            qualifiesAsBestThird = false
        )

        val listForAdapter = listOf(header) + groupStandings
        groupsAdapter.updateList(listForAdapter)

        // 2) Partidos del grupo (GameToView)
        val gamesToView = buildGamesToViewForGroup(
            games = games,
            teams = teams,
            groupCode = currentGroup
        )
        gamedayAdapter.updateList(gamesToView)

        // 3) Mostrar vistas
        binding.rvGames.visibility = View.VISIBLE
        binding.rvGroup.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
    }

    private fun buildGamesToViewForGroup(
        games: List<Game>,
        teams: List<Team>,
        groupCode: String
    ): List<GameToView> {
        val teamsById = teams.associateBy { it.id }

        return games
            .filter { it.stage == MatchStage.GROUP && it.group == groupCode }
            .sortedBy { it.matchNumber }
            .map { game ->
                val homeTeam = teamsById[game.homeTeamId]
                val awayTeam = teamsById[game.awayTeamId]
                val score = game.score

                GameToView(
                    homeTeamName = homeTeam?.name ?: game.homeTeamId,
                    awayTeamName = awayTeam?.name ?: game.awayTeamId,
                    flag1 = homeTeam?.flagCode ?: "",
                    flag2 = awayTeam?.flagCode ?: "",
                    date = game.date,
                    homeGoals = score?.homeGoals ?: 0,
                    awayGoals = score?.awayGoals ?: 0,
                    round = game.group ?: "",
                    number = game.matchNumber,
                    points = 0,            // aquí luego puedes inyectar puntos de predicción
                    hasPrediction = false, // idem
                    gameId = game.id,
                    tournamentId = game.tournamentId
                )
            }
    }
}