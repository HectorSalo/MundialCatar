package com.skysam.hchirinos.mundialcatar.ui.commonView

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.skysam.hchirinos.mundialcatar.R
import com.skysam.hchirinos.mundialcatar.common.Common
import com.skysam.hchirinos.mundialcatar.databinding.DialogEditResultsBinding
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.GameToView
import com.skysam.hchirinos.mundialcatar.dataclass.GamePredictionEntity
import com.skysam.hchirinos.mundialcatar.dataclass.GameScore
import com.skysam.hchirinos.mundialcatar.repositories.Auth
import com.skysam.hchirinos.mundialcatar.ui.gameday.GamedayViewModel
import com.skysam.hchirinos.mundialcatar.ui.predicts.PredictsViewModel
import java.util.Date
import javax.inject.Inject

/**
 * Created by Hector Chirinos on 11/05/2022.
 */

class EditResultsDialog(private val isGameday: Boolean) : DialogFragment() {
    private var _binding: DialogEditResultsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PredictsViewModel by activityViewModels()
    private val viewModelGameday: GamedayViewModel by activityViewModels()
    @Inject
    lateinit var auth: Auth
    private lateinit var buttonPositive: Button
    private lateinit var gameToView: GameToView
    private lateinit var game: Game
    private var games = listOf<GamePredictionEntity>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogEditResultsBinding.inflate(layoutInflater)

        val builder = AlertDialog.Builder(requireActivity())
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
        if (!isGameday) {
            viewModel.gameUser.observe(this.requireActivity()) {
                if (_binding != null) {
                    gameToView = it
                    binding.tvTeam1.text = gameToView.homeTeamName
                    binding.tvTeam2.text = gameToView.awayTeamName
                    binding.etGoal1.setText(gameToView.homeGoals.toString())
                    binding.etGoal2.setText(gameToView.awayGoals.toString())
                }
            }
        } else {
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
        if (!isGameday) {
            val currentGameId = gameToView.gameId
            val currentTournamentId = gameToView.tournamentId
            val currentMatchNumber = gameToView.number
            val existingPrediction = games.find { it.gameId == currentGameId }
            val now = Date()

            if (existingPrediction != null) {
                // Actualizamos la predicción existente
                val prediction = existingPrediction.copy(
                    predictedHomeGoals = goals1,
                    predictedAwayGoals = goals2,
                    points = gameToView.points,
                    updatedAt = now
                )
                viewModel.updatePredict(prediction)
            } else {
                // Creamos una nueva predicción
                val newId = "${auth.getCurrentUser()!!.uid}_${currentMatchNumber}" // o el esquema que uses

                val prediction = GamePredictionEntity(
                    id = newId,
                    userId = auth.getCurrentUser()!!.uid,
                    gameId = currentGameId,
                    tournamentId = currentTournamentId,
                    matchNumber = currentMatchNumber,
                    predictedHomeGoals = goals1,
                    predictedAwayGoals = goals2,
                    points = gameToView.points,
                    createdAt = now,
                    updatedAt = now
                )

                viewModel.createPredict(prediction)
            }
        } else {
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
        }
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}