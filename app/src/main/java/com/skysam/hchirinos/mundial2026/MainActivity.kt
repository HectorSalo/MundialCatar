package com.skysam.hchirinos.mundial2026

import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.skysam.hchirinos.mundial2026.common.Permission
import com.skysam.hchirinos.mundial2026.databinding.ActivityMainBinding
import com.skysam.hchirinos.mundial2026.repositories.Preferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (!isGranted) {
            Snackbar.make(
                binding.root,
                getString(R.string.error_permission_notification),
                Snackbar.LENGTH_SHORT
            )
                .setAnchorView(R.id.coordinator).show()
            lifecycleScope.launch {
                Preferences.changeNotificationStatus(isGranted)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupEdgeToEdge(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController
        navView.setupWithNavController(navController)

        // Aseguramos que al tocar "Rondas" siempre se aterrice en ExtrasFragment,
        // aun cuando el estado restaurado tenga un hijo (ej. BrowseByDateFragment) encima.
        navView.setOnItemSelectedListener { item ->
            val handled = NavigationUI.onNavDestinationSelected(item, navController)
            if (handled && item.itemId == R.id.navigation_extras) {
                navController.popBackStack(R.id.navigation_extras, false)
            }
            handled
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!Permission.checkPermissionNotification())
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }


    }
}