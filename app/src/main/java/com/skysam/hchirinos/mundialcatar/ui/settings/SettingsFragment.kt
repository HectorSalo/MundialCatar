package com.skysam.hchirinos.mundialcatar.ui.settings

import android.content.Intent
import android.os.Bundle
import android.text.SpannedString
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.italic
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreferenceCompat
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.skysam.hchirinos.mundialcatar.BuildConfig
import com.skysam.hchirinos.mundialcatar.R
import com.skysam.hchirinos.mundialcatar.common.CloudMessaging
import com.skysam.hchirinos.mundialcatar.repositories.Auth
import com.skysam.hchirinos.mundialcatar.ui.init.InitActivity
import kotlinx.coroutines.launch

class SettingsFragment : PreferenceFragmentCompat(), MenuProvider {
    private val viewModel: SettingsViewModel by activityViewModels()
    private lateinit var switchNotification: SwitchPreferenceCompat
    private var statusNotification = true

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        requireActivity().addMenuProvider(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        switchNotification = findPreference(getString(R.string.notification_key))!!

        switchNotification.setOnPreferenceChangeListener { _, newValue ->
            val isOn = newValue as Boolean
            lifecycleScope.launch {
                viewModel.changeNotificationStatus(isOn)
            }
            true
        }

        loadViewModels()

        val rules: PreferenceScreen = findPreference("rules")!!
        rules.setOnPreferenceClickListener {
            dialogRules()
            true
        }

        val signOutPreference: PreferenceScreen = findPreference("signOut")!!
        signOutPreference.setOnPreferenceClickListener {
            signOut()
            true
        }

        val aboutPreference: PreferenceScreen = findPreference("about")!!
        aboutPreference.setOnPreferenceClickListener {
            NavHostFragment.findNavController(this)
                .navigate(R.id.action_settingsFragment_to_aboutFragment)
            true
        }

        val share: PreferenceScreen = findPreference(getString(R.string.share_key))!!
        share.setOnPreferenceClickListener {
            val appPackageName = requireContext().packageName
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(
                Intent.EXTRA_TEXT,
                "Disfruta de esta aplicación: https://play.google.com/store/apps/details?id=$appPackageName"
            )
            sendIntent.type = "text/plain"
            startActivity(sendIntent)
            true
        }

        val versionPreferenceScreen = findPreference<PreferenceScreen>("name_version")
        versionPreferenceScreen?.title = getString(R.string.version_name, BuildConfig.VERSION_NAME)
    }

    private fun loadViewModels() {
        viewModel.notificationActive.observe(viewLifecycleOwner) {
            statusNotification = it
            switchNotification.isChecked = it
            val icon = if (it) R.drawable.ic_notifications_active_24 else R.drawable.ic_notifications_off_24
            switchNotification.setIcon(icon)
            switchNotification.title = if (it) getString(R.string.notification_title)
            else getString(R.string.notification_title_off)
        }
    }

    private fun signOut() {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(getString(R.string.title_sign_out))
            .setMessage(getString(R.string.message_sign_out))
            .setPositiveButton(R.string.title_sign_out) { _, _ ->
                val provider = Auth.getCurrenUser()!!.providerId
                AuthUI.getInstance().signOut(requireContext())
                    .addOnSuccessListener {
                        if (provider == "google.com") {
                            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(getString(R.string.default_web_client_id))
                                .requestEmail()
                                .build()

                            val googleSingInClient : GoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
                            googleSingInClient.signOut()
                        }
                        CloudMessaging.unsubscribeToNotifications()
                        requireActivity().startActivity(Intent(requireContext(), InitActivity::class.java))
                        requireActivity().finish()
                    }
            }
            .setNegativeButton(R.string.text_cancel, null)

        val dialog = builder.create()
        dialog.show()
    }

    private fun dialogRules() {
        val string: SpannedString = buildSpannedString {
            bold { italic { append("\n5 Puntos") } }
            append(" si aciertas el marcador exacto.\n\n")
            bold { italic { append("3 Puntos") } }
            append(" si aciertas el ganador o predices que fue empate, pero fallas en el marcador.\n\n")
            bold { italic { append("-1 Punto") } }
            append(" si el equipo que predices como ganador, pierde. También si predices empate y hay un ganador. También si predices un ganador y termina en empate\n\n")
            bold { italic { append("-2 Puntos") } }
            append(" si aciertas el marcador, pero perdiendo el equipo que seleccionaste como ganador.")
        }
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(getString(R.string.title_rules))
            .setMessage(string)
            .setPositiveButton(R.string.text_accept, null)

        val dialog = builder.create()
        dialog.show()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) {
            requireActivity().finish()
        }
        return true
    }
}