package com.skysam.hchirinos.mundialcatar.ui.predicts

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.skysam.hchirinos.mundialcatar.dataclass.GameToView
import com.skysam.hchirinos.mundialcatar.dataclass.GameUser

/**
 * Created by Hector Chirinos on 07/09/2023.
 */

class PredictDiffUtil(private val oldList: List<GameUser>, private val newList: List<GameUser>):
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