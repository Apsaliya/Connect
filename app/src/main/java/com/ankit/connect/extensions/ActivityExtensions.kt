@file:Suppress("NOTHING_TO_INLINE")

package com.ankit.connect.extensions

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.view.View
import com.ankit.connect.R

/**
 * Created by ankit on 13/04/18.
 */
inline fun Activity.showSnackBar(message: String) {
  val snackbar = Snackbar.make(findViewById<View>(android.R.id.content),
      message, Snackbar.LENGTH_LONG)
  snackbar.show()
}

fun shouldShowPermissionRationale(activity: Activity, pemissionsToBeChecked: Array<String>): Boolean {
  for (permission in pemissionsToBeChecked) {
    if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
      return false
    }
  }
  return true
}

fun checkPermissions(context: Context, permissions: Array<String>): Boolean {
  // At least one result must be checked.
  if (permissions.isEmpty()) {
    return false
  }
  
  // Verify that each required permission has been granted, otherwise return false.
  for (permission in permissions) {
    try {
      if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
        // In API<16 there are no permissions called as READ_CALL_LOG and WRITE_CALL_LOG so eliminating checking of those permissions if API < 16
        // https://developer.android.com/reference/android/Manifest.permission.html#READ_CALL_LOG
        // https://developer.android.com/reference/android/Manifest.permission.html#WRITE_CALL_LOG
        // DebugLog.e("Permission Not Granted : " + permission);
        return false
      }
    } catch (e: RuntimeException) {
      return false
    }
    
  }
  return true
}

inline fun Activity.checkAndRequestStoragePermission(): Boolean {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    val pemissionsToBeChecked = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    if (!checkPermissions(this, pemissionsToBeChecked)) run {
      ActivityCompat.requestPermissions(this, pemissionsToBeChecked, 1)
      return false
    }
  }
  return true
}

fun Activity.showPermissionDialog(requestAgain: Boolean) {
  val okListener = DialogInterface.OnClickListener { dialog, which ->
    if (requestAgain) {
      checkAndRequestStoragePermission()
    } else {
      val myAppSettings = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
      myAppSettings.addCategory(Intent.CATEGORY_DEFAULT)
      myAppSettings.flags = Intent.FLAG_ACTIVITY_NEW_TASK
      startActivity(myAppSettings)
    }
  }
  
  val builder = AlertDialog.Builder(this).setMessage(getString(R.string.no_permission)).setPositiveButton(getString(R.string.label_ok), okListener)
  
  if (!TextUtils.isEmpty(title)) {
    builder.setTitle(title)
  }
  
  builder.show()
}


@Suppress("NOTHING_TO_INLINE")
inline fun Activity.processPermissionResponse(): Boolean {
  //val pemissionsToBeChecked = Array<String>(2)
  val pemissionsToBeChecked = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
  when {
    checkPermissions(this, pemissionsToBeChecked) -> run {
      return true
    }
    shouldShowPermissionRationale(this, pemissionsToBeChecked) -> run {
      // system says we should prompt user why we need permission.
      showPermissionDialog(true)
    }
    else -> showPermissionDialog(false)
  }
  return false
}
