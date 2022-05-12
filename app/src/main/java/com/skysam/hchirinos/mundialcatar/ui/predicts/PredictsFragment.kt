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
import com.skysam.hchirinos.mundialcatar.ui.commonView.EditResultsDialog
import com.skysam.hchirinos.mundialcatar.ui.commonView.SelectGame
import java.util.*

class PredictsFragment : Fragment(), SelectGame {
    private var _binding: FragmentPredictsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PredictsViewModel by activityViewModels()
    private lateinit var predictsAdapter: PredictsAdapter
    private val games = mutableListOf<GameUser>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPredictsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        predictsAdapter = PredictsAdapter(games, this)
        binding.rvGames.apply {
            setHasFixedSize(true)
            adapter = predictsAdapter
        }

        loadViewModel()
    }

    private fun loadViewModel() {
        viewModel.games.observe(viewLifecycleOwner) {
            if (_binding != null) {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.MONTH, 10)
                calendar.set(Calendar.DAY_OF_MONTH, 24)
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                var position = 0
                games.clear()
                games.addAll(it)
                for (game in games) {
                    if (game.date.after(calendar.time)) {
                        position = games.indexOf(game)
                        break
                    }
                }
                binding.progressBar.visibility = View.GONE
                binding.rvGames.visibility = View.VISIBLE
                predictsAdapter.notifyDataSetChanged()
                binding.rvGames.scrollToPosition(position)
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
        if (gameUser.team1.isEmpty() || gameUser.team2.isEmpty()) {
            Snackbar.make(binding.coordinator, "Falta equipos por definirse", Snackbar.LENGTH_SHORT).show()
            return
        }
        viewModel.updatePredict(gameUser)
        val editResultsDialog = EditResultsDialog()
        editResultsDialog.show(requireActivity().supportFragmentManager, tag)
    }
}