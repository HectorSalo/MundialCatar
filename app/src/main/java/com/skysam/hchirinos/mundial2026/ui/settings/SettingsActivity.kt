package com.skysam.hchirinos.mundial2026.ui.settings

import android.os.Bundle
import com.skysam.hchirinos.mundial2026.BaseActivity
import com.skysam.hchirinos.mundial2026.databinding.SettingsActivityBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : BaseActivity() {

    private lateinit var binding: SettingsActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupEdgeToEdge(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}
