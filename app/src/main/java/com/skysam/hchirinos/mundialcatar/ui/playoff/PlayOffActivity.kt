package com.skysam.hchirinos.mundialcatar.ui.playoff

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.skysam.hchirinos.mundialcatar.common.Constants
import com.skysam.hchirinos.mundialcatar.databinding.ActivityPlayOffBinding

class PlayOffActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayOffBinding
    private val viewModel: PlayOffViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPlayOffBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sectionsPagerAdapter = SectionsPagerAdapter(this)
        binding.viewPager.adapter = sectionsPagerAdapter
        TabLayoutMediator(binding.tabs, binding.viewPager) { tab, position ->
            tab.text = when(position) {
                0 -> Constants.OCTAVOS
                1 -> Constants.CUARTOS
                2 -> Constants.SEMIFINAL
                3 -> Constants.FINAL
                else -> Constants.GAMES
            }
        }.attach()
        binding.viewPager.registerOnPageChangeCallback(callback)
    }

    private val callback: ViewPager2.OnPageChangeCallback = object: ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            viewModel.setIndex(position)
        }
    }
}