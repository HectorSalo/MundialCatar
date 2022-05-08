package com.skysam.hchirinos.mundialcatar.ui.groups

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.skysam.hchirinos.mundialcatar.R
import com.skysam.hchirinos.mundialcatar.dataclass.Team

/**
 * Created by Hector Chirinos on 07/05/2022.
 */

class GroupsAdapter(private var teams: MutableList<Team>): RecyclerView.Adapter<GroupsAdapter.ViewHolder>() {
 lateinit var context: Context

 override fun onCreateViewHolder(
  parent: ViewGroup,
  viewType: Int
 ): GroupsAdapter.ViewHolder {
  val view = LayoutInflater.from(parent.context)
   .inflate(R.layout.layout_item_group, parent, false)
  context = parent.context
  return ViewHolder(view)
 }

 override fun onBindViewHolder(holder: GroupsAdapter.ViewHolder, position: Int) {
  val item = teams[position]
  holder.team.text = item.id
  holder.wins.text = if (item.wins == -1) "PG" else item.wins.toString()
  holder.tied.text = if (item.tied == -1) "PE" else item.tied.toString()
  holder.defeats.text = if (item.defeats == -1) "PP" else item.defeats.toString()
  holder.goalsConceded.text = if (item.goalsConceded == -1) "GC" else item.goalsConceded.toString()
  holder.goalsMade.text = if (item.goalsMade == -1) "GF" else item.goalsMade.toString()
  holder.points.text = if (item.points == -1) "Pts" else item.points.toString()

  if (item.flag.isNotEmpty()) {
   Glide.with(context)
    .load(item.flag)
    .centerCrop()
    .placeholder(R.drawable.ic_flag_24)
    .into(holder.flag)
   holder.flag.visibility = View.VISIBLE
  } else {
   holder.flag.visibility = View.GONE
  }

  if (position == 1 || position == 2) holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.rosado_light))
 }

 override fun getItemCount(): Int = teams.size

 inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
  val team: TextView = view.findViewById(R.id.tv_team)
  val wins: TextView = view.findViewById(R.id.tv_wins)
  val tied: TextView = view.findViewById(R.id.tv_tied)
  val defeats: TextView = view.findViewById(R.id.tv_defeats)
  val flag: ImageView = view.findViewById(R.id.iv_flag)
  val goalsMade: TextView = view.findViewById(R.id.tv_goals_made)
  val goalsConceded: TextView = view.findViewById(R.id.tv_goals_conceded)
  val points: TextView = view.findViewById(R.id.tv_points)
  val card: MaterialCardView = view.findViewById(R.id.card)
 }
}