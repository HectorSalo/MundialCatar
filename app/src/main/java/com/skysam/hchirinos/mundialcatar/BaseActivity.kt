package com.skysam.hchirinos.mundialcatar

import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * Created by Hector Chirinos in the home office on 3 abr. 2026
 */
abstract class BaseActivity: AppCompatActivity() {
    protected fun setupEdgeToEdge(contentView: View, bottomBar: View? = null) {
        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(contentView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.updatePadding(
                left = systemBars.left,
                top = systemBars.top,
                right = systemBars.right
            )

            bottomBar?.updatePadding(
                left = systemBars.left,
                right = systemBars.right,
                bottom = systemBars.bottom
            )

            insets
        }
    }
}