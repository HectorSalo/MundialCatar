package com.skysam.hchirinos.mundialcatar.ui.predicts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import com.skysam.hchirinos.mundialcatar.databinding.FragmentPredictsBinding
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.GameToView
import com.skysam.hchirinos.mundialcatar.dataclass.GameUser
import com.skysam.hchirinos.mundialcatar.dataclass.Team
import com.skysam.hchirinos.mundialcatar.ui.commonView.EditResultsDialog
import com.skysam.hchirinos.mundialcatar.ui.commonView.SelectGame
import java.util.Calendar

class PredictsFragment : Fragment(), SelectGame {
    private var _binding: FragmentPredictsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PredictsViewModel by activityViewModels()
    private lateinit var predictsAdapter: PredictsAdapter
    private var games = listOf<Game>()
    private var gamesUser = listOf<GameUser>()
    private var teams = listOf<Team>()
    private var moveList = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPredictsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        predictsAdapter = PredictsAdapter(this)
        binding.rvGames.apply {
            setHasFixedSize(true)
            adapter = predictsAdapter
        }

        loadViewModel()
    }

    private fun loadViewModel() {
        viewModel.gamesUser.observe(viewLifecycleOwner) {
            if (_binding != null) {
                gamesUser = it
                joinData()
            }
        }
        viewModel.games.observe(viewLifecycleOwner) {
            if (_binding != null) {
                games = it
                joinData()
            }
        }
        viewModel.teams.observe(viewLifecycleOwner) {
            if (_binding != null) {
                teams = it
                joinData()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun joinData() {
        if (games.isEmpty() || teams.isEmpty()) return

        val gamesToView = mutableListOf<GameToView>()
        games.forEach { game ->
            var flag1 = ""
            var flag2 = ""
            var goals1 = 0
            var goals2 = 0
            var points = 0

            for (team in teams) {
                if (team.id == game.team1) flag1 = team.flag
                if (team.id == game.team2) flag2 = team.flag
            }

            if (gamesUser.isNotEmpty()) {
                for (gm in gamesUser) {
                    if (gm.number == game.number) {
                        goals1 = gm.goals1
                        goals2 = gm.goals2
                        points = gm.points
                        break
                    }
                }
            }

            val newGameToView = GameToView(
                game.team1,
                game.team2,
                flag1,
                flag2,
                game.date,
                goals1,
                goals2,
                game.round,
                game.number,
                points
            )
            gamesToView.add(newGameToView)
        }
        fillData(gamesToView)
    }

    private fun fillData(gamesToView: List<GameToView>) {
        predictsAdapter.updateList(gamesToView)
        binding.rvGames.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        if (moveList) {
            for (game in gamesToView) {
                if (game.date.after(calendar.time)) {
                    binding.rvGames.scrollToPosition(gamesToView.indexOf(game))
                    moveList = false
                    break
                }
            }
        }
    }

    override fun updatePredict(gameToView: GameToView) {
        var star = false
        for (game in games) {
            if (game.number == gameToView.number) {
                star = game.started
                break
            }
        }
        val calendarCurrent = Calendar.getInstance()
        val calendar = Calendar.getInstance()
        calendar.time = gameToView.date
        calendar.add(Calendar.MINUTE, -10)

        if (calendarCurrent.time.before(calendar.time) && !star) {
            viewModel.editPredict(gameToView)
            val editResultsDialog = EditResultsDialog(false)
            editResultsDialog.show(requireActivity().supportFragmentManager, tag)
        } else {
            Snackbar.make(binding.coordinator, "Juego iniciado. No puede crear predicción", Snackbar.LENGTH_SHORT).show()
        }
    }
}