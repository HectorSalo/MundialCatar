package com.skysam.hchirinos.mundialcatar.ui.extras

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.skysam.hchirinos.mundialcatar.databinding.FragmentExtrasBinding
import com.skysam.hchirinos.mundialcatar.ui.groups.GroupsActivity
import com.skysam.hchirinos.mundialcatar.ui.playoff.PlayOffActivity
import com.skysam.hchirinos.mundialcatar.ui.settings.SettingsActivity

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
  binding.btnGroups.setOnClickListener {
   startActivity(Intent(requireContext(), GroupsActivity::class.java))
  }
  binding.btnGames.setOnClickListener {
   startActivity(Intent(requireContext(), PlayOffActivity::class.java))
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