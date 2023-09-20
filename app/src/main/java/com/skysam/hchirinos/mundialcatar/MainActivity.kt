package com.skysam.hchirinos.mundialcatar

import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.skysam.hchirinos.mundialcatar.common.Permission
import com.skysam.hchirinos.mundialcatar.databinding.ActivityMainBinding
import com.skysam.hchirinos.mundialcatar.repositories.Preferences
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

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

        val navView: BottomNavigationView = binding.navView

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController
        navView.setupWithNavController(navController)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!Permission.checkPermissionReadStorage())
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }


    }
}