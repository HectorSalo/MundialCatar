package com.skysam.hchirinos.mundialcatar.ui.playoff

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.skysam.hchirinos.mundialcatar.common.FlagsMapper
import com.skysam.hchirinos.mundialcatar.databinding.FragmentPlayOffBinding
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.GameToView
import com.skysam.hchirinos.mundialcatar.dataclass.MatchStage
import com.skysam.hchirinos.mundialcatar.dataclass.Team
import com.skysam.hchirinos.mundialcatar.ui.gameday.GamedayAdapter

class PlayOffFragment : Fragment() {

    private val viewModel: PlayOffViewModel by activityViewModels()
    private var _binding: FragmentPlayOffBinding? = null
    private val binding get() = _binding!!
    private lateinit var gamedayAdapter: GamedayAdapter
    private var games: List<Game> = emptyList()
    private var teams: List<Team> = emptyList()
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
        loadViewModel()
    }

    companion object {
        @JvmStatic
        fun newInstance(): PlayOffFragment = PlayOffFragment()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadViewModel() {
        viewModel.index.observe(viewLifecycleOwner) { idx ->
            if (_binding == null) return@observe
            currentStage = stageFromIndex(idx)
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
            4 -> MatchStage.FINAL
            // Si tienes tercer puesto en otra pestaña:
            5 -> MatchStage.THIRD_PLACE
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
                    round = currentStage.name,       // etiqueta, luego la puedes mapear a string de recursos
                    number = game.matchNumber,
                    points = 0,                      // puntos de predicción si quieres mezclarlos
                    hasPrediction = false,           // idem
                    gameId = game.id,
                    tournamentId = game.tournamentId,
                    stadiumName = game.venue.name,
                    stadiumCity = game.venue.location
                )
            }

        gamedayAdapter.updateList(gamesToView)
        binding.rvGames.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
    }
}