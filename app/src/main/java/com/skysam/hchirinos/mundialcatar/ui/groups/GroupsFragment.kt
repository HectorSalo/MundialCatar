package com.skysam.hchirinos.mundialcatar.ui.groups

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.chip.Chip
import com.skysam.hchirinos.mundialcatar.R
import com.skysam.hchirinos.mundialcatar.common.Common.formatRound
import com.skysam.hchirinos.mundialcatar.common.Constants
import com.skysam.hchirinos.mundialcatar.common.FlagsMapper
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
    private var currentGroupIndex: Int = 0
    private var currentGroupCode: String = "A"

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
        setupGroupChips()
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

    private fun setupGroupChips() {
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

        val chipGroup = binding.chipGroups
        chipGroup.removeAllViews()

        groups.forEachIndexed { index, label ->
            val chip = Chip(requireContext()).apply {
                text = label
                isCheckable = true
                isClickable = true
            }
            chipGroup.addView(chip)

            chip.setOnClickListener {
                viewModel.setIndex(index)   // seguimos usando el ViewModel como fuente de verdad
            }
        }

        // Seleccionar chip inicial según el índice actual del ViewModel
        val initialIndex = viewModel.index.value ?: 0
        (chipGroup.getChildAt(initialIndex) as? Chip)?.isChecked = true
        currentGroupIndex = initialIndex
        currentGroupCode = groupCodeFromIndex(initialIndex)
    }

    private fun loadViewModel() {
        viewModel.index.observe(viewLifecycleOwner) { idx ->
            if (_binding == null) return@observe
            currentGroupIndex = idx
            currentGroupCode = groupCodeFromIndex(idx)

            // Sincronizar selección visual de chips
            val chipGroup = binding.chipGroups
            if (idx in 0 until chipGroup.childCount) {
                (chipGroup.getChildAt(idx) as? Chip)?.isChecked = true
            }
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

        viewModel.standings.observe(viewLifecycleOwner) { map ->
            if (_binding == null) return@observe
            standingsByGroup = map ?: emptyMap()
            showGroup()
        }
    }

    private fun groupCodeFromIndex(index: Int): String {
        return when (index) {
            0 -> "A"
            1 -> "B"
            2 -> "C"
            3 -> "D"
            4 -> "E"
            5 -> "F"
            6 -> "G"
            7 -> "H"
            8 -> "I"
            9 -> "J"
            10 -> "K"
            11 -> "L"
            else -> "A"
        }
    }

    private fun showGroup() {
        if (_binding == null) return

        // 1) Tabla de posiciones
        val groupStandings = standingsByGroup[currentGroupCode].orEmpty()

        // 2) Partidos del grupo (GameToView)
        val gamesToView = buildGamesToViewForGroup(
            games = games,
            teams = teams,
            groupCode = currentGroupCode
        )

        // 3) Mostrar vistas
        if (gamesToView.isNotEmpty() && groupStandings.isNotEmpty()) {
            groupsAdapter.updateList(groupStandings)
            gamedayAdapter.updateList(gamesToView)
            binding.cardStandings.visibility = View.VISIBLE
            binding.rvGroup.visibility = View.VISIBLE
            binding.tvGamesTitle.visibility = View.VISIBLE
            binding.horizontalScrollView.visibility = View.VISIBLE
            binding.tvStandingsTitle.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
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
                    flag1Res = FlagsMapper.from(homeTeam?.flagCode),
                    flag2Res = FlagsMapper.from(awayTeam?.flagCode),
                    date = game.date,
                    homeGoals = score?.homeGoals ?: 0,
                    awayGoals = score?.awayGoals ?: 0,
                    round = formatRound(game),
                    number = game.matchNumber,
                    points = 0,            // aquí luego puedes inyectar puntos de predicción
                    hasPrediction = false, // idem
                    gameId = game.id,
                    tournamentId = game.tournamentId,
                    stadiumName = game.venue.name,
                    stadiumCity = game.venue.location
                )
            }
    }
}