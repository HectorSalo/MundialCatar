package com.skysam.hchirinos.mundialcatar.ui.init

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.User
import com.skysam.hchirinos.mundialcatar.repositories.GamesRepository
import com.skysam.hchirinos.mundialcatar.repositories.UsersRespository

/**
 * Created by Hector Chirinos on 11/05/2022.
 */

class InitViewModel: ViewModel() {
 val games: LiveData<MutableList<Game>> = GamesRepository.getAllGames().asLiveData()
 val users: LiveData<MutableList<User>> = UsersRespository.getUsersByPoints().asLiveData()

 fun createUser(user: User) {
  UsersRespository.createUser(user)
 }
}