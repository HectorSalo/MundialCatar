package com.skysam.hchirinos.mundialcatar.ui.commonView

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.skysam.hchirinos.mundialcatar.R
import com.skysam.hchirinos.mundialcatar.common.Common
import com.skysam.hchirinos.mundialcatar.databinding.DialogEditResultsBinding
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.GameUser
import com.skysam.hchirinos.mundialcatar.ui.gameday.GamedayViewModel
import com.skysam.hchirinos.mundialcatar.ui.predicts.PredictsViewModel

/**
 * Created by Hector Chirinos on 11/05/2022.
 */

class EditResultsDialog: DialogFragment() {
 private var _binding: DialogEditResultsBinding? = null
 private val binding get() = _binding!!
 private val predictsViewModel: PredictsViewModel by activityViewModels()
 private val gamedayViewModel: GamedayViewModel by activityViewModels()
 private lateinit var buttonPositive: Button
 private var gameUser: GameUser? = null
 private var game: Game? = null
 private var number = 0

 override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
  _binding = DialogEditResultsBinding.inflate(layoutInflater)

  predictsViewModel.gameUser.observe(this.requireActivity()) {
   if (_binding != null && it != null) {
    gameUser = it
    binding.tvTeam1.text = gameUser?.team1
    binding.tvTeam2.text = gameUser?.team2
    binding.etGoal1.setText(gameUser?.goalsTeam1.toString())
    binding.etGoal2.setText(gameUser?.goalsTeam2.toString())
    number = gameUser?.number!!

    if (gameUser?.number !in 49..64) {
     binding.tfPenal1.visibility = View.GONE
     binding.tfPenal2.visibility = View.GONE
    }
   }
  }

  gamedayViewModel.game.observe(this.requireActivity()) {
   if (_binding != null && it != null) {
    game = it
    binding.tvTeam1.text = game?.team1
    binding.tvTeam2.text = game?.team2
    binding.etGoal1.setText(game?.goalsTeam1.toString())
    binding.etGoal2.setText(game?.goalsTeam2.toString())
    number = game?.number!!

    if (game?.number !in 49..64) {
     binding.tfPenal1.visibility = View.GONE
     binding.tfPenal2.visibility = View.GONE
    }
   }
  }

  if (number > 48) {
   binding.etGoal1.doAfterTextChanged {
    if (it.toString().trim().isNotEmpty()) {
     if (it.toString().trim().toInt() > 0 || binding.etGoal2.text.toString().toInt() > 0) {
      binding.tfPenal1.visibility = View.GONE
      binding.tfPenal2.visibility = View.GONE
     } else {
      binding.tfPenal1.visibility = View.VISIBLE
      binding.tfPenal2.visibility = View.VISIBLE
     }
    }
   }

   binding.etGoal2.doAfterTextChanged {
    if (it.toString().trim().isNotEmpty()) {
     if (it.toString().trim().toInt() > 0 || binding.etGoal1.text.toString().toInt() > 0) {
      binding.tfPenal1.visibility = View.GONE
      binding.tfPenal2.visibility = View.GONE
     } else {
      binding.tfPenal1.visibility = View.VISIBLE
      binding.tfPenal2.visibility = View.VISIBLE
     }
    }
   }
  }

  val builder = AlertDialog.Builder(requireActivity())
  builder.setTitle(getString(R.string.text_update_result))
   .setView(binding.root)
   .setPositiveButton(R.string.text_update, null)

  val dialog = builder.create()
  dialog.show()

  buttonPositive = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
  buttonPositive.setOnClickListener { validateData() }
  return dialog
 }

 private fun validateData() {
  if (binding.etGoal1.text.toString().isEmpty() || binding.etGoal2.text.toString().isEmpty()) {
   Toast.makeText(requireContext(), "No puede dejar el marcador vacío", Toast.LENGTH_SHORT).show()
   return
  }

  Common.closeKeyboard(binding.root)
  if (game != null) {
   val newG = Game(
    game!!.id,
    game!!.team1,
    game!!.team2,
    game!!.flag1,
    game!!.flag2,
    game!!.date,
    game!!.stadium,
    binding.etGoal1.text.toString().toInt(),
    binding.etGoal2.text.toString().toInt(),
    binding.etPenal1.text.toString().toInt(),
    binding.etPenal2.text.toString().toInt(),
    game!!.round,
    game!!.number,
    game!!.gameTo,
    game!!.positionTo,
    game!!.started
   )
   gamedayViewModel.updateResultGame(newG)
  }

  if (gameUser != null) {
   val newG = GameUser(
    gameUser!!.id,
    gameUser!!.team1,
    gameUser!!.team2,
    gameUser!!.flag1,
    gameUser!!.flag2,
    gameUser!!.date,
    binding.etGoal1.text.toString().toInt(),
    binding.etGoal2.text.toString().toInt(),
    binding.etPenal1.text.toString().toInt(),
    binding.etPenal2.text.toString().toInt(),
    gameUser!!.round,
    gameUser!!.number,
    gameUser!!.points
   )
   predictsViewModel.updatePredict(newG)
  }
  dismiss()
 }

 override fun onDestroyView() {
  super.onDestroyView()
  predictsViewModel.editPredict(null)
  gamedayViewModel.editGame(null)
  _binding = null
 }
}