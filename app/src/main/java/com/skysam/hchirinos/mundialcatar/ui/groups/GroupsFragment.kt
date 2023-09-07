package com.skysam.hchirinos.mundialcatar.ui.groups

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.skysam.hchirinos.mundialcatar.common.Constants
import com.skysam.hchirinos.mundialcatar.databinding.FragmentGroupsBinding
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.GameToView
import com.skysam.hchirinos.mundialcatar.dataclass.Team
import com.skysam.hchirinos.mundialcatar.ui.gameday.GamedayAdapter

class GroupsFragment : Fragment() {

    private val viewModel: GroupsViewModel by activityViewModels()
    private var _binding: FragmentGroupsBinding? = null
    private val binding get() = _binding!!
    private lateinit var groupsAdapter: GroupsAdapter
    private lateinit var gamedayAdapter: GamedayAdapter
    private var teams = listOf<Team>()
    private var games = listOf<Game>()
    private var group: String = Constants.GROUP_A

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroupsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        groupsAdapter = GroupsAdapter()
        gamedayAdapter = GamedayAdapter()
        binding.rvGroup.apply {
            setHasFixedSize(true)
            adapter = groupsAdapter
        }
        binding.rvGames.apply {
            setHasFixedSize(true)
            adapter = gamedayAdapter
        }
        loadViewModel()
    }

    companion object {
        @JvmStatic
        fun newInstance(): GroupsFragment {
            return GroupsFragment()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadViewModel() {

        viewModel.index.observe(viewLifecycleOwner) {
            if (_binding != null) {
                group = when(it) {
                    0 -> Constants.GROUP_A
                    1 -> Constants.GROUP_B
                    2 -> Constants.GROUP_C
                    3 -> Constants.GROUP_D
                    4 -> Constants.GROUP_E
                    5 -> Constants.GROUP_F
                    6 -> Constants.GROUP_G
                    7 -> Constants.GROUP_H
                    else -> Constants.GROUP_A
                }
                if (games.isEmpty()) {
                    viewModel.games.observe(viewLifecycleOwner) {gamesFrom ->
                        if (_binding != null) {
                            games = gamesFrom
                            if (teams.isEmpty()) {
                                viewModel.teams.observe(viewLifecycleOwner) {teamsFrom ->
                                    if (_binding != null) {
                                        teams = teamsFrom
                                        showGroup()
                                    }
                                }
                            } else showGroup()
                        }
                    }
                } else showGroup()
            }
        }
    }

    private fun showGroup() {
        val teamsByGroup = mutableListOf<Team>()
        val titles = Team("", "", -1, -1, -1, -1, -1, -1, "")
        teamsByGroup.add(titles)
        for (team in teams) {
            if (team.group == group) teamsByGroup.add(team)
        }

        val gamesToView = mutableListOf<GameToView>()
        for (game in games) {
            if (game.round == group) {
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
                    game.round
                )
                gamesToView.add(newGameToView)
            }
        }

        gamedayAdapter.updateList(gamesToView)
        groupsAdapter.updateList(teamsByGroup)
        binding.rvGames.visibility = View.VISIBLE
        binding.rvGroup.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
    }
}