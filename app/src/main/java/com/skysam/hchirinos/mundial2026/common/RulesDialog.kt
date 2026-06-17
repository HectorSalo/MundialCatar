package com.skysam.hchirinos.mundial2026.common

import android.content.Context
import androidx.core.text.HtmlCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.skysam.hchirinos.mundial2026.R

/**
 * Única fuente de la verdad para las reglas de puntaje.
 * Usado tanto en Ajustes como en la vista de Puntos.
 */
object RulesDialog {

    fun show(context: Context) {
        val message = HtmlCompat.fromHtml(
            context.getString(R.string.rules_message),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.title_rules))
            .setMessage(message)
            .setPositiveButton(R.string.text_accept, null)
            .create()
            .show()
    }
}
