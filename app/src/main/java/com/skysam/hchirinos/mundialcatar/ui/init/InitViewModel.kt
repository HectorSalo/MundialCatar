package com.skysam.hchirinos.mundialcatar.ui.init

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.skysam.hchirinos.mundialcatar.dataclass.User
import com.skysam.hchirinos.mundialcatar.repositories.UsersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Created by Hector Chirinos on 11/05/2022.
 */

@HiltViewModel
class InitViewModel @Inject constructor(private val usersRepository: UsersRepository) : ViewModel() {
 val users: LiveData<List<User>> = usersRepository.getUsersByPoints().asLiveData()

 fun createUser(user: User) {
  usersRepository.createUser(user)
 }
}