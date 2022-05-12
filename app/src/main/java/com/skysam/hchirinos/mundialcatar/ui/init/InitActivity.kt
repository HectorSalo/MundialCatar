package com.skysam.hchirinos.mundialcatar.ui.init

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.firebase.ui.auth.AuthUI
import com.skysam.hchirinos.mundialcatar.MainActivity
import com.skysam.hchirinos.mundialcatar.R
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.GameUser
import com.skysam.hchirinos.mundialcatar.dataclass.User
import com.skysam.hchirinos.mundialcatar.repositories.Auth

class InitActivity : AppCompatActivity() {
    private val viewModel: InitViewModel by viewModels()
    private val games = mutableListOf<Game>()
    private val users = mutableListOf<User>()

    private val requestIntentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            //CloudMessaging.subscribeTopicMessagingForUser()
            var exists = false
            for(user in users) {
                if (user.id == Auth.getCurrenUser()?.uid) exists = true
            }
            if (!exists) {
                val gamesUser = mutableListOf<GameUser>()
                for (ga in games) {
                    val newGame = GameUser(
                        ga.id,
                        ga.team1,
                        ga.team2,
                        ga.flag1,
                        ga.flag2,
                        ga.date,
                        ga.goalsTeam1,
                        ga.goalsTeam2,
                        ga.penal1,
                        ga.penal2,
                        ga.round,
                        ga.number
                    )
                    gamesUser.add(newGame)
                }
                val newUser = User(
                    Auth.getCurrenUser()!!.uid,
                    Auth.getCurrenUser()!!.displayName,
                    Auth.getCurrenUser()!!.photoUrl.toString(),
                    Auth.getCurrenUser()!!.email,
                    games = gamesUser
                )
                viewModel.createUser(newUser)
            }
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContentView(R.layout.activity_init)

        viewModel.games.observe(this) {
            games.clear()
            games.addAll(it)
        }
        viewModel.users.observe(this) {
            users.clear()
            users.addAll(it)
        }

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
                .setLogo(R.drawable.logo_catar)
                .setTheme(R.style.Theme_MundialCatar)
                .build())
    }
}