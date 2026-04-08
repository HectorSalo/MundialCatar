package com.skysam.hchirinos.mundial2026.ui.predicts

import androidx.recyclerview.widget.DiffUtil
import com.skysam.hchirinos.mundial2026.dataclass.GameToView

/**
 * Created by Hector Chirinos on 07/09/2023.
 */

class PredictDiffUtil(private val oldList: List<GameToView>, private val newList: List<GameToView>):
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