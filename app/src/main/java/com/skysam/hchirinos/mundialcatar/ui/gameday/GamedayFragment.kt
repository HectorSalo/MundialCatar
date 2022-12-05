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
import com.skysam.hchirinos.mundialcatar.common.Constants
import com.skysam.hchirinos.mundialcatar.databinding.FragmentGamedayBinding
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.GameUser
import com.skysam.hchirinos.mundialcatar.dataclass.User
import com.skysam.hchirinos.mundialcatar.repositories.Auth
import com.skysam.hchirinos.mundialcatar.ui.commonView.EditResultsDialog
import com.skysam.hchirinos.mundialcatar.ui.commonView.GameSelectAdapter
import com.skysam.hchirinos.mundialcatar.ui.commonView.SelectGame
import com.skysam.hchirinos.mundialcatar.ui.commonView.WrapContentLinearLayoutManager
import java.util.*

class GamedayFragment : Fragment(), SelectGame {

    private var _binding: FragmentGamedayBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GamedayViewModel by activityViewModels()
    private val games = mutableListOf<Game>()
    private val allgames = mutableListOf<Game>()
    private val users = mutableListOf<User>()
    private lateinit var gamedayAdapter: GamedayAdapter
    private lateinit var gameSelectAdapter: GameSelectAdapter
    private lateinit var wrapContentLinearLayoutManager: WrapContentLinearLayoutManager
    private lateinit var calendar: Calendar
    private var admin = false

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
        if (Auth.getCurrenUser()!!.email == Constants.USER_MAIN ||
            Auth.getCurrenUser()!!.email == Constants.USER_TEST) admin = true
        gamedayAdapter = GamedayAdapter(games)
        gameSelectAdapter = GameSelectAdapter(games, this)
        wrapContentLinearLayoutManager = WrapContentLinearLayoutManager(requireContext(),
            RecyclerView.VERTICAL, false)
        binding.rvGames.apply {
            setHasFixedSize(true)
            adapter = if (admin) {
                gameSelectAdapter
            } else {
                gamedayAdapter
            }
            layoutManager = wrapContentLinearLayoutManager
        }
        calendar = Calendar.getInstance()

        /*binding.titleGameday.setOnClickListener {
            val tt = allgames.size
            if (allgames.size == 64) {
                viewModel.updatePOff(users, allgames)
            }
        }*/
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
                                if (i.id == j.id && (i.goalsTeam1 != j.goalsTeam1 || i.goalsTeam2 != j.goalsTeam2)) {
                                    if (admin) {
                                        gameSelectAdapter.notifyItemChanged(games.indexOf(i))
                                    } else {
                                        gamedayAdapter.notifyItemChanged(games.indexOf(i))
                                    }
                                }
                            }
                        }
                    } else {
                        if (admin) {
                            gameSelectAdapter.notifyItemRangeInserted(0, games.size)
                        } else {
                            gamedayAdapter.notifyItemRangeInserted(0, games.size)
                        }

                    }
                    if (Common.convertDateToString(calendar.time) == Common.convertDateToString(it[0].date))
                        binding.titleGameday.text = getString(R.string.title_gameday_yes)
                    else binding.titleGameday.text = getString(R.string.title_gameday_no)
                    binding.rvGames.visibility = View.VISIBLE
                    starsGames()
                } else {
                    binding.rvGames.visibility = View.GONE
                }
                binding.progressBar.visibility = View.GONE
            }
        }

        viewModel.users.observe(viewLifecycleOwner) {
            if (_binding != null) {
                users.clear()
                users.addAll(it)
            }
        }
        viewModel.allgames.observe(viewLifecycleOwner) {
            if (_binding != null) {
                allgames.clear()
                allgames.addAll(it)
            }
        }
    }

    private fun starsGames() {
        for (game in games) {
            val calendarCurrent = Calendar.getInstance()
            /*calendarCurrent.set(Calendar.DAY_OF_MONTH, 21)
            calendarCurrent.set(Calendar.MINUTE, 55)
            calendarCurrent.set(Calendar.MONTH, 10)
            calendarCurrent.set(Calendar.HOUR_OF_DAY, 5)*/
            val calendar = Calendar.getInstance()
            calendar.time = game.date
            calendar.add(Calendar.MINUTE, -10)

            if (calendarCurrent.time.after(calendar.time) && !game.started) {
                viewModel.starsGame(game)
            }
        }
    }

    override fun updateResult(game: Game) {
        viewModel.editGame(game)
        val editResultsDialog = EditResultsDialog()
        editResultsDialog.show(requireActivity().supportFragmentManager, tag)
    }

    override fun updatePredict(gameUser: GameUser) {

    }
}