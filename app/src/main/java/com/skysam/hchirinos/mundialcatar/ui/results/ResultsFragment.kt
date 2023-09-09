package com.skysam.hchirinos.mundialcatar.ui.results

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.skysam.hchirinos.mundialcatar.databinding.FragmentResultsBinding
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.GameToView
import com.skysam.hchirinos.mundialcatar.ui.gameday.GamedayAdapter
import com.skysam.hchirinos.mundialcatar.ui.gameday.OnClick

class ResultsFragment : Fragment(), OnClick {

    private var _binding: FragmentResultsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ResultsViewModel by activityViewModels()
    private var games = listOf<Game>()
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
        gamedayAdapter = GamedayAdapter(this)
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
        viewModel.games.observe(viewLifecycleOwner) {
            if (_binding != null) {
                if (it.isNotEmpty()) {
                    games = it
                    viewModel.teams.observe(viewLifecycleOwner) {teams ->
                        val gamesToView = mutableListOf<GameToView>()
                        games.forEach { game ->
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
                                game.round,
                                game.number
                            )
                            gamesToView.add(newGameToView)
                            if (games.last() == game) gamedayAdapter.updateList(gamesToView)
                        }

                    }
                    binding.rvGames.visibility = View.VISIBLE
                    binding.listEmpty.visibility = View.GONE
                } else {
                    binding.rvGames.visibility = View.GONE
                    binding.listEmpty.visibility = View.VISIBLE
                }
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun select(game: GameToView) {

    }
}