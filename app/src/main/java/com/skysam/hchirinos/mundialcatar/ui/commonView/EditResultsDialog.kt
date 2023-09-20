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
import com.skysam.hchirinos.mundialcatar.dataclass.GameUser
import com.skysam.hchirinos.mundialcatar.dataclass.User
import com.skysam.hchirinos.mundialcatar.repositories.Auth
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
 private lateinit var gameUser: GameUser
 private lateinit var game: Game
 private val games = mutableListOf<GameUser>()
 private var users = listOf<User>()

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
   viewModel.getGamesByUser(Auth.getCurrenUser()!!.uid).observe(this.requireActivity()) {
    if (_binding != null) {
     games.clear()
     games.addAll(it)
    }
   }
  if (!isGameday) {
   viewModel.gameUser.observe(this.requireActivity()) {
    if (_binding != null) {
     gameUser = it
     binding.tvTeam1.text = gameUser.team1
     binding.tvTeam2.text = gameUser.team2
     binding.etGoal1.setText(gameUser.goals1.toString())
     binding.etGoal2.setText(gameUser.goals2.toString())
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
   viewModelGameday.users.observe(this.requireActivity()) {
    users = it
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
   val gamesToSend = mutableListOf<GameUser>()
   for (gm in games) {
    val newG = if (gameUser.number == gm.number) {
     GameUser(
      gameUser.team1,
      gameUser.team2,
      gameUser.date,
      goals1.toInt(),
      goals2.toInt(),
      "",
      "",
      gameUser.round,
      gameUser.number,
      gameUser.points,
      true
     )
    } else {
     GameUser(
      gm.team1,
      gm.team2,
      gm.date,
      gm.goals1,
      gm.goals2,
      "",
      "",
      gm.round,
      gm.number,
      gm.points,
      gm.predict
     )
    }
    gamesToSend.add(newG)
   }
   viewModel.updatePredict(gamesToSend)
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
   viewModelGameday.setResultGame(newG, users)
  }
  dismiss()
 }

 override fun onDestroyView() {
  super.onDestroyView()
  _binding = null
 }
}