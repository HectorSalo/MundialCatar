package com.skysam.hchirinos.mundialcatar.ui.groups

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.skysam.hchirinos.mundialcatar.databinding.ActivityGroupsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGroupsBinding
    private val viewModel: GroupsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGroupsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, GroupsFragment.newInstance())
                .commit()
        }

        viewModel.setIndex(0)
    }
}