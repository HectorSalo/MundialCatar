package com.skysam.hchirinos.mundialcatar.ui.predicts

import android.content.Context
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
import com.skysam.hchirinos.mundialcatar.common.Common
import com.skysam.hchirinos.mundialcatar.dataclass.GameToView

/**
 * Created by Hector Chirinos on 11/05/2022.
 */

class PredictsAdapter(private val onGameClick: (GameToView) -> Unit) :
    RecyclerView.Adapter<PredictsAdapter.ViewHolder>() {
    private var games = listOf<GameToView>()
    private lateinit var context: Context

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
        holder.date.text = Common.convertDateTimeToString(item.date)

        if (item.hasPrediction) {
            // Mostrar marcador pronosticado
            holder.result1.text = item.homeGoals.toString()
            holder.result2.text = item.awayGoals.toString()

            // Mostrar puntos
            holder.round.text = context.getString(
                R.string.text_points_predict,
                item.points
            )

            // Estilo de tarjeta "activa"
            holder.card.strokeColor = ContextCompat.getColor(
                context,
                R.color.garnet_normal
            )
            holder.card.alpha = 1f
        } else {
            // Sin predicción
            holder.result1.text = context.getString(R.string.text_empty_score)
            holder.result2.text = context.getString(R.string.text_empty_score)

            // Texto indicando que no hay predicción
            holder.round.text = context.getString(R.string.text_not_predict)

            // Estilo de tarjeta "pendiente"
            holder.card.strokeColor = ContextCompat.getColor(
                context,
                android.R.color.darker_gray
            )
            holder.card.alpha = 0.8f
        }


        Glide.with(context)
            .load(item.flag1)
            .centerCrop()
            .circleCrop()
            .placeholder(R.drawable.ic_flag_24)
            .into(holder.flag1)

        Glide.with(context)
            .load(item.flag2)
            .centerCrop()
            .circleCrop()
            .placeholder(R.drawable.ic_flag_24)
            .into(holder.flag2)

        holder.card.isCheckable = true
        holder.card.isFocusable = true
        holder.card.setOnClickListener { onGameClick(item) }
    }

    override fun getItemCount(): Int = games.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val team1: TextView = view.findViewById(R.id.tv_team1)
        val team2: TextView = view.findViewById(R.id.tv_team2)
        val result1: TextView = view.findViewById(R.id.tv_result1)
        val result2: TextView = view.findViewById(R.id.tv_result2)
        val flag1: ImageView = view.findViewById(R.id.iv_flag1)
        val flag2: ImageView = view.findViewById(R.id.iv_flag2)
        val date: TextView = view.findViewById(R.id.tv_date)
        val round: TextView = view.findViewById(R.id.tv_round)
        val card: MaterialCardView = view.findViewById(R.id.card)
    }

    fun updateList(newList: List<GameToView>) {
        val diffUtil = PredictDiffUtil(games, newList)
        val result = DiffUtil.calculateDiff(diffUtil)
        games = newList
        result.dispatchUpdatesTo(this)
    }
}