package com.skysam.hchirinos.mundial2026.ui.groups

import androidx.recyclerview.widget.DiffUtil
import com.skysam.hchirinos.mundial2026.dataclass.GroupStandingUi

class GroupsDiffUtil(private val oldList: List<GroupStandingUi>, private val newList: List<GroupStandingUi>):
    DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return newList.contains(oldList[oldItemPosition])
    }
}