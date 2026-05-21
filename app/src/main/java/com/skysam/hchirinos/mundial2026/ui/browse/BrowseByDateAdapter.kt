package com.skysam.hchirinos.mundial2026.ui.browse

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.skysam.hchirinos.mundial2026.R
import com.skysam.hchirinos.mundial2026.common.Common
import com.skysam.hchirinos.mundial2026.dataclass.GameToView
import com.skysam.hchirinos.mundial2026.dataclass.MatchScheduleSlot

/**
 * Adapter sólo lectura para `BrowseByDateFragment`. Renderiza dos tipos:
 *  - [Row.GameRow]: usa `layout_game_item.xml` (equipos, banderas, marcador).
 *  - [Row.SlotRow]: usa `layout_slot_item.xml` (título + "Partido por definir"
 *                   + sede). Para fechas con slot oficial pero sin Game real.
 *
 * No emite clicks ni soporta edición — la pantalla es de consulta.
 */
class BrowseByDateAdapter :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    sealed class Row {
        data class GameRow(val gameToView: GameToView) : Row()
        data class SlotRow(val slot: MatchScheduleSlot) : Row()
    }

    private lateinit var context: Context
    private var items: List<Row> = emptyList()

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is Row.GameRow -> VIEW_TYPE_GAME
        is Row.SlotRow -> VIEW_TYPE_SLOT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_GAME -> GameViewHolder(
                inflater.inflate(R.layout.layout_game_item, parent, false)
            )
            VIEW_TYPE_SLOT -> SlotViewHolder(
                inflater.inflate(R.layout.layout_slot_item, parent, false)
            )
            else -> throw IllegalStateException("Unknown viewType=$viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = items[position]) {
            is Row.GameRow -> (holder as GameViewHolder).bind(row.gameToView)
            is Row.SlotRow -> (holder as SlotViewHolder).bind(row.slot)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newItems: List<Row>) {
        val diff = DiffUtil.calculateDiff(BrowseByDateDiff(items, newItems))
        items = newItems
        diff.dispatchUpdatesTo(this)
    }

    // ---- Game view holder (replica el binding de GamedayAdapter en modo lectura) ----

    private inner class GameViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val team1: TextView = view.findViewById(R.id.tv_team1)
        private val team2: TextView = view.findViewById(R.id.tv_team2)
        private val result1: TextView = view.findViewById(R.id.tv_result1)
        private val result2: TextView = view.findViewById(R.id.tv_result2)
        private val flag1: ImageView = view.findViewById(R.id.iv_flag1)
        private val flag2: ImageView = view.findViewById(R.id.iv_flag2)
        private val date: TextView = view.findViewById(R.id.tv_date)
        private val round: TextView = view.findViewById(R.id.tv_round)
        private val stadium: TextView = view.findViewById(R.id.tv_stadium)
        private val location: TextView = view.findViewById(R.id.tv_location)

        fun bind(item: GameToView) {
            team1.text = item.homeTeamName.ifEmpty { "Sin definir" }
            team2.text = item.awayTeamName.ifEmpty { "Sin definir" }
            result1.text = item.homeGoals?.toString()
                ?: context.getString(R.string.text_empty_score)
            result2.text = item.awayGoals?.toString()
                ?: context.getString(R.string.text_empty_score)
            date.text = Common.convertDateTimeToString(item.date)
            round.text = item.round
            stadium.text = item.stadiumName
            location.text = item.stadiumCity

            Glide.with(context)
                .load(item.flag1Res)
                .centerCrop()
                .circleCrop()
                .placeholder(R.drawable.ic_flag_24)
                .into(flag1)

            Glide.with(context)
                .load(item.flag2Res)
                .centerCrop()
                .circleCrop()
                .placeholder(R.drawable.ic_flag_24)
                .into(flag2)
        }
    }

    // ---- Slot view holder ----

    private inner class SlotViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvDate: TextView = view.findViewById(R.id.tv_slot_date)
        private val tvTitle: TextView = view.findViewById(R.id.tv_slot_title)
        private val tvStadium: TextView = view.findViewById(R.id.tv_slot_stadium)
        private val tvLocation: TextView = view.findViewById(R.id.tv_slot_location)

        fun bind(item: MatchScheduleSlot) {
            tvDate.text = Common.convertDateTimeToString(item.date)
            tvTitle.text = item.title
            tvStadium.text = item.venue.name
            tvLocation.text = item.venue.location
        }
    }

    // ---- DiffUtil ----

    private class BrowseByDateDiff(
        private val old: List<Row>,
        private val new: List<Row>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = old.size
        override fun getNewListSize(): Int = new.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val a = old[oldItemPosition]
            val b = new[newItemPosition]
            return when {
                a is Row.GameRow && b is Row.GameRow ->
                    a.gameToView.number == b.gameToView.number
                a is Row.SlotRow && b is Row.SlotRow ->
                    a.slot.matchNumber == b.slot.matchNumber
                else -> false
            }
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            old[oldItemPosition] == new[newItemPosition]
    }

    private companion object {
        const val VIEW_TYPE_GAME = 1
        const val VIEW_TYPE_SLOT = 2
    }
}
