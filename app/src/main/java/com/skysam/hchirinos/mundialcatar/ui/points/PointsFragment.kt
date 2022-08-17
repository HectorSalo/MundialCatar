package com.skysam.hchirinos.mundialcatar.ui.points

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.skysam.hchirinos.mundialcatar.databinding.FragmentPointsBinding
import com.skysam.hchirinos.mundialcatar.dataclass.User

class PointsFragment : Fragment() {
    private var _binding: FragmentPointsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PointsViewModel by activityViewModels()
    private val users = mutableListOf<User>()
    private lateinit var pointsAdapter: PointsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPointsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pointsAdapter = PointsAdapter(users)

        binding.rvPoints.apply {
            setHasFixedSize(true)
            adapter = pointsAdapter
        }

        viewModel.users.observe(viewLifecycleOwner) {
            users.clear()
            users.addAll(it)
            binding.progressBar.visibility = View.GONE
            binding.rvPoints.visibility = View.VISIBLE
            pointsAdapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}