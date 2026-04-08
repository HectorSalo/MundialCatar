package com.skysam.hchirinos.mundial2026.ui.points

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.skysam.hchirinos.mundial2026.dataclass.User
import com.skysam.hchirinos.mundial2026.repositories.UsersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PointsViewModel @Inject constructor(
    private val usersRepository: UsersRepository
) : ViewModel() {
    val users: LiveData<List<User>> = usersRepository.getUsersByPoints().asLiveData()
}