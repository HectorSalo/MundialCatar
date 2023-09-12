package com.skysam.hchirinos.mundialcatar.ui.gameday

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.skysam.hchirinos.mundialcatar.R
import com.skysam.hchirinos.mundialcatar.common.Common
import com.skysam.hchirinos.mundialcatar.databinding.FragmentGamedayBinding
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.GameToView
import com.skysam.hchirinos.mundialcatar.ui.commonView.EditResultsDialog
import java.util.Calendar
import java.util.Date

class GamedayFragment : Fragment(), OnClick {

    private var _binding: FragmentGamedayBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GamedayViewModel by activityViewModels()
    private var games = mutableListOf<Game>()
    private lateinit var gamedayAdapter: GamedayAdapter
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
        gamedayAdapter = GamedayAdapter(this)
        binding.rvGames.apply {
            setHasFixedSize(true)
            adapter = gamedayAdapter
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
                    for (gm in it) {
                        if (validateDates(it[0].date, gm.date)) games.add(gm)
                    }
                    if (Common.convertDateToString(calendar.time) == Common.convertDateToString(it[0].date))
                        binding.titleGameday.text = getString(R.string.title_gameday_yes)
                    else binding.titleGameday.text = getString(R.string.title_gameday_no)
                    viewModel.teams.observe(viewLifecycleOwner) {teams ->
                        val gamesToView = mutableListOf<GameToView>()
                        games.forEach { game ->
                            var flag1 = ""
                            var flag2 = ""

                            for (team in teams) {
                                if (team.id == game.team1) flag1 = team.flag
                                if (team.id == game.team2) flag2 = team.flag
                            }

                            val newGameToView = GameToView(
                                game.team1,
                                game.team2,
                                flag1,
                                flag2,
                                game.date,
                                game.goalsTeam1,
                                game.goalsTeam2,
                                game.round,
                                game.number
                            )
                            gamesToView.add(newGameToView)
                            if (games.last() == game) gamedayAdapter.updateList(gamesToView)
                        }

                    }
                    binding.rvGames.visibility = View.VISIBLE
                    starsGames()
                } else {
                    binding.rvGames.visibility = View.GONE
                }
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun starsGames() {
        for (game in games) {
            val calendarCurrent = Calendar.getInstance()
            val calendar = Calendar.getInstance()
            calendar.time = game.date
            calendar.add(Calendar.MINUTE, -10)

            if (calendarCurrent.time.after(calendar.time) && !game.started) {
                viewModel.starsGame(game)
            }
        }
    }

    private fun validateDates(firstDate: Date, secondDate: Date): Boolean {
        val calendar1 = Calendar.getInstance()
        calendar1.time = firstDate
        val day1 = calendar1.get(Calendar.DAY_OF_MONTH)
        val month1 = calendar1.get(Calendar.MONTH)
        val year1 = calendar1.get(Calendar.YEAR)

        val calendar2 = Calendar.getInstance()
        calendar2.time = secondDate
        val day2 = calendar2.get(Calendar.DAY_OF_MONTH)
        val month2 = calendar2.get(Calendar.MONTH)
        val year2 = calendar2.get(Calendar.YEAR)

        return day1 == day2 && month1 == month2 && year1 == year2
    }

    override fun select(game: GameToView) {
        var setGame: Game? = null
        for (gm in games) {
            if (gm.number == game.number) {
                setGame = gm
                break
            }
        }
        viewModel.setGame(setGame!!)
        val editResultsDialog = EditResultsDialog(true)
        editResultsDialog.show(requireActivity().supportFragmentManager, tag)
    }
}