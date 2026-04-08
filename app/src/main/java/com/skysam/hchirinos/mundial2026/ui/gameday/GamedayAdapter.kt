package com.skysam.hchirinos.mundial2026.ui.gameday

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.skysam.hchirinos.mundial2026.R
import com.skysam.hchirinos.mundial2026.common.Common
import com.skysam.hchirinos.mundial2026.dataclass.GameToView

/**
 * Created by Hector Chirinos on 05/05/2022.
 */

class GamedayAdapter(private val canEdit: Boolean,
                     private val onGameClick: (GameToView) -> Unit) :
    RecyclerView.Adapter<GamedayAdapter.ViewHolder>() {
    lateinit var context: Context
    private var games = listOf<GameToView>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_game_item, parent, false)
        context = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = games[position]
        holder.team1.text = item.homeTeamName.ifEmpty { "Sin definir" }
        holder.team2.text = item.awayTeamName.ifEmpty { "Sin definir" }
        holder.result1.text = if (item.homeGoals != null) item.homeGoals.toString() else context.getString(R.string.text_empty_score)
        holder.result2.text = if (item.awayGoals != null) item.awayGoals.toString() else context.getString(R.string.text_empty_score)
        holder.date.text = Common.convertDateTimeToString(item.date)
        holder.round.text = item.round
        holder.stadium.text = item.stadiumName
        holder.location.text = item.stadiumCity

        Glide.with(context)
            .load(item.flag1Res)
            .centerCrop()
            .circleCrop()
            .placeholder(R.drawable.ic_flag_24)
            .into(holder.flag1)

        Glide.with(context)
            .load(item.flag2Res)
            .centerCrop()
            .circleCrop()
            .placeholder(R.drawable.ic_flag_24)
            .into(holder.flag2)

        if (canEdit) {
            holder.card.setOnClickListener { onGameClick(item) }
        } else {
            holder.card.setOnClickListener(null)
        }
    }

    override fun getItemCount(): Int = games.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val team1: TextView = view.findViewById(R.id.tv_team1)
        val team2: TextView = view.findViewById(R.id.tv_team2)
        val result1: TextView = view.findViewById(R.id.tv_result1)
        val result2: TextView = view.findViewById(R.id.tv_result2)
        val flag1: ImageView = view.findViewById(R.id.iv_flag1)
        val flag2: ImageView = view.findViewById(R.id.iv_flag2)
        val date: TextView = view.findViewById(R.id.tv_date)
        val round: TextView = view.findViewById(R.id.tv_round)
        val stadium: TextView = view.findViewById(R.id.tv_stadium)
        val location: TextView = view.findViewById(R.id.tv_location)
        val card: MaterialCardView = view.findViewById(R.id.card)
    }

    fun updateList(newList: List<GameToView>) {
        val diffUtil = GameDayDiffUtil(games, newList)
        val result = DiffUtil.calculateDiff(diffUtil)
        games = newList
        result.dispatchUpdatesTo(this)
    }
}