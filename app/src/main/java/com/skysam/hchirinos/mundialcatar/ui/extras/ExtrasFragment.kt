package com.skysam.hchirinos.mundialcatar.ui.extras

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.skysam.hchirinos.mundialcatar.databinding.FragmentExtrasBinding
import com.skysam.hchirinos.mundialcatar.ui.groups.GroupsActivity
import com.skysam.hchirinos.mundialcatar.ui.playoff.PlayOffActivity
import com.skysam.hchirinos.mundialcatar.ui.settings.SettingsActivity
import java.util.Calendar
import java.util.Date

class ExtrasFragment : Fragment() {

 private var _binding: FragmentExtrasBinding? = null
 private val binding get() = _binding!!

 override fun onCreateView(
  inflater: LayoutInflater, container: ViewGroup?,
  savedInstanceState: Bundle?
 ): View {
  _binding = FragmentExtrasBinding.inflate(inflater, container, false)
  return binding.root
 }

 override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
  super.onViewCreated(view, savedInstanceState)
  val calendar = Calendar.getInstance()
  calendar.set(Calendar.DAY_OF_MONTH, 17)
  calendar.set(Calendar.MONTH, 11)
  calendar.set(Calendar.YEAR, 2023)


  binding.cardGroups.setOnClickListener {
   startActivity(Intent(requireContext(), GroupsActivity::class.java))
  }
  binding.cardPlayoff.setOnClickListener {
   if (calendar.time.before(Date())) startActivity(Intent(requireContext(), PlayOffActivity::class.java))
   else Snackbar.make(binding.btnSettings, "Próximamente disponible", Snackbar.LENGTH_SHORT).show()
  }
  binding.btnSettings.setOnClickListener {
   startActivity(Intent(requireContext(), SettingsActivity::class.java))
  }
 }

 override fun onDestroyView() {
  super.onDestroyView()
  _binding = null
 }

}