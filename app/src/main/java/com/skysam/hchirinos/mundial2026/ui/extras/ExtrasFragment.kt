package com.skysam.hchirinos.mundial2026.ui.extras

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.skysam.hchirinos.mundial2026.R
import com.skysam.hchirinos.mundial2026.common.Constants
import com.skysam.hchirinos.mundial2026.databinding.FragmentExtrasBinding
import com.skysam.hchirinos.mundial2026.repositories.Auth
import com.skysam.hchirinos.mundial2026.ui.groups.GroupsActivity
import com.skysam.hchirinos.mundial2026.ui.playoff.PlayOffActivity
import com.skysam.hchirinos.mundial2026.ui.settings.SettingsActivity
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlin.collections.contains

@AndroidEntryPoint
class ExtrasFragment: Fragment() {
    private var _binding: FragmentExtrasBinding? = null
    private val binding get() = _binding!!
    @Inject
    lateinit var auth: Auth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExtrasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val canEdit = auth.getCurrentUser()?.email in setOf(
            Constants.USER_MAIN,
            Constants.USER_TEST
        )
        val unlockDate = LocalDate.of(2026, 6, 24)
        val today = LocalDate.now(ZoneId.systemDefault())
        val isUnlocked = !today.isBefore(unlockDate) || canEdit

        setPlayoffState(isUnlocked)

        binding.cardGroups.setOnClickListener {
            startActivity(Intent(requireContext(), GroupsActivity::class.java))
        }
        binding.cardPlayoff.setOnClickListener {
            if (isUnlocked) startActivity(Intent(requireContext(), PlayOffActivity::class.java))
            else Snackbar.make(binding.root, getString(R.string.text_playoff_locked), Snackbar.LENGTH_SHORT).show()
        }
        binding.cardBrowseByDate.setOnClickListener {
            findNavController().navigate(R.id.action_extras_to_browse_by_date)
        }
        binding.cardSettings.setOnClickListener {
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