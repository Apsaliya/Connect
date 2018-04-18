package com.ankit.connect.feature.login

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.ankit.connect.R
import com.ankit.connect.util.helpers.GAuthHelper
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import timber.log.Timber
import androidx.core.content.edit
import com.ankit.connect.extensions.*
import com.ankit.connect.util.Constants.LOGGED_IN
import com.google.firebase.auth.TwitterAuthProvider
import com.jakewharton.rxbinding2.view.clicks
import com.twitter.sdk.android.core.*
import com.twitter.sdk.android.core.identity.TwitterAuthClient


/**
 * Created by ankit on 13/04/18.
 */
class LoginActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {
  private var auth: FirebaseAuth? = null
  private var authListener: FirebaseAuth.AuthStateListener? = null
  private var googleApiClient: GoogleApiClient? = null
  
  val mTwitterAuthClient = TwitterAuthClient()
  
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)
    
    val preferences = getPreference()
    val authRequired = !preferences.getBoolean(LOGGED_IN, false)
    if (!authRequired) {
      val i = Intent(this, CreatePostActivity::class.java)
      startActivity(i)
      finish()
    }
    
    auth = FirebaseAuth.getInstance()
    googleApiClient = GAuthHelper.createGoogleApiClient(this)
    
    initGoogleLogin()
    initTwitterLogin()
    listenAuthState()
  }
  
  private fun initGoogleLogin() {
    google.clicks()
        .doOnNext {
          spin_kit.show()
          val anim = AnimationUtils
              .loadAnimation(this, R.anim.fadout)
          google.startAnimation(anim)
          anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation?) {
              google.hide()
            }
    
            override fun onAnimationRepeat(animation: Animation?) {
              //no-op
            }
    
            override fun onAnimationStart(animation: Animation?) {
              //no-op
            }
          })
        }
        .subscribe {
          signInWithGoogle()
        }
  }
  
  private fun initTwitterLogin() {
    twitter.clicks()
        .doOnNext { spin_kit.show() }
        .subscribe {
          mTwitterAuthClient.authorize(this, object : Callback<TwitterSession>() {
            override fun success(result: Result<TwitterSession>?) {
              val credential = TwitterAuthProvider.getCredential(
                  result?.data?.authToken?.token!!,
                  result.data?.authToken?.secret!!)
              
              auth?.signInWithCredential(credential)!!
                  .addOnCompleteListener(this@LoginActivity) { task ->
                    if (!task.isSuccessful) {
                      showSnackBar("Auth error")
                    } else {
                      onFirebaseAuthSuccess()
                    }
                  }
            }
            
            override fun failure(exception: TwitterException?) {
              showSnackBar("Could not log you in.")
            }
          })
        }
  }
  
  private fun listenAuthState() {
    authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
      val user = firebaseAuth.currentUser
      if (user != null) {
        // Profile is signed in
        val preference = getPreference()
        preference.edit {
          putBoolean(LOGGED_IN, true)
        }
      }
    }
  }
  
  override fun onStart() {
    super.onStart()
    authListener?.let { auth?.addAuthStateListener(it) }
    
    if (googleApiClient != null) {
      Timber.d("connecting google api client.")
      googleApiClient?.connect()
    }
  }
  
  override fun onStop() {
    super.onStop()
    authListener?.let { auth?.removeAuthStateListener(it) }
    
    if (googleApiClient != null && googleApiClient?.isConnected!!) {
      googleApiClient?.stopAutoManage(this)
      googleApiClient?.disconnect()
    }
  }
  
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    mTwitterAuthClient.onActivityResult(requestCode, resultCode, data)
    if (requestCode == 101) {
      val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
      if (result.isSuccess) {
        val account = result.signInAccount
        firebaseAuthWithGoogle(account!!)
      } else {
        spin_kit.hide()
        showSnackBar("Auth error")
      }
    } else {
      spin_kit.hide()
      google.show()
    }
  }
  
  private fun onFirebaseAuthSuccess() {
    spin_kit.hide()
    finish()
    val i = Intent(this@LoginActivity, CreatePostActivity::class.java)
    startActivity(i)
  }
  
  private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
    val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
    auth?.signInWithCredential(credential)!!
        .addOnCompleteListener(this) { task ->
          if (!task.isSuccessful) {
            showSnackBar("Auth error")
          } else {
            onFirebaseAuthSuccess()
          }
        }
  }
  
  private fun signInWithGoogle() {
    if (hasInternetConnection()) {
      val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
      startActivityForResult(signInIntent, 101)
    } else {
      showSnackBar("No internet!")
    }
  }
  
  override fun onConnectionFailed(p0: ConnectionResult) {
    showSnackBar("Connection failed")
  }
}