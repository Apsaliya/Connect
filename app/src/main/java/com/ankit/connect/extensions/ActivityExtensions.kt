@file:Suppress("NOTHING_TO_INLINE")

package com.ankit.connect.extensions

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
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

/**
 * Created by ankit on 13/04/18.
 */
inline fun Activity.showSnackBar(message: String) {
  val snackbar = Snackbar.make(findViewById<View>(android.R.id.content),
      message, Snackbar.LENGTH_LONG)
  snackbar.show()
}

@Suppress("NOTHING_TO_INLINE")
inline fun Activity.checkStoragePermission(): Boolean {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    val permissionCheckRead = ContextCompat.checkSelfPermission(this,
        Manifest.permission.READ_EXTERNAL_STORAGE)
    val permissionCheckWrite = ContextCompat.checkSelfPermission(this,
        Manifest.permission.WRITE_EXTERNAL_STORAGE)
    if (permissionCheckRead != PackageManager.PERMISSION_GRANTED || permissionCheckWrite != PackageManager.PERMISSION_GRANTED) {
      if (ActivityCompat.shouldShowRequestPermissionRationale(this,
              Manifest.permission.READ_EXTERNAL_STORAGE)) {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            1)
      } else {
        
        val okListener = DialogInterface.OnClickListener { dialog, which ->
          val myAppSettings = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
          myAppSettings.addCategory(Intent.CATEGORY_DEFAULT)
          myAppSettings.flags = Intent.FLAG_ACTIVITY_NEW_TASK
          startActivity(myAppSettings)
        }
        
        val builder = AlertDialog.Builder(this).setMessage("Permission required").setPositiveButton("OK", okListener)
        
        if (!TextUtils.isEmpty(title)) {
          builder.setTitle(title)
        }
        
        builder.show()
      }
      return false
    } else
      return true
  }
  return true
}