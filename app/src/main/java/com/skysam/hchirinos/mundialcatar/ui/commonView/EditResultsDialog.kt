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
import com.skysam.hchirinos.mundialcatar.dataclass.GameUser
import com.skysam.hchirinos.mundialcatar.ui.gameday.GamedayViewModel
import com.skysam.hchirinos.mundialcatar.ui.predicts.PredictsViewModel

/**
 * Created by Hector Chirinos on 11/05/2022.
 */

class EditResultsDialog(private val isGameday: Boolean): DialogFragment() {
 private var _binding: DialogEditResultsBinding? = null
 private val binding get() = _binding!!
 private val viewModel: PredictsViewModel by activityViewModels()
 private val viewModelGameday: GamedayViewModel by activityViewModels()
 private lateinit var buttonPositive: Button
 private lateinit var gameToView: GameToView
 private lateinit var game: Game
 private var games = listOf<GameUser>()

 override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
  _binding = DialogEditResultsBinding.inflate(layoutInflater)

  val builder = AlertDialog.Builder(requireActivity())
  builder.setTitle(getString(R.string.text_update_result))
   .setView(binding.root)
   .setPositiveButton(R.string.text_update, null)

  val dialog = builder.create()
  dialog.show()

  buttonPositive = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
  buttonPositive.setOnClickListener { validateData() }

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
     binding.tvTeam1.text = gameToView.team1
     binding.tvTeam2.text = gameToView.team2
     binding.etGoal1.setText(gameToView.goalsTeam1.toString())
     binding.etGoal2.setText(gameToView.goalsTeam2.toString())
    }
   }
  } else {
   viewModelGameday.game.observe(this.requireActivity()) {
    if (_binding != null) {
     game = it
     binding.tvTeam1.text = game.team1
     binding.tvTeam2.text = game.team2
     binding.etGoal1.setText(game.goalsTeam1.toString())
     binding.etGoal2.setText(game.goalsTeam2.toString())
    }
   }
  }
 }

 private fun validateData() {
  val goals1 = binding.etGoal1.text.toString()
  val goals2 = binding.etGoal2.text.toString()

  if (goals1.isEmpty() || goals2.isEmpty()) {
   Toast.makeText(requireContext(), "No puede dejar el marcador vacío", Toast.LENGTH_SHORT).show()
   return
  }

  Common.closeKeyboard(binding.root)
  if (!isGameday) {
   var exists = false
   var id = ""
   var idUser = ""
   for (gm in games) {
    if (gm.number == gameToView.number) {
     id = gm.id
     idUser = gm.idUser
     exists = true
     break
    }
   }
   val gameUser = GameUser(
    id,
    idUser,
    goals1.toInt(),
    goals2.toInt(),
    gameToView.number,
    gameToView.points
   )

   if (exists) viewModel.updatePredict(gameUser) else viewModel.createPredict(gameUser)
  } else {
   val newG = Game(
    game.id,
    game.team1,
    game.team2,
    game.date,
    goals1.toInt(),
    goals2.toInt(),
    game.round,
    game.number,
    game.started
   )
   viewModelGameday.setResultGame(newG)
  }
  dismiss()
 }

 override fun onDestroyView() {
  super.onDestroyView()
  _binding = null
 }
}