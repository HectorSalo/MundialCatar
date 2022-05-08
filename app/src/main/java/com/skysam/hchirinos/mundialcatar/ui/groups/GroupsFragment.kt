package com.skysam.hchirinos.mundialcatar.ui.groups

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.skysam.hchirinos.mundialcatar.common.Constants
import com.skysam.hchirinos.mundialcatar.databinding.FragmentGroupsBinding
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.Team
import com.skysam.hchirinos.mundialcatar.ui.commonView.WrapContentLinearLayoutManager
import com.skysam.hchirinos.mundialcatar.ui.gameday.GamedayAdapter

class GroupsFragment : Fragment() {

    private val viewModel: GroupsViewModel by activityViewModels()
    private var _binding: FragmentGroupsBinding? = null
    private val binding get() = _binding!!
    private lateinit var wrapContentLinearLayoutManager: WrapContentLinearLayoutManager
    private lateinit var wrapContentLinearLayoutManager2: WrapContentLinearLayoutManager
    private lateinit var groupsAdapter: GroupsAdapter
    private lateinit var gamedayAdapter: GamedayAdapter
    private val teams = mutableListOf<Team>()
    private val teamsByGroup = mutableListOf<Team>()
    private val games = mutableListOf<Game>()
    private val gamesByGroup = mutableListOf<Game>()
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
        wrapContentLinearLayoutManager = WrapContentLinearLayoutManager(requireContext(),
            RecyclerView.VERTICAL, false)
        wrapContentLinearLayoutManager2 = WrapContentLinearLayoutManager(requireContext(),
            RecyclerView.VERTICAL, false)
        groupsAdapter = GroupsAdapter(teamsByGroup)
        gamedayAdapter = GamedayAdapter(gamesByGroup)
        binding.rvGroup.apply {
            setHasFixedSize(true)
            adapter = groupsAdapter
            layoutManager = wrapContentLinearLayoutManager2
        }
        binding.rvGames.apply {
            setHasFixedSize(true)
            adapter = gamedayAdapter
            layoutManager = wrapContentLinearLayoutManager
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
                showGroup()
            }
        }

        viewModel.teams.observe(viewLifecycleOwner) {
            if (_binding != null) {
                teams.clear()
                teams.addAll(it)
                showGroup()
            }
        }

        viewModel.games.observe(viewLifecycleOwner) {
            if (_binding != null) {
                games.clear()
                games.addAll(it)
                showGroup()
            }
        }
    }

    private fun showGroup() {
        gamesByGroup.clear()
        teamsByGroup.clear()
        val titles = Team("", "", -1, -1, -1, -1, -1, -1, "")
        teamsByGroup.add(titles)
        for (game in games) {
            if (game.round == group) gamesByGroup.add(game)
        }
        for (team in teams) {
            if (team.group == group) teamsByGroup.add(team)
        }

        binding.rvGames.visibility = View.VISIBLE
        binding.rvGroup.visibility = View.VISIBLE
        gamedayAdapter.notifyDataSetChanged()
        groupsAdapter.notifyDataSetChanged()
        binding.progressBar.visibility = View.GONE
    }
}