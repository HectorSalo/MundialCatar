package com.skysam.hchirinos.mundialcatar.ui.predicts

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.skysam.hchirinos.mundialcatar.R

class PredictsFragment : Fragment() {

    companion object {
        fun newInstance() = PredictsFragment()
    }

    private lateinit var viewModel: PredictsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_predicts, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PredictsViewModel::class.java)
        // TODO: Use the ViewModel
    }

}