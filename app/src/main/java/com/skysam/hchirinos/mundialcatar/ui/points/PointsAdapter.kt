package com.skysam.hchirinos.mundialcatar.ui.points

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.skysam.hchirinos.mundialcatar.R
import com.skysam.hchirinos.mundialcatar.dataclass.User
import com.skysam.hchirinos.mundialcatar.repositories.Auth

/**
 * Created by Hector Chirinos on 11/05/2022.
 */

class PointsAdapter: RecyclerView.Adapter<PointsAdapter.ViewHolder>() {
    lateinit var context: Context
    private var users = listOf<User>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PointsAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_user_points_item, parent, false)
        context = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: PointsAdapter.ViewHolder, position: Int) {
        val item = users[position]
        holder.user.text = item.name
        holder.points.text = item.points.toString()

        Glide.with(context)
            .load(item.image)
            .centerCrop()
            .circleCrop()
            .placeholder(R.drawable.ic_person_24)
            .into(holder.image)

        if (item.id == Auth.getCurrenUser()!!.uid) {
            holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.rosado_light))
        } else {
            holder.card.setCardBackgroundColor(getPrimaryColor())
        }
    }

    override fun getItemCount(): Int = users.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val user: TextView = view.findViewById(R.id.tv_user)
        val image: ImageView = view.findViewById(R.id.iv_user)
        val points: TextView = view.findViewById(R.id.tv_points)
        val card: MaterialCardView = view.findViewById(R.id.card)
    }

    private fun getPrimaryColor(): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.colorBackground, typedValue, true)
        return ContextCompat.getColor(context, typedValue.resourceId)
    }

    fun updateList(newList: List<User>) {
        val diffUtil = PointsDiffUtil(users, newList)
        val result = DiffUtil.calculateDiff(diffUtil)
        users = newList
        result.dispatchUpdatesTo(this)
    }
}