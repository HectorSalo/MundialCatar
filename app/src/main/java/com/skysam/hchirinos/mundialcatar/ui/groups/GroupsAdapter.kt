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
import com.skysam.hchirinos.mundialcatar.dataclass.GroupStandingUi
import com.skysam.hchirinos.mundialcatar.dataclass.Team

/**
 * Created by Hector Chirinos on 07/05/2022.
 */

class GroupsAdapter : RecyclerView.Adapter<GroupsAdapter.ViewHolder>() {
    private var teams = listOf<GroupStandingUi>()
    lateinit var context: Context

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_group, parent, false)
        context = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = teams[position]
        if (position == 0) {
            // Fila de títulos
            holder.team.text = "Equipo"
            holder.wins.text = "PG"
            holder.tied.text = "PE"
            holder.defeats.text = "PP"
            holder.goalsConceded.text = "GC"
            holder.goalsMade.text = "GF"
            holder.points.text = "Pts"
            holder.flag.visibility = View.GONE

            holder.card.setCardBackgroundColor(getPrimaryColor())
            setTextColor(holder, getColorText())
            return
        }

        holder.team.text = item.teamName
        holder.wins.text = item.wins.toString()
        holder.tied.text = item.draws.toString()
        holder.defeats.text = item.losses.toString()
        holder.goalsConceded.text = item.goalsAgainst.toString()
        holder.goalsMade.text = item.goalsFor.toString()
        holder.points.text = item.points.toString()

        if (item.flagUrl.isNotEmpty()) {
            Glide.with(context)
                .load(item.flagUrl)
                .centerCrop()
                .circleCrop()
                .placeholder(R.drawable.ic_flag_24)
                .into(holder.flag)
            holder.flag.visibility = View.VISIBLE
        } else {
            holder.flag.visibility = View.GONE
        }

        val isQualified = item.qualifiesAsTopTwo || item.qualifiesAsBestThird

        if (isQualified) {
            holder.card.setCardBackgroundColor(
                ContextCompat.getColor(context, R.color.garnet_normal)
            )
            setTextColor(holder, ContextCompat.getColor(context, R.color.white))
        } else {
            holder.card.setCardBackgroundColor(getPrimaryColor())
            setTextColor(holder, getColorText())
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

    fun updateList(newList: List<GroupStandingUi>) {
        val diffUtil = GroupsDiffUtil(teams, newList)
        val result = DiffUtil.calculateDiff(diffUtil)
        teams = newList
        result.dispatchUpdatesTo(this)
    }

    private fun setTextColor(holder: ViewHolder, color: Int) {
        holder.team.setTextColor(color)
        holder.wins.setTextColor(color)
        holder.defeats.setTextColor(color)
        holder.tied.setTextColor(color)
        holder.goalsConceded.setTextColor(color)
        holder.goalsMade.setTextColor(color)
        holder.points.setTextColor(color)
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