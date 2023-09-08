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
import com.skysam.hchirinos.mundialcatar.dataclass.GameUser
import com.skysam.hchirinos.mundialcatar.repositories.Auth
import com.skysam.hchirinos.mundialcatar.ui.commonView.EditResultsDialog
import com.skysam.hchirinos.mundialcatar.ui.commonView.SelectGame
import java.util.Calendar

class PredictsFragment : Fragment(), SelectGame {
    private var _binding: FragmentPredictsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PredictsViewModel by activityViewModels()
    private lateinit var predictsAdapter: PredictsAdapter
    private var games = listOf<Game>()

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
        viewModel.getGamesByUser(Auth.getCurrenUser()!!.uid).observe(viewLifecycleOwner) {
            if (_binding != null) {
                if (it.isNotEmpty()) {
                    viewModel.teams.observe(viewLifecycleOwner) {teams ->
                        val gamesToView = mutableListOf<GameUser>()
                        it.forEach { game ->
                            var flag1 = ""
                            var flag2 = ""

                            for (team in teams) {
                                if (team.id == game.team1) flag1 = team.flag
                                if (team.id == game.team2) flag2 = team.flag
                            }

                            val newGameToView = GameUser(
                                game.team1,
                                game.team2,
                                game.date,
                                game.goals1,
                                game.goals2,
                                flag1,
                                flag2,
                                game.round,
                                game.number,
                                game.points,
                                game.predict
                            )
                            gamesToView.add(newGameToView)
                        }
                        predictsAdapter.updateList(gamesToView)
                        binding.rvGames.visibility = View.VISIBLE
                        val calendar = Calendar.getInstance()
                        calendar.set(Calendar.HOUR_OF_DAY, 0)
                        calendar.set(Calendar.MINUTE, 0)
                        calendar.set(Calendar.SECOND, 0)
                        for (game in gamesToView) {
                            if (game.date.after(calendar.time)) {
                                binding.rvGames.scrollToPosition(gamesToView.indexOf(game))
                                break
                            }
                        }
                    }
                } else {
                    binding.rvGames.visibility = View.GONE
                }
                binding.progressBar.visibility = View.GONE
            }
        }
        viewModel.games.observe(viewLifecycleOwner) {
            if (_binding != null) {
                games = it
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun updatePredict(gameUser: GameUser) {
        var star = false
        for (game in games) {
            if (game.number == gameUser.number) {
                star = game.started
                break
            }
        }
        val calendarCurrent = Calendar.getInstance()
        val calendar = Calendar.getInstance()
        calendar.time = gameUser.date
        calendar.add(Calendar.MINUTE, -10)

        if (calendarCurrent.time.before(calendar.time) && !star) {
            if (gameUser.team1.isEmpty() || gameUser.team2.isEmpty()) {
                Snackbar.make(binding.coordinator, "Juego no definido. No puede crear predicción", Snackbar.LENGTH_SHORT).show()
                return
            }

            viewModel.editPredict(gameUser)
            val editResultsDialog = EditResultsDialog()
            editResultsDialog.show(requireActivity().supportFragmentManager, tag)
        } else {
            Snackbar.make(binding.coordinator, "Juego iniciado. No puede crear predicción", Snackbar.LENGTH_SHORT).show()
        }
    }
}