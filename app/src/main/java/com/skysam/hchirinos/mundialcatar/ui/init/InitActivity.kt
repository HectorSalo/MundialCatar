package com.skysam.hchirinos.mundialcatar.ui.init

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.firebase.ui.auth.AuthUI
import com.skysam.hchirinos.mundialcatar.MainActivity
import com.skysam.hchirinos.mundialcatar.R
import com.skysam.hchirinos.mundialcatar.common.CloudMessaging
import com.skysam.hchirinos.mundialcatar.databinding.ActivityInitBinding
import com.skysam.hchirinos.mundialcatar.dataclass.User
import com.skysam.hchirinos.mundialcatar.repositories.Auth
import com.skysam.hchirinos.mundialcatar.repositories.TeamsRespository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class InitActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInitBinding
    private val viewModel: InitViewModel by viewModels()
    @Inject
    lateinit var auth: Auth
    @Inject lateinit var teamsRepository: TeamsRespository

    private val requestIntentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            CloudMessaging.subscribeToNotifications()
            handleSignedInUser()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityInitBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (auth.getCurrentUser() == null) {
            startAuthUI()
        } else {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun startAuthUI() {
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build())

// Create and launch sign-in intent
        requestIntentLauncher.launch(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setCredentialManagerEnabled(false)
                .setLogo(R.drawable.logo)
                .setTheme(R.style.Theme_Generic)
                .build())
    }

    private fun handleSignedInUser() {
        val firebaseUser = auth.getCurrentUser()
        if (firebaseUser == null) {
            startAuthUI()
            return
        }

        val newUser = User(
            id = firebaseUser.uid,
            name = firebaseUser.displayName,
            image = firebaseUser.photoUrl?.toString(), // evitamos "null" como String
            email = firebaseUser.email,
            points = 0
        )

        lifecycleScope.launch {

            viewModel.ensureUserExists(newUser)
            goToMain()
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}