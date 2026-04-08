package com.skysam.hchirinos.mundial2026.ui.predicts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.color.MaterialColors
import com.skysam.hchirinos.mundial2026.R
import com.skysam.hchirinos.mundial2026.common.Common
import com.skysam.hchirinos.mundial2026.databinding.LayoutPredictGameItemBinding
import com.skysam.hchirinos.mundial2026.dataclass.GameToView

/**
 * Created by Hector Chirinos on 11/05/2022.
 */

class PredictsAdapter(
    private val onGameClick: (GameToView) -> Unit,
    private val onDraftChange: (matchNumber: Int, home: Int, away: Int) -> Unit,
    private val onDraftClear: (matchNumber: Int) -> Unit,          // NUEVO
    private val onSaveClick: (matchNumber: Int) -> Unit
) : RecyclerView.Adapter<PredictsAdapter.ViewHolder>() {
    private var games = listOf<GameToView>()
    private var canEditByMatch: Map<Int, Boolean> = emptyMap()
    private var draftCache: Map<Int, Pair<Int, Int>> = emptyMap()
    private var syncStateByMatch: Map<Int, SyncUiState> = emptyMap()

    companion object {
        private const val PAYLOAD_DRAFT = "payload_draft"
        private const val PAYLOAD_SYNC = "payload_sync"
    }

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = games[position].number.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutPredictGameItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindFull(games[position])
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        val item = games[position]

        val hasDraftPayload = payloads.contains(PAYLOAD_DRAFT)
        val hasSyncPayload = payloads.contains(PAYLOAD_SYNC)

        when {
            hasDraftPayload && hasSyncPayload -> {
                holder.bindDraftOnly(item)
                holder.bindSyncOnly(item)
            }
            hasDraftPayload -> holder.bindDraftOnly(item)
            hasSyncPayload -> holder.bindSyncOnly(item)
            else -> holder.bindFull(item)
        }
    }

    override fun getItemCount(): Int = games.size

    inner class ViewHolder(
        private val binding: LayoutPredictGameItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bindFull(item: GameToView) = with(binding) {
            val ctx = root.context

            tvTeam1.text = item.homeTeamName
            tvTeam2.text = item.awayTeamName
            tvDate.text = Common.convertDateTimeToString(item.date)

            val canEdit = canEditByMatch[item.number] ?: true

            // UI de draft (incluye visibilidad +/- y btnSave)
            applyDraftUI(item, canEdit)

            // Sync state (lock/saving/pending/error)
            applySyncUI(item, canEdit)

            piSync.isIndeterminate = true

            // Meta + estilo (sin tocar resultados aquí)
            if (item.hasPrediction) {
                tvMeta.text = ctx.getString(R.string.text_points_predict, item.points)
                card.strokeColor = ContextCompat.getColor(ctx, R.color.garnet_normal)
                card.alpha = 1f
            } else {
                tvMeta.text = ctx.getString(R.string.text_not_predict)
                card.strokeColor = MaterialColors.getColor(card, com.google.android.material.R.attr.colorOutline)
                card.alpha = 0.85f
            }

            // Imágenes SOLO en full bind
            Glide.with(ctx)
                .load(item.flag1Res)
                .centerCrop()
                .circleCrop()
                .placeholder(R.drawable.ic_flag_24)
                .into(ivFlag1)

            Glide.with(ctx)
                .load(item.flag2Res)
                .centerCrop()
                .circleCrop()
                .placeholder(R.drawable.ic_flag_24)
                .into(ivFlag2)

            // Listeners
            btnPlus1.setOnClickListener { onPlusPressed(item, TeamSide.HOME) }
            btnMinus1.setOnClickListener { onMinusPressed(item, TeamSide.HOME) }
            btnPlus2.setOnClickListener { onPlusPressed(item, TeamSide.AWAY) }
            btnMinus2.setOnClickListener { onMinusPressed(item, TeamSide.AWAY) }

            btnSave.setOnClickListener { onSaveClick(item.number) }

            // Reintentar en error sin invadir (por ahora reusamos onSaveClick)
            ivError.setOnClickListener { onSaveClick(item.number) }

            card.isCheckable = true
            card.isFocusable = true
            card.setOnClickListener { onGameClick(item) }
        }

        fun bindDraftOnly(item: GameToView) = with(binding) {
            val canEdit = canEditByMatch[item.number] ?: true
            applyDraftUI(item, canEdit)
        }

        fun bindSyncOnly(item: GameToView) = with(binding) {
            val canEdit = canEditByMatch[item.number] ?: true
            applyDraftUI(item, canEdit)
            applySyncUI(item, canEdit)
            piSync.isIndeterminate = true
        }

        private fun applyDraftUI(item: GameToView, canEdit: Boolean) = with(binding) {
            val ctx = root.context

            val draft = draftCache[item.number]
            val isUndefined = (!item.hasPrediction && draft == null)

            // Visibilidad de botones MINUS según estado
            btnMinus1.visibility = if (isUndefined) View.GONE else View.VISIBLE
            btnMinus2.visibility = if (isUndefined) View.GONE else View.VISIBLE

            // PLUS siempre visible (pero puede quedar deshabilitado si canEdit=false o si está syncing)
            btnPlus1.visibility = View.VISIBLE
            btnPlus2.visibility = View.VISIBLE

            // Si está guardando/pendiente, se deshabilita interacción (sin bloquear toda la lista)
            val sync = syncStateByMatch[item.number] ?: SyncUiState.Idle
            val interactionEnabled = canEdit && sync.allowsEditing()

            setStepperEnabled(btnMinus1, tvResult1, btnPlus1, enabled = interactionEnabled, minusVisible = !isUndefined)
            setStepperEnabled(btnMinus2, tvResult2, btnPlus2, enabled = interactionEnabled, minusVisible = !isUndefined)

            if (isUndefined) {
                tvResult1.text = ctx.getString(R.string.text_empty_score) // "—"
                tvResult2.text = ctx.getString(R.string.text_empty_score) // "—"
                btnSave.visibility = View.GONE
                return@with
            }

            // Si llegamos aquí: hay draft o hay predicción persistida
            val baseHome = item.homeGoals
            val baseAway = item.awayGoals

            val shownHome = draft?.first ?: baseHome
            val shownAway = draft?.second ?: baseAway

            tvResult1.text = shownHome.toString()
            tvResult2.text = shownAway.toString()

            // Dirty:
            // - Si había predicción: dirty solo si difiere del persistido
            // - Si NO había predicción: cualquier draft (incluye 0-0) es guardable
            val isDirty = if (item.hasPrediction) {
                (shownHome != baseHome) || (shownAway != baseAway)
            } else {
                draft != null
            }

            // Guardar solo si hay cambios, se puede editar y NO está en saving/pending
            val showSave = isDirty && canEdit && sync.allowsSaving()
            btnSave.visibility = if (showSave) View.VISIBLE else View.GONE
        }

        private fun applySyncUI(item: GameToView, canEdit: Boolean) = with(binding) {
            val ctx = root.context
            val state = syncStateByMatch[item.number] ?: SyncUiState.Idle

            // Reset base
            ivLock.visibility = View.GONE
            piSync.visibility = View.GONE
            ivError.visibility = View.GONE
            tvSyncState.visibility = View.GONE

            if (!canEdit) {
                // Bloqueado: solo candado, sin mensajes extra
                ivLock.visibility = View.VISIBLE
                return@with
            }

            when (state) {
                SyncUiState.Idle -> {
                    // Éxito silencioso: no mostrar nada
                }

                SyncUiState.Saving -> {
                    piSync.visibility = View.VISIBLE
                    tvSyncState.visibility = View.VISIBLE
                    tvSyncState.text = ctx.getString(R.string.text_saving)
                }

                SyncUiState.Pending -> {
                    piSync.visibility = View.VISIBLE
                    tvSyncState.visibility = View.VISIBLE
                    tvSyncState.text = ctx.getString(R.string.text_pending_sync)
                }

                is SyncUiState.Error -> {
                    ivError.visibility = View.VISIBLE
                    tvSyncState.visibility = View.VISIBLE
                    tvSyncState.text = ctx.getString(R.string.text_save_failed)
                }
            }
        }

        private fun setStepperEnabled(
            minus: View,
            score: View,
            plus: View,
            enabled: Boolean,
            minusVisible: Boolean
        ) {
            minus.isEnabled = enabled && minusVisible
            plus.isEnabled = enabled

            val alphaPlus = if (enabled) 1f else 0.35f
            plus.alpha = alphaPlus

            if (minusVisible) {
                val alphaMinus = if (enabled) 1f else 0.35f
                minus.alpha = alphaMinus
            } else {
                minus.alpha = 0f
            }

            score.alpha = if (enabled) 1f else 0.70f
        }

        private fun onPlusPressed(item: GameToView, team: TeamSide) {
            val match = item.number
            val canEdit = canEditByMatch[match] ?: true
            if (!canEdit) return

            // Si está guardando/pendiente, no dejamos editar
            val sync = syncStateByMatch[match] ?: SyncUiState.Idle
            if (!sync.allowsEditing()) return

            val currentDraft = draftCache[match]

            // Caso "sin definir": primer PLUS solo inicializa a 0-0 (sin incrementar)
            if (!item.hasPrediction && currentDraft == null) {
                emitDraft(match, 0, 0)
                return
            }

            // Caso normal: incrementa el lado correspondiente
            val baseHome = item.homeGoals
            val baseAway = item.awayGoals

            val (home, away) = currentDraft ?: (baseHome to baseAway)
            val newHome = if (team == TeamSide.HOME) home!! + 1 else home
            val newAway = if (team == TeamSide.AWAY) away!! + 1 else away

            emitDraft(match, newHome!!, newAway!!)
        }

        private fun onMinusPressed(item: GameToView, team: TeamSide) {
            val match = item.number
            val canEdit = canEditByMatch[match] ?: true
            if (!canEdit) return

            // Si está guardando/pendiente, no dejamos editar
            val sync = syncStateByMatch[match] ?: SyncUiState.Idle
            if (!sync.allowsEditing()) return

            val currentDraft = draftCache[match] ?: run {
                // Si no hay draft, entonces el usuario tiene predicción (porque en "sin definir" ocultamos minus)
                if (!item.hasPrediction) return
                item.homeGoals to item.awayGoals
            }

            val (home, away) = currentDraft

            // Si NO había predicción y está en 0-0: MINUS debe volver a "- vs -" (clear draft)
            if (!item.hasPrediction && home == 0 && away == 0) {
                emitClearDraft(match)
                return
            }

            // Si había predicción: jamás permitimos "sin predicción". Clamp a 0.
            val newHome = if (team == TeamSide.HOME) (home!! - 1).coerceAtLeast(0) else home
            val newAway = if (team == TeamSide.AWAY) (away!! - 1).coerceAtLeast(0) else away

            if (newHome == home && newAway == away) return
            emitDraft(match, newHome!!, newAway!!)
        }
        private fun emitDraft(match: Int, home: Int, away: Int) {
            // 1) Cache local inmediato (sin esperar LiveData)
            draftCache = draftCache.toMutableMap().apply { put(match, home to away) }
            notifyDraftChanged(match)

            // 2) Fuente real: ViewModel
            onDraftChange(match, home, away)
        }

        private fun emitClearDraft(match: Int) {
            // 1) Cache local inmediato
            draftCache = draftCache.toMutableMap().apply { remove(match) }
            notifyDraftChanged(match)

            // 2) Fuente real: ViewModel
            onDraftClear(match)
        }
    }

    private enum class TeamSide { HOME, AWAY }

    // -----------------------------------------
    // Public API
    // -----------------------------------------

    fun updateList(newList: List<GameToView>) {
        val diffUtil = PredictDiffUtil(games, newList)
        val result = DiffUtil.calculateDiff(diffUtil)
        games = newList
        result.dispatchUpdatesTo(this)
    }

    fun updateEditabilityMap(map: Map<Int, Boolean>) {
        canEditByMatch = map
        notifyItemRangeChanged(0, itemCount)
    }
    fun setDraftCache(map: Map<Int, Pair<Int, Int>>) {
        draftCache = map
    }

    fun notifyDraftChanged(matchNumber: Int) {
        val index = games.indexOfFirst { it.number == matchNumber }
        if (index != -1) notifyItemChanged(index, PAYLOAD_DRAFT)
    }
    fun setSyncStateMap(map: Map<Int, SyncUiState>) {
        syncStateByMatch = map
    }

    fun notifySyncChanged(matchNumber: Int) {
        val index = games.indexOfFirst { it.number == matchNumber }
        if (index != -1) notifyItemChanged(index, PAYLOAD_SYNC)
    }

    // -----------------------------------------
    // Sync UI state
    // -----------------------------------------

    sealed class SyncUiState {
        data object Idle : SyncUiState()
        data object Saving : SyncUiState()
        data object Pending : SyncUiState()
        data class Error(val message: String? = null) : SyncUiState()

        fun allowsEditing(): Boolean = this is Idle || this is Error
        fun allowsSaving(): Boolean = this is Idle || this is Error
    }
}

