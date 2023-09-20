package com.skysam.hchirinos.mundialcatar.common

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

/**
 * Created by Hector Chirinos on 19/09/2023.
 */

object Permission {
 @RequiresApi(Build.VERSION_CODES.TIRAMISU)
 fun checkPermissionReadStorage(): Boolean {
  val result = ContextCompat.checkSelfPermission(Mundial.Mundial.getContext(), Manifest.permission.POST_NOTIFICATIONS)
  return result == PackageManager.PERMISSION_GRANTED
 }
}