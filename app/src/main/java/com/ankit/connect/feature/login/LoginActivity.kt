package com.ankit.connect.feature.login

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.ankit.connect.store.FirebaseDbHelper
import com.ankit.connect.R
import com.ankit.connect.extensions.hasInternetConnection
import com.ankit.connect.extensions.showSnackBar
import com.ankit.connect.util.helpers.GAuthHelper
import com.ankit.connect.util.managers.ProfileManager
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_login.*
import timber.log.Timber

/**
 * Created by ankit on 13/04/18.
 */
class LoginActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {
  private var mAuth: FirebaseAuth? = null
  private var mAuthListener: FirebaseAuth.AuthStateListener? = null
  private var mGoogleApiClient: GoogleApiClient? = null
  private var profilePhotoUrlLarge: String? = null
  
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)
    Timber.plant(Timber.DebugTree())
  
    mAuth = FirebaseAuth.getInstance()
    mGoogleApiClient = GAuthHelper.createGoogleApiClient(this)
    google.setOnClickListener {
      signInWithGoogle()
    }
  
    mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
      Timber.d("firebase auth listener callback.")
      val user = firebaseAuth.currentUser
      if (user != null) {
        // Profile is signed in
        Timber.d("user non null. Checking if profile exists.")
        ProfileManager.getInstance().getProfileExists(user.uid)
            .subscribe({
              Timber.d("profile exists response : $it")
              if (it) {
                FirebaseDbHelper.getInstance()
                    .addRegistrationToken(FirebaseInstanceId.getInstance().token, user.uid)
              } else {
              
              }
            }, {
             it.printStackTrace()
            })
      } else {
        // Profile is signed out
      }
    }
  }
  
  override fun onStart() {
    super.onStart()
    mAuthListener?.let { mAuth?.addAuthStateListener(it) }
  
    if (mGoogleApiClient != null) {
      Timber.d("connecting google api client.")
      mGoogleApiClient?.connect()
    }
  }
  
  override fun onStop() {
    super.onStop()
    mAuthListener?.let { mAuth?.removeAuthStateListener(it) }
  
    if (mGoogleApiClient != null && mGoogleApiClient?.isConnected!!) {
      mGoogleApiClient?.stopAutoManage(this)
      mGoogleApiClient?.disconnect()
    }
  }
  
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == 101) {
      Timber.d("Activity has come back with code 101.")
      val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
      if (result.isSuccess) {
        Timber.d("Successfull login")
        // Google Sign In was successful, authenticate with Firebase
        val account = result.signInAccount
        profilePhotoUrlLarge = String.format(getString(R.string.google_large_image_url_pattern),
            account!!.photoUrl, 1280)
        firebaseAuthWithGoogle(account)
      } else {
        Timber.e("login failure")
      }
    }
  }
  
  private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
    
    val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
    Timber.d("Signing in with credential")
    mAuth?.signInWithCredential(credential)!!
        .addOnCompleteListener(this) { task ->
          
          // If sign in fails, display a message to the user. If sign in succeeds
          // the auth state listener will be notified and logic to handle the
          // signed in user can be handled in the listener.
          Timber.d("signInWithCredential task completed.")
          
          if (!task.isSuccessful) {
            Timber.d("signInWithCredential task unsuccessfull.")
            showSnackBar("Auth error")
          } else {
            val i = Intent(this@LoginActivity, CreatePostActivity::class.java)
            startActivity(i)
          }
        }
  }
  
  private fun signInWithGoogle() {
    Timber.d("Signing in with google.")
    if (hasInternetConnection()) {
      Timber.d("Internet available. Stating activity.")
      val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
      startActivityForResult(signInIntent, 101)
    } else {
      showSnackBar("No internet!")
    }
  }
  
  override fun onConnectionFailed(p0: ConnectionResult) {
  }
}