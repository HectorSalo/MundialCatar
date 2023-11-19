package com.skysam.hchirinos.mundialcatar.ui.groups

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
import com.skysam.hchirinos.mundialcatar.dataclass.Team

/**
 * Created by Hector Chirinos on 07/05/2022.
 */

class GroupsAdapter: RecyclerView.Adapter<GroupsAdapter.ViewHolder>() {
 private var teams = listOf<Team>()
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
    .circleCrop()
    .placeholder(R.drawable.ic_flag_24)
    .into(holder.flag)
   holder.flag.visibility = View.VISIBLE
  } else {
   holder.flag.visibility = View.GONE
  }

  if (position == 1 || position == 2){
   holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.indigo))
   holder.team.setTextColor(ContextCompat.getColor(context, R.color.white))
   holder.wins.setTextColor(ContextCompat.getColor(context, R.color.white))
   holder.defeats.setTextColor(ContextCompat.getColor(context, R.color.white))
   holder.tied.setTextColor(ContextCompat.getColor(context, R.color.white))
   holder.goalsConceded.setTextColor(ContextCompat.getColor(context, R.color.white))
   holder.goalsMade.setTextColor(ContextCompat.getColor(context, R.color.white))
   holder.points.setTextColor(ContextCompat.getColor(context, R.color.white))
  } else {
   holder.card.setCardBackgroundColor(getPrimaryColor())
   holder.team.setTextColor(getColorText())
   holder.wins.setTextColor(getColorText())
   holder.defeats.setTextColor(getColorText())
   holder.tied.setTextColor(getColorText())
   holder.goalsConceded.setTextColor(getColorText())
   holder.goalsMade.setTextColor(getColorText())
   holder.points.setTextColor(getColorText())
  }
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

 fun updateList(newList: List<Team>) {
  val diffUtil = GroupsDiffUtil(teams, newList)
  val result = DiffUtil.calculateDiff(diffUtil)
  teams = newList
  result.dispatchUpdatesTo(this)
 }

 private fun getPrimaryColor(): Int {
  val typedValue = TypedValue()
  context.theme.resolveAttribute(android.R.attr.colorBackground, typedValue, true)
  return ContextCompat.getColor(context, typedValue.resourceId)
 }

 private fun getColorText(): Int {
  val typedValue = TypedValue()
  context.theme.resolveAttribute(android.R.attr.colorControlNormal, typedValue, true)
  return ContextCompat.getColor(context, typedValue.resourceId)
 }
}