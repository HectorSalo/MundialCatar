package com.skysam.hchirinos.mundial2026.ui.commonView

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.skysam.hchirinos.mundial2026.R
import com.skysam.hchirinos.mundial2026.common.Common
import com.skysam.hchirinos.mundial2026.databinding.DialogEditResultsBinding
import com.skysam.hchirinos.mundial2026.dataclass.Game
import com.skysam.hchirinos.mundial2026.dataclass.GamePredictionEntity
import com.skysam.hchirinos.mundial2026.dataclass.GameScore
import com.skysam.hchirinos.mundial2026.repositories.Auth
import com.skysam.hchirinos.mundial2026.ui.gameday.GamedayViewModel
import com.skysam.hchirinos.mundial2026.ui.predicts.PredictsViewModel
import javax.inject.Inject

/**
 * Created by Hector Chirinos on 11/05/2022.
 */

class EditResultsDialog : DialogFragment() {
    private var _binding: DialogEditResultsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PredictsViewModel by activityViewModels()
    private val viewModelGameday: GamedayViewModel by activityViewModels()
    @Inject
    lateinit var auth: Auth
    private lateinit var buttonPositive: Button
    private lateinit var game: Game
    private var games = listOf<GamePredictionEntity>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogEditResultsBinding.inflate(layoutInflater)

        val builder = MaterialAlertDialogBuilder(requireActivity())
        builder.setTitle(getString(R.string.text_update_result))
            .setView(binding.root)
            .setPositiveButton(R.string.text_update, null)

        val dialog = builder.create()
        dialog.setOnShowListener {
            buttonPositive = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
            buttonPositive.setOnClickListener { validateData() }
        }

        subscribeObservers()

        return dialog
    }

    private fun subscribeObservers() {
        viewModel.gamesUser.observe(this.requireActivity()) {
            if (_binding != null) {
                games = it
            }
        }
        viewModelGameday.game.observe(this.requireActivity()) {
            if (_binding != null) {
                game = it
                binding.tvTeam1.text = game.homeTeamId
                binding.tvTeam2.text = game.awayTeamId
                val score = game.score
                binding.etGoal1.setText(score?.homeGoals?.toString() ?: "")
                binding.etGoal2.setText(score?.awayGoals?.toString() ?: "")
            }
        }
    }

    private fun validateData() {
        val goals1Text = binding.etGoal1.text.toString().trim()
        val goals2Text = binding.etGoal2.text.toString().trim()

        if (goals1Text.isEmpty() || goals2Text.isEmpty()) {
            Toast.makeText(requireContext(), "No puede dejar el marcador vacío", Toast.LENGTH_SHORT)
                .show()
            return
        }

        val goals1 = goals1Text.toInt()
        val goals2 = goals2Text.toInt()

        Common.closeKeyboard(binding.root)
        val oldScore = game.score
        val newScore = oldScore?.copy(
            homeGoals = goals1,
            awayGoals = goals2
        )
            ?: GameScore(
                homeGoals = goals1,
                awayGoals = goals2
            )

        // AQUÍ usas la firma real del ViewModel:
        viewModelGameday.setResultGame(
            gameId = game.id,
            score = newScore
        )
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}