package com.skysam.hchirinos.mundialcatar.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

/**
 * Created by Hector Chirinos on 09/05/2022.
 */

object Auth {
 fun getCurrenUser(): FirebaseUser? {
  val mAuth = FirebaseAuth.getInstance()
  return mAuth.currentUser
 }
}