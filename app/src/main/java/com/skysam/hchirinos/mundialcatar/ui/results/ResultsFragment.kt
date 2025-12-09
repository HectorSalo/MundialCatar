package com.skysam.hchirinos.mundialcatar.ui.results

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.skysam.hchirinos.mundialcatar.common.Constants
import com.skysam.hchirinos.mundialcatar.databinding.FragmentResultsBinding
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.GameToView
import com.skysam.hchirinos.mundialcatar.dataclass.MatchStage
import com.skysam.hchirinos.mundialcatar.dataclass.Team
import com.skysam.hchirinos.mundialcatar.ui.gameday.GamedayAdapter

class ResultsFragment : Fragment() {

    private var _binding: FragmentResultsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ResultsViewModel by activityViewModels()
    private var games: List<Game> = emptyList()
    private var teams: List<Team> = emptyList()
    private lateinit var gamedayAdapter: GamedayAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gamedayAdapter = GamedayAdapter(canEdit = false) {

        }
        binding.rvGames.apply {
            setHasFixedSize(true)
            adapter = gamedayAdapter
        }
        loadViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadViewModel() {
        viewModel.games.observe(viewLifecycleOwner) { newGames ->
            if (_binding == null) return@observe
            games = newGames
            renderResults()
        }

        viewModel.teams.observe(viewLifecycleOwner) { newTeams ->
            if (_binding == null) return@observe
            teams = newTeams
            renderResults()
        }
    }

    private fun renderResults() {
        if (_binding == null) return

        binding.progressBar.visibility = View.VISIBLE

        if (games.isEmpty()) {
            binding.rvGames.visibility = View.GONE
            binding.listEmpty.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
            return
        }

        if (teams.isEmpty()) {
            // Aún no llegan los equipos; esperamos a que se dispare el observer de teams
            return
        }

        val teamsById = teams.associateBy { it.id }

        val gamesToView = games.map { game ->
            val home = teamsById[game.homeTeamId]
            val away = teamsById[game.awayTeamId]

            val homeName = home?.shortName ?: ""
            val awayName = away?.shortName ?: ""

            val flag1 = home?.flagCode ?: ""
            val flag2 = away?.flagCode ?: ""

            GameToView(
                homeTeamName = homeName,
                awayTeamName = awayName,
                flag1 = flag1,
                flag2 = flag2,
                date = game.date,
                homeGoals = game.score?.homeGoals ?: 0,
                awayGoals = game.score?.awayGoals ?: 0,
                round = formatRound(game),
                number = game.matchNumber,
                points = 0,              // aquí solo mostramos resultado real, no puntos de predicción
                hasPrediction = false    // en esta vista no aplica
            )
        }

        gamedayAdapter.updateList(gamesToView)

        binding.rvGames.visibility = View.VISIBLE
        binding.listEmpty.visibility = if (gamesToView.isEmpty()) View.VISIBLE else View.GONE
        binding.progressBar.visibility = View.GONE
    }

    private fun formatRound(game: Game): String =
        when (game.stage) {
            MatchStage.GROUP -> when (game.group) {
                "A" -> Constants.GROUP_A
                "B" -> Constants.GROUP_B
                "C" -> Constants.GROUP_C
                "D" -> Constants.GROUP_D
                "E" -> Constants.GROUP_E
                "F" -> Constants.GROUP_F
                "G" -> Constants.GROUP_G
                "H" -> Constants.GROUP_H
                "I" -> Constants.GROUP_I
                "J" -> Constants.GROUP_J
                "K" -> Constants.GROUP_K
                "L" -> Constants.GROUP_L
                else -> "Fase de grupos"
            }

            MatchStage.ROUND_OF_32 -> Constants.ROUND_OF_32
            MatchStage.ROUND_OF_16 -> Constants.ROUND_OF_16
            MatchStage.QUARTER_FINAL -> Constants.ROUND_OF_8
            MatchStage.SEMI_FINAL -> Constants.SEMIFINAL
            MatchStage.THIRD_PLACE -> Constants.THIRD_PLACE
            MatchStage.FINAL -> Constants.FINAL
        }
}