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
import com.google.android.material.chip.Chip
import com.google.android.material.color.MaterialColors
import com.skysam.hchirinos.mundialcatar.R
import com.skysam.hchirinos.mundialcatar.dataclass.User
import com.skysam.hchirinos.mundialcatar.repositories.Auth

/**
 * Created by Hector Chirinos on 11/05/2022.
 */

class PointsAdapter(private val auth: Auth): RecyclerView.Adapter<PointsAdapter.ViewHolder>() {
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
        val rank = position + 1

        holder.user.text = item.name
        holder.points.text = item.points.toString()

        Glide.with(holder.itemView)
            .load(item.image)
            .centerCrop()
            .circleCrop()
            .placeholder(R.drawable.ic_person_24)
            .into(holder.image)

        // Chip Top 3
        if (rank <= 3) {
            holder.chipRank.visibility = View.VISIBLE
            holder.chipRank.text = "#$rank"

            // Paleta sutil por posición (sin colores chillones)
            val (bgAttr, strokeAttr, textAttr) = when (rank) {
                1 -> Triple(
                    com.google.android.material.R.attr.colorTertiaryContainer,
                    com.google.android.material.R.attr.colorTertiary,
                    com.google.android.material.R.attr.colorOnTertiaryContainer
                )
                2 -> Triple(
                    com.google.android.material.R.attr.colorSecondaryContainer,
                    com.google.android.material.R.attr.colorSecondary,
                    com.google.android.material.R.attr.colorOnSecondaryContainer
                )
                else -> Triple(
                    com.google.android.material.R.attr.colorPrimaryContainer,
                    com.google.android.material.R.attr.colorOnPrimary,
                    com.google.android.material.R.attr.colorOnPrimaryContainer
                )
            }

            val bg = MaterialColors.getColor(holder.itemView, bgAttr)
            val stroke = MaterialColors.getColor(holder.itemView, strokeAttr)
            val text = MaterialColors.getColor(holder.itemView, textAttr)

            holder.chipRank.chipBackgroundColor = android.content.res.ColorStateList.valueOf(bg)
            holder.chipRank.chipStrokeColor = android.content.res.ColorStateList.valueOf(stroke)
            holder.chipRank.setTextColor(text)
        } else {
            holder.chipRank.visibility = View.GONE
        }

        // Highlight "tú" (sutil)
        val isMe = item.id == auth.getCurrentUser()?.uid
        val cardBg = if (isMe) {
            MaterialColors.getColor(
                holder.itemView,
                com.google.android.material.R.attr.colorSecondaryContainer
            )
        } else {
            MaterialColors.getColor(
                holder.itemView,
                com.google.android.material.R.attr.colorSurface
            )
        }

        val cardStroke = if (isMe) {
            MaterialColors.getColor(
                holder.itemView,
                com.google.android.material.R.attr.colorSecondary
            )
        } else {
            MaterialColors.getColor(
                holder.itemView,
                com.google.android.material.R.attr.colorOutline
            )
        }

        holder.card.setCardBackgroundColor(cardBg)
        holder.card.strokeColor = cardStroke
        holder.card.strokeWidth = if (isMe) 2 else 1
    }

    override fun getItemCount(): Int = users.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val user: TextView = view.findViewById(R.id.tv_user)
        val image: ImageView = view.findViewById(R.id.iv_user)
        val points: TextView = view.findViewById(R.id.tv_points)
        val chipRank: Chip = view.findViewById(R.id.chipRank)

        val card: MaterialCardView = view.findViewById(R.id.card)
    }

    fun updateList(newList: List<User>) {
        val diffUtil = PointsDiffUtil(users, newList)
        val result = DiffUtil.calculateDiff(diffUtil)
        users = newList
        result.dispatchUpdatesTo(this)
    }
}