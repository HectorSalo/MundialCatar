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
import com.skysam.hchirinos.mundialcatar.dataclass.Team
import com.skysam.hchirinos.mundialcatar.ui.commonView.EditResultsDialog
import com.skysam.hchirinos.mundialcatar.ui.commonView.SelectGame
import java.util.*

class PredictsFragment : Fragment(), SelectGame {
    private var _binding: FragmentPredictsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PredictsViewModel by activityViewModels()
    private lateinit var predictsAdapter: PredictsAdapter
    private val gamesUser = mutableListOf<GameUser>()
    private val games = mutableListOf<Game>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPredictsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        predictsAdapter = PredictsAdapter(gamesUser, this)
        binding.rvGames.apply {
            setHasFixedSize(true)
            adapter = predictsAdapter
        }

        loadViewModel()
    }

    private fun loadViewModel() {
        viewModel.gamesUser.observe(viewLifecycleOwner) {
            if (_binding != null) {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                var position = 0
                gamesUser.clear()
                gamesUser.addAll(it)
                for (game in gamesUser) {
                    if (game.date.after(calendar.time)) {
                        position = gamesUser.indexOf(game)
                        break
                    }
                }
                binding.progressBar.visibility = View.GONE
                binding.rvGames.visibility = View.VISIBLE
                predictsAdapter.notifyDataSetChanged()
                binding.rvGames.scrollToPosition(position)
            }
        }

        viewModel.games.observe(viewLifecycleOwner) {
            if (_binding != null) {
                games.clear()
                games.addAll(it)
            }
        }

        viewModel.teams.observe(viewLifecycleOwner) {
            if (_binding != null) {
                val list = mutableListOf<Team>()
                list.addAll(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun updateResult(game: Game) {

    }

    override fun updatePredict(gameUser: GameUser) {
        var star = false
        for (game in games) {
            if (game.id == gameUser.id) star = game.started
        }
        val calendarCurrent = Calendar.getInstance()
        val calendar = Calendar.getInstance()
        calendar.time = gameUser.date
        calendar.add(Calendar.MINUTE, -10)

        if (calendarCurrent.time.before(calendar.time) && !star) {
            viewModel.updatePredict(gameUser)
            val editResultsDialog = EditResultsDialog()
            editResultsDialog.show(requireActivity().supportFragmentManager, tag)
        } else {
            Snackbar.make(binding.coordinator, "Juego iniciado. No puede crear predicción", Snackbar.LENGTH_SHORT).show()
        }
    }
}