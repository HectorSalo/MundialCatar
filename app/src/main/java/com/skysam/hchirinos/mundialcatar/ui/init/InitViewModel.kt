package com.skysam.hchirinos.mundialcatar.ui.init

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.User
import com.skysam.hchirinos.mundialcatar.repositories.GamesRepository
import com.skysam.hchirinos.mundialcatar.repositories.UsersRepository

/**
 * Created by Hector Chirinos on 11/05/2022.
 */

class InitViewModel: ViewModel() {
 val users: LiveData<List<User>> = UsersRepository.getUsersByPoints().asLiveData()

 fun createUser(user: User) {
  UsersRepository.createUser(user)
 }
}