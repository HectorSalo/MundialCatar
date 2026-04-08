package com.skysam.hchirinos.mundial2026.ui.init

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.skysam.hchirinos.mundial2026.dataclass.User
import com.skysam.hchirinos.mundial2026.repositories.UsersRepository
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

    suspend fun ensureUserExists(user: User) {
        val exists = usersRepository.userExists(user.id)
        if (!exists) {
            usersRepository.createUser(user)
        }
    }
}