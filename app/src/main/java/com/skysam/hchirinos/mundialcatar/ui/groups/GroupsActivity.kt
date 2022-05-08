package com.skysam.hchirinos.mundialcatar.ui.groups

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.skysam.hchirinos.mundialcatar.common.Constants
import com.skysam.hchirinos.mundialcatar.databinding.ActivityGroupsBinding

class GroupsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGroupsBinding
    private val viewModel: GroupsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGroupsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sectionsPagerAdapter = SectionsPagerAdapter(this)
        binding.viewPager.adapter = sectionsPagerAdapter
        TabLayoutMediator(binding.tabs, binding.viewPager) { tab, position ->
            tab.text = when(position) {
                0 -> Constants.GROUP_A
                1 -> Constants.GROUP_B
                2 -> Constants.GROUP_C
                3 -> Constants.GROUP_D
                4 -> Constants.GROUP_E
                5 -> Constants.GROUP_F
                6 -> Constants.GROUP_G
                7 -> Constants.GROUP_H
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