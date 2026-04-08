package com.skysam.hchirinos.mundial2026.ui.playoff

import android.os.Bundle
import androidx.activity.viewModels
import com.skysam.hchirinos.mundial2026.BaseActivity
import com.skysam.hchirinos.mundial2026.databinding.ActivityPlayOffBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayOffActivity : BaseActivity() {

    private lateinit var binding: ActivityPlayOffBinding
    private val viewModel: PlayOffViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPlayOffBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupEdgeToEdge(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, PlayOffFragment.newInstance())
                .commit()
        }

        viewModel.setIndex(0)
    }
}