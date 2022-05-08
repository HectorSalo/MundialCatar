package com.skysam.hchirinos.mundialcatar.ui.gameday

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.skysam.hchirinos.mundialcatar.R
import com.skysam.hchirinos.mundialcatar.common.Common
import com.skysam.hchirinos.mundialcatar.databinding.FragmentGamedayBinding
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.ui.commonView.WrapContentLinearLayoutManager
import java.util.*

class GamedayFragment : Fragment() {

    private var _binding: FragmentGamedayBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GamedayViewModel by activityViewModels()
    private val games = mutableListOf<Game>()
    private lateinit var gamedayAdapter: GamedayAdapter
    private lateinit var wrapContentLinearLayoutManager: WrapContentLinearLayoutManager
    private lateinit var calendar: Calendar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGamedayBinding.inflate(inflater, container, false)
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
        calendar = Calendar.getInstance()
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
                    for (game in it) {
                        if (Common.convertDateToString(game.date) == Common.convertDateToString(it[0].date)) games.add(game)
                    }
                    if (Common.convertDateToString(calendar.time) == Common.convertDateToString(it[0].date))
                        binding.titleGameday.text = getString(R.string.title_gameday_yes)
                    else binding.titleGameday.text = getString(R.string.title_gameday_no)
                    gamedayAdapter.notifyItemRangeInserted(0, games.size)
                    binding.rvGames.visibility = View.VISIBLE
                } else {
                    binding.rvGames.visibility = View.GONE
                }
                binding.progressBar.visibility = View.GONE
            }
        }
    }
}