package com.skysam.hchirinos.mundialcatar.ui.points

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.skysam.hchirinos.mundialcatar.dataclass.User
import com.skysam.hchirinos.mundialcatar.repositories.UsersRespository

class PointsViewModel : ViewModel() {
    val users: LiveData<MutableList<User>> = UsersRespository.getUsersByPoints().asLiveData()
}