package com.skysam.hchirinos.mundial2026.ui.groups

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.skysam.hchirinos.mundial2026.R
import com.skysam.hchirinos.mundial2026.dataclass.GroupStandingUi

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

        holder.team.text = item.teamName
        holder.points.text = item.points.toString()
        val statsText = "PJ ${item.played}   G ${item.wins}   E ${item.draws}   P ${item.losses}   " +
                "GF ${item.goalsFor}   GC ${item.goalsAgainst}   DG ${item.goalDiff}"
        holder.stats.text = statsText

        Glide.with(context)
            .load(item.flagUrl)
            .centerCrop()
            .circleCrop()
            .placeholder(R.drawable.ic_flag_24)
            .into(holder.flag)

        val isQualified = item.qualifiesAsTopTwo || item.qualifiesAsBestThird

        if (isQualified) {
            holder.constraint.setBackgroundColor(
                ContextCompat.getColor(context, R.color.garnet_normal)
            )
            setTextColor(holder, ContextCompat.getColor(context, R.color.white))
        } else {
            holder.constraint.setBackgroundColor(getPrimaryColor())
            setTextColor(holder, getColorText())
        }
    }

    override fun getItemCount(): Int = teams.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val team: TextView = view.findViewById(R.id.tv_team)
        val flag: ImageView = view.findViewById(R.id.iv_flag)
        val stats: TextView = view.findViewById(R.id.tv_stats)
        val points: TextView = view.findViewById(R.id.tv_points)
        val constraint: ConstraintLayout = view.findViewById(R.id.card)
    }

    fun updateList(newList: List<GroupStandingUi>) {
        val diffUtil = GroupsDiffUtil(teams, newList)
        val result = DiffUtil.calculateDiff(diffUtil)
        teams = newList
        result.dispatchUpdatesTo(this)
    }

    private fun setTextColor(holder: ViewHolder, color: Int) {
        holder.team.setTextColor(color)
        holder.stats.setTextColor(color)
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