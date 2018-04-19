package com.ankit.connect.feature.login.helpers

import android.support.v4.app.FragmentActivity
import com.ankit.connect.R
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient

object GAuthHelper {
  fun createGoogleApiClient(fragmentActivity: FragmentActivity): GoogleApiClient {
    val failedListener: GoogleApiClient.OnConnectionFailedListener
    
    if (fragmentActivity is GoogleApiClient.OnConnectionFailedListener) {
      failedListener = fragmentActivity
    } else {
      throw IllegalArgumentException(fragmentActivity.javaClass.simpleName + " should implement OnConnectionFailedListener")
    }
    
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(fragmentActivity.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()
    
    return GoogleApiClient.Builder(fragmentActivity)
        .enableAutoManage(fragmentActivity, failedListener)
        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
        .build()
  }
}