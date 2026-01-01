package com.skysam.hchirinos.mundialcatar.ui.extras

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.skysam.hchirinos.mundialcatar.R
import com.skysam.hchirinos.mundialcatar.databinding.FragmentExtrasBinding
import com.skysam.hchirinos.mundialcatar.ui.groups.GroupsActivity
import com.skysam.hchirinos.mundialcatar.ui.playoff.PlayOffActivity
import com.skysam.hchirinos.mundialcatar.ui.settings.SettingsActivity
import java.time.LocalDate
import java.time.ZoneId


class ExtrasFragment: Fragment() {
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
        val unlockDate = LocalDate.of(2026, 6, 24)
        val today = LocalDate.now(ZoneId.systemDefault())
        val isUnlocked = !today.isBefore(unlockDate)

        setPlayoffState(isUnlocked)

        binding.cardGroups.setOnClickListener {
            startActivity(Intent(requireContext(), GroupsActivity::class.java))
        }
        binding.cardPlayoff.setOnClickListener {
            if (isUnlocked) startActivity(Intent(requireContext(), PlayOffActivity::class.java))
            else Snackbar.make(binding.btnSettings, getString(R.string.text_playoff_locked), Snackbar.LENGTH_SHORT).show()
        }
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setPlayoffState(isUnlocked: Boolean) {
        binding.cardPlayoff.alpha = if (isUnlocked) 1f else 0.6f
        binding.ivPlayoffLock.visibility = if (isUnlocked) View.GONE else View.VISIBLE
        binding.tvSubtitlePlayoff.visibility = if (isUnlocked) View.GONE else View.VISIBLE
    }
}