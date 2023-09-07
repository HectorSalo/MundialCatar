package com.skysam.hchirinos.mundialcatar.ui.groups

import androidx.recyclerview.widget.DiffUtil
import com.skysam.hchirinos.mundialcatar.dataclass.Team

class GroupsDiffUtil(private val oldList: List<Team>, private val newList: List<Team>):
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