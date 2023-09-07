package com.skysam.hchirinos.mundialcatar.ui.points

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.skysam.hchirinos.mundialcatar.dataclass.User
import com.skysam.hchirinos.mundialcatar.repositories.UsersRepository

class PointsViewModel : ViewModel() {
    val users: LiveData<List<User>> = UsersRepository.getUsersByPoints().asLiveData()
}