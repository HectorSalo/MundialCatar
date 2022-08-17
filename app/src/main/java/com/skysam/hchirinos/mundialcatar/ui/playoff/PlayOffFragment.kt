package com.skysam.hchirinos.mundialcatar.ui.playoff

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.skysam.hchirinos.mundialcatar.common.Constants
import com.skysam.hchirinos.mundialcatar.databinding.FragmentPlayOffBinding
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.Team
import com.skysam.hchirinos.mundialcatar.ui.commonView.WrapContentLinearLayoutManager
import com.skysam.hchirinos.mundialcatar.ui.gameday.GamedayAdapter

class PlayOffFragment : Fragment() {

    private val viewModel: PlayOffViewModel by activityViewModels()
    private var _binding: FragmentPlayOffBinding? = null
    private val binding get() = _binding!!
    private lateinit var wrapContentLinearLayoutManager: WrapContentLinearLayoutManager
    private lateinit var gamedayAdapter: GamedayAdapter
    private val games = mutableListOf<Game>()
    private val gamesByRound = mutableListOf<Game>()
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
        wrapContentLinearLayoutManager = WrapContentLinearLayoutManager(requireContext(),
            RecyclerView.VERTICAL, false)
        gamedayAdapter = GamedayAdapter(gamesByRound)
        binding.rvGames.apply {
            setHasFixedSize(true)
            adapter = gamedayAdapter
            layoutManager = wrapContentLinearLayoutManager
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
                showRound()
            }
        }

        viewModel.games.observe(viewLifecycleOwner) {
            if (_binding != null) {
                games.clear()
                games.addAll(it)
                showRound()
            }
        }

        viewModel.teams.observe(viewLifecycleOwner) {
            if (_binding != null) {
                val teams = mutableListOf<Team>()
                teams.addAll(it)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showRound() {
        gamesByRound.clear()
        for (game in games) {
            if (game.round == round) gamesByRound.add(game)
        }

        binding.rvGames.visibility = View.VISIBLE
        gamedayAdapter.notifyDataSetChanged()
        binding.progressBar.visibility = View.GONE
    }
}