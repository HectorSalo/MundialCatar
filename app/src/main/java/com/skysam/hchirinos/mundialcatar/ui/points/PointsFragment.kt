package com.skysam.hchirinos.mundialcatar.ui.points

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.skysam.hchirinos.mundialcatar.R

class PointsFragment : Fragment() {

    companion object {
        fun newInstance() = PointsFragment()
    }

    private lateinit var viewModel: PointsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.points_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PointsViewModel::class.java)
        // TODO: Use the ViewModel
    }

}