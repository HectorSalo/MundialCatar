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
import com.skysam.hchirinos.mundialcatar.ui.commonView.GameSelectAdapter
import com.skysam.hchirinos.mundialcatar.ui.commonView.SelectGame
import com.skysam.hchirinos.mundialcatar.ui.commonView.WrapContentLinearLayoutManager
import java.util.*

class GamedayFragment : Fragment(), SelectGame {

    private var _binding: FragmentGamedayBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GamedayViewModel by activityViewModels()
    private val games = mutableListOf<Game>()
    private lateinit var gamedayAdapter: GamedayAdapter
    private lateinit var gameSelectAdapter: GameSelectAdapter
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
        gameSelectAdapter = GameSelectAdapter(games, this)
        wrapContentLinearLayoutManager = WrapContentLinearLayoutManager(requireContext(),
            RecyclerView.VERTICAL, false)
        binding.rvGames.apply {
            setHasFixedSize(true)
            adapter = gameSelectAdapter
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
                    val listTemp = mutableListOf<Game>()
                    listTemp.addAll(games)
                    games.clear()
                    for (game in it) {
                        if (Common.convertDateToString(game.date) == Common.convertDateToString(it[0].date)) games.add(game)
                    }
                    if (listTemp.isNotEmpty()) {
                        for (i in games) {
                            for (j in listTemp) {
                                if (i.id == j.id && (i.goalsTeam1 != j.goalsTeam1 || i.goalsTeam2 != j.goalsTeam2))
                                    gameSelectAdapter.notifyItemChanged(games.indexOf(i))
                            }
                        }
                    } else {
                        gameSelectAdapter.notifyItemRangeInserted(0, games.size)
                    }
                    if (Common.convertDateToString(calendar.time) == Common.convertDateToString(it[0].date))
                        binding.titleGameday.text = getString(R.string.title_gameday_yes)
                    else binding.titleGameday.text = getString(R.string.title_gameday_no)
                    binding.rvGames.visibility = View.VISIBLE
                } else {
                    binding.rvGames.visibility = View.GONE
                }
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun select(game: Game) {
        /*val ne = Game(
            game.id,
            game.team1,
            game.team2,
            game.flag1,
            game.flag2,
            game.date,
            game.stadium,
            5,3,
            0,0,
            game.round,
            game.number,
            game.gameTo,
            game.positionTo
        )
        viewModel.updateResultGame(ne)*/
    }
}