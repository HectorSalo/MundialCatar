package com.skysam.hchirinos.mundialcatar.ui.commonView

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.skysam.hchirinos.mundialcatar.R
import com.skysam.hchirinos.mundialcatar.databinding.DialogEditResultsBinding
import com.skysam.hchirinos.mundialcatar.dataclass.GameUser
import com.skysam.hchirinos.mundialcatar.ui.predicts.PredictsViewModel

/**
 * Created by Hector Chirinos on 11/05/2022.
 */

class EditResultsDialog: DialogFragment() {
 private var _binding: DialogEditResultsBinding? = null
 private val binding get() = _binding!!
 private val viewModel: PredictsViewModel by activityViewModels()
 private lateinit var buttonPositive: Button
 private lateinit var game: GameUser

 override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
  _binding = DialogEditResultsBinding.inflate(layoutInflater)

  viewModel.game.observe(this.requireActivity()) {
   if (_binding != null) {
    game = it
    binding.tvTeam1.text = game.team1
    binding.tvTeam2.text = game.team2
    binding.etGoal1.setText(game.goalsTeam1.toString())
    binding.etGoal2.setText(game.goalsTeam2.toString())
   }
  }

  val builder = AlertDialog.Builder(requireActivity())
  builder.setTitle(getString(R.string.text_update_result))
   .setView(binding.root)
   .setPositiveButton(R.string.text_update, null)

  val dialog = builder.create()
  dialog.show()

  buttonPositive = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
  buttonPositive.setOnClickListener { dialog.dismiss() }
  return dialog
 }

 override fun onDestroyView() {
  super.onDestroyView()
  _binding = null
 }
}