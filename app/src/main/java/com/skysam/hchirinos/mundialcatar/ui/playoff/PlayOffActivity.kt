package com.skysam.hchirinos.mundialcatar.ui.playoff

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.skysam.hchirinos.mundialcatar.BaseActivity
import com.skysam.hchirinos.mundialcatar.databinding.ActivityPlayOffBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayOffActivity : BaseActivity() {

    private lateinit var binding: ActivityPlayOffBinding
    private val viewModel: PlayOffViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPlayOffBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupEdgeToEdge(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, PlayOffFragment.newInstance())
                .commit()
        }

        viewModel.setIndex(0)
    }
}