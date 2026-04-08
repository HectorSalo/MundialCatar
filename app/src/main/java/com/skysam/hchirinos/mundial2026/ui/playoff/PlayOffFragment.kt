package com.skysam.hchirinos.mundial2026.ui.playoff

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.chip.Chip
import com.skysam.hchirinos.mundial2026.common.Common
import com.skysam.hchirinos.mundial2026.common.Constants
import com.skysam.hchirinos.mundial2026.common.FlagsMapper
import com.skysam.hchirinos.mundial2026.databinding.FragmentPlayOffBinding
import com.skysam.hchirinos.mundial2026.dataclass.Game
import com.skysam.hchirinos.mundial2026.dataclass.GameToView
import com.skysam.hchirinos.mundial2026.dataclass.MatchStage
import com.skysam.hchirinos.mundial2026.dataclass.Team
import com.skysam.hchirinos.mundial2026.ui.gameday.GamedayAdapter

class PlayOffFragment : Fragment() {

    private val viewModel: PlayOffViewModel by activityViewModels()
    private var _binding: FragmentPlayOffBinding? = null
    private val binding get() = _binding!!
    private lateinit var gamedayAdapter: GamedayAdapter
    private var games: List<Game> = emptyList()
    private var teams: List<Team> = emptyList()
    private var currentStageIndex: Int = 0
    private var currentStage: MatchStage = MatchStage.ROUND_OF_32

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayOffBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gamedayAdapter = GamedayAdapter(false){}
        binding.rvGames.apply {
            setHasFixedSize(true)
            adapter = gamedayAdapter
        }
        setupStageChips()
        loadViewModel()
    }

    companion object {
        @JvmStatic
        fun newInstance(): PlayOffFragment {
            return PlayOffFragment()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupStageChips() {
        // Ajusta el orden o textos si en tu torneo no existe Round of 32 o Third Place
        val stages = listOf(
            Constants.ROUND_OF_32,
            Constants.ROUND_OF_16,
            Constants.ROUND_OF_8,
            Constants.SEMIFINAL,
            Constants.THIRD_PLACE,
            Constants.FINAL
        )

        val chipGroup = binding.chipStages
        chipGroup.removeAllViews()

        stages.forEachIndexed { index, stage ->
            val chip = Chip(requireContext()).apply {
                text = stage
                isCheckable = true
                isClickable = true
            }
            chipGroup.addView(chip)

            chip.setOnClickListener {
                viewModel.setIndex(index) // ViewModel como fuente de verdad
            }
        }

        // Selección inicial desde VM (o 0)
        val initialIndex = viewModel.index.value ?: 0
        if (initialIndex in 0 until chipGroup.childCount) {
            (chipGroup.getChildAt(initialIndex) as? Chip)?.isChecked = true
        }
        currentStageIndex = initialIndex
        currentStage = stageFromIndex(initialIndex)
    }

    private fun loadViewModel() {
        viewModel.index.observe(viewLifecycleOwner) { idx ->
            if (_binding == null) return@observe

            currentStageIndex = idx
            currentStage = stageFromIndex(idx)

            // Sincronizar selección visual
            val chipGroup = binding.chipStages
            if (idx in 0 until chipGroup.childCount) {
                (chipGroup.getChildAt(idx) as? Chip)?.isChecked = true
            }

            showStage()
        }

        viewModel.games.observe(viewLifecycleOwner) { list ->
            if (_binding == null) return@observe
            games = list
            showStage()
        }

        viewModel.teams.observe(viewLifecycleOwner) { list ->
            if (_binding == null) return@observe
            teams = list
            showStage()
        }
    }

    private fun stageFromIndex(index: Int): MatchStage =
        when (index) {
            0 -> MatchStage.ROUND_OF_32
            1 -> MatchStage.ROUND_OF_16
            2 -> MatchStage.QUARTER_FINAL
            3 -> MatchStage.SEMI_FINAL
            4 -> MatchStage.THIRD_PLACE
            5 -> MatchStage.FINAL
            else -> MatchStage.ROUND_OF_32
        }

    private fun showStage() {
        if (_binding == null) return

        val teamsById = teams.associateBy { it.id }

        val gamesToView = games
            .filter { it.stage == currentStage }
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
                    round = Common.formatRound(game),       // etiqueta, luego la puedes mapear a string de recursos
                    number = game.matchNumber,
                    points = 0,                      // puntos de predicción si quieres mezclarlos
                    hasPrediction = false,           // idem
                    gameId = game.id,
                    tournamentId = game.tournamentId,
                    stadiumName = game.venue.name,
                    stadiumCity = game.venue.location
                )
            }

        binding.progressBar.visibility = View.GONE
        if (gamesToView.isNotEmpty()) {
            binding.rvGames.visibility = View.VISIBLE
            gamedayAdapter.updateList(gamesToView)
            binding.tvListEmpty.visibility = View.GONE
        } else {
            binding.tvListEmpty.visibility = View.VISIBLE
            binding.rvGames.visibility = View.GONE
        }
    }
}