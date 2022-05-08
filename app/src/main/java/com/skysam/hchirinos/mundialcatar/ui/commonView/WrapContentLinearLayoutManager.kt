package com.skysam.hchirinos.mundialcatar.ui.commonView

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Hector Chirinos on 07/05/2022.
 */

class WrapContentLinearLayoutManager: LinearLayoutManager {
 constructor(context: Context?) : super(context)
 constructor(context: Context?, orientation: Int, reverseLayout: Boolean) : super(context, orientation, reverseLayout)
 constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

 override fun supportsPredictiveItemAnimations(): Boolean {
  return false
 }

 override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
  try {
   super.onLayoutChildren(recycler, state)
  } catch (ex: Exception) {
   ex.printStackTrace()
  }
 }
}