package com.skysam.hchirinos.mundialcatar.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Hector Chirinos on 09/05/2022.
 */

@Singleton
class Auth @Inject constructor(
 private val firebaseAuth: FirebaseAuth
) {
 fun getCurrentUser(): FirebaseUser? =
  firebaseAuth.currentUser

 fun isSignedIn(): Boolean =
  firebaseAuth.currentUser != null
}