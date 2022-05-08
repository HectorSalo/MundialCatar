package com.skysam.hchirinos.mundialcatar.ui.results

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.skysam.hchirinos.mundialcatar.databinding.FragmentResultsBinding
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.ui.commonView.WrapContentLinearLayoutManager
import com.skysam.hchirinos.mundialcatar.ui.gameday.GamedayAdapter

class ResultsFragment : Fragment() {

    private var _binding: FragmentResultsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ResultsViewModel by activityViewModels()
    private val games = mutableListOf<Game>()
    private lateinit var gamedayAdapter: GamedayAdapter
    private lateinit var wrapContentLinearLayoutManager: WrapContentLinearLayoutManager

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
        wrapContentLinearLayoutManager = WrapContentLinearLayoutManager(requireContext(),
            RecyclerView.VERTICAL, false)
        binding.rvGames.apply {
            setHasFixedSize(true)
            adapter = gamedayAdapter
            layoutManager = wrapContentLinearLayoutManager
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
                    games.clear()
                    games.addAll(it)
                    gamedayAdapter.notifyItemRangeInserted(0, games.size)
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
}