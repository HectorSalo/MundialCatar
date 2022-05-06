package com.skysam.hchirinos.mundialcatar.ui.results

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.skysam.hchirinos.mundialcatar.databinding.FragmentResultsBinding
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.ui.gameday.GamedayAdapter
import com.skysam.hchirinos.mundialcatar.ui.gameday.GamedayViewModel

class ResultsFragment : Fragment() {

    private var _binding: FragmentResultsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GamedayViewModel by activityViewModels()
    private val games = mutableListOf<Game>()
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
        gamedayAdapter = GamedayAdapter(games)
        binding.rvGames.apply {
            setHasFixedSize(true)
            adapter = gamedayAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}