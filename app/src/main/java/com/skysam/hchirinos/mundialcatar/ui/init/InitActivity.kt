package com.skysam.hchirinos.mundialcatar.ui.init

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.firebase.ui.auth.AuthUI
import com.skysam.hchirinos.mundialcatar.MainActivity
import com.skysam.hchirinos.mundialcatar.R
import com.skysam.hchirinos.mundialcatar.common.CloudMessaging
import com.skysam.hchirinos.mundialcatar.dataclass.GameUser
import com.skysam.hchirinos.mundialcatar.dataclass.User
import com.skysam.hchirinos.mundialcatar.repositories.Auth

class InitActivity : AppCompatActivity() {
    private val viewModel: InitViewModel by viewModels()

    private val requestIntentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            CloudMessaging.subscribeToNotifications()
            viewModel.users.observe(this) {users ->
                viewModel.games.observe(this) {games ->
                    var exists = false
                    for(user in users) {
                        if (user.id == Auth.getCurrenUser()?.uid) exists = true
                    }
                    if (!exists) {
                        val gamesToUser = mutableListOf<GameUser>()
                        for (game in games) {
                            val newGame = GameUser(
                                game.team1,
                                game.team2,
                                game.date,
                                0,
                                0,
                                round = game.round,
                                number = game.number,
                                points = 0,
                            )
                            gamesToUser.add(newGame)
                        }
                        val newUser = User(
                            Auth.getCurrenUser()!!.uid,
                            Auth.getCurrenUser()!!.displayName,
                            Auth.getCurrenUser()!!.photoUrl.toString(),
                            Auth.getCurrenUser()!!.email,
                            0,
                            gamesToUser
                        )
                        viewModel.createUser(newUser)
                    }
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContentView(R.layout.activity_init)


        if (Auth.getCurrenUser() == null) {
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
                .setIsSmartLockEnabled(false)
                .setLogo(R.drawable.logo)
                .setTheme(R.style.Theme_Generic)
                .build())
    }
}