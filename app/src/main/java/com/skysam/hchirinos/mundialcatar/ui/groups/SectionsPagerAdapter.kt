package com.skysam.hchirinos.mundialcatar.ui.groups

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class SectionsPagerAdapter(fm: FragmentActivity) :
    FragmentStateAdapter(fm) {

    override fun getItemCount(): Int = 8

    override fun createFragment(position: Int): Fragment {
        return GroupsFragment.newInstance()
    }
}