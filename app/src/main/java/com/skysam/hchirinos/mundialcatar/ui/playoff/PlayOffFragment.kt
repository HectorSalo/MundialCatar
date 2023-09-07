package com.skysam.hchirinos.mundialcatar.ui.playoff

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.skysam.hchirinos.mundialcatar.common.Constants
import com.skysam.hchirinos.mundialcatar.databinding.FragmentPlayOffBinding
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.GameToView
import com.skysam.hchirinos.mundialcatar.dataclass.Team
import com.skysam.hchirinos.mundialcatar.ui.gameday.GamedayAdapter

class PlayOffFragment : Fragment() {

    private val viewModel: PlayOffViewModel by activityViewModels()
    private var _binding: FragmentPlayOffBinding? = null
    private val binding get() = _binding!!
    private lateinit var gamedayAdapter: GamedayAdapter
    private var games = listOf<Game>()
    private var teams = listOf<Team>()
    private var round: String = Constants.OCTAVOS

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayOffBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gamedayAdapter = GamedayAdapter()
        binding.rvGames.apply {
            setHasFixedSize(true)
            adapter = gamedayAdapter
        }
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

    private fun loadViewModel() {
        viewModel.index.observe(viewLifecycleOwner) {
            if (_binding != null) {
                round = when(it) {
                    0 -> Constants.OCTAVOS
                    1 -> Constants.CUARTOS
                    2 -> Constants.SEMIFINAL
                    3 -> Constants.TERCER_LUGAR
                    4 -> Constants.FINAL
                    else -> Constants.OCTAVOS
                }
                if (games.isEmpty()) {
                    viewModel.games.observe(viewLifecycleOwner) {gamesFrom ->
                        games = gamesFrom
                        if (teams.isEmpty()) {
                            viewModel.teams.observe(viewLifecycleOwner) { teamsFrom->
                                teams = teamsFrom
                                showRound()
                            }
                        } else showRound()
                    }
                } else showRound()
            }
        }
    }

    private fun showRound() {
        val gamesToView = mutableListOf<GameToView>()
        for (game in games) {
            if (game.round == round) {
                var flag1 = ""
                var flag2 = ""

                for (team in teams) {
                    if (team.id == game.team1) flag1 = team.flag
                    if (team.id == game.team2) flag2 = team.flag
                }

                val newGameToView = GameToView(
                    game.team1,
                    game.team2,
                    flag1,
                    flag2,
                    game.date,
                    game.goalsTeam1,
                    game.goalsTeam2,
                    game.round
                )
                gamesToView.add(newGameToView)
            }
        }

        gamedayAdapter.updateList(gamesToView)
        binding.rvGames.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
    }
}