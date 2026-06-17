package com.skysam.hchirinos.mundial2026.ui.points

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.skysam.hchirinos.mundial2026.common.RulesDialog
import com.skysam.hchirinos.mundial2026.databinding.FragmentPointsBinding
import com.skysam.hchirinos.mundial2026.dataclass.User
import com.skysam.hchirinos.mundial2026.repositories.Auth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
@AndroidEntryPoint
class PointsFragment : Fragment() {
    private var _binding: FragmentPointsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PointsViewModel by activityViewModels()
    @Inject
    lateinit var auth: Auth
    private var users = listOf<User>()
    private lateinit var pointsAdapter: PointsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPointsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pointsAdapter = PointsAdapter(auth)

        binding.rvPoints.apply {
            setHasFixedSize(true)
            adapter = pointsAdapter
        }

        binding.btnRules.setOnClickListener { RulesDialog.show(requireActivity()) }

        viewModel.users.observe(viewLifecycleOwner) {
            users = it
            binding.progress.visibility = View.GONE
            binding.rvPoints.visibility = View.VISIBLE
            pointsAdapter.updateList(users)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}