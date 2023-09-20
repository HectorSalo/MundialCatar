package com.skysam.hchirinos.mundialcatar.ui.playoff

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter


class SectionsPagerAdapter(fm: FragmentActivity) :
    FragmentStateAdapter(fm) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return PlayOffFragment.newInstance()
    }
}