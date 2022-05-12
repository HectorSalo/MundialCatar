package com.skysam.hchirinos.mundialcatar.ui.points

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.skysam.hchirinos.mundialcatar.R
import com.skysam.hchirinos.mundialcatar.dataclass.User

/**
 * Created by Hector Chirinos on 11/05/2022.
 */

class PointsAdapter(private val users: MutableList<User>): RecyclerView.Adapter<PointsAdapter.ViewHolder>() {
    lateinit var context: Context

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
        holder.team.text = item.name
        holder.points.text = item.points.toString()

        Glide.with(context)
            .load(item.image)
            .centerCrop()
            .circleCrop()
            .placeholder(R.drawable.ic_person_24)
            .into(holder.image)
    }

    override fun getItemCount(): Int = users.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val team: TextView = view.findViewById(R.id.tv_user)
        val image: ImageView = view.findViewById(R.id.iv_user)
        val points: TextView = view.findViewById(R.id.tv_points)
    }
}