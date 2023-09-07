package com.skysam.hchirinos.mundialcatar.ui.gameday

import androidx.recyclerview.widget.DiffUtil
import com.skysam.hchirinos.mundialcatar.dataclass.GameToView

class GameDayDiffUtil(private val oldList: List<GameToView>, private val newList: List<GameToView>):
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