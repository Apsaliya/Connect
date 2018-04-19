package com.ankit.connect.feature.login

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.ankit.connect.R
import com.ankit.connect.feature.login.helpers.GAuthHelper
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.login.*
import androidx.core.content.edit
import com.ankit.connect.extensions.*
import com.ankit.connect.feature.feed.ui.FeedActivity
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
    setContentView(R.layout.login)
    
    val preferences = getPreference()
    val authRequired = !preferences.getBoolean(LOGGED_IN, false)
    if (!authRequired) {
      val i = Intent(this, FeedActivity::class.java)
      startActivity(i)
      finish()
    }
    
    auth = FirebaseAuth.getInstance()
    googleApiClient = GAuthHelper.createGoogleApiClient(this)
    
    initGoogleLogin()
    initTwitterLogin()
    listenAuthState()
  }
  
  private fun hideButtonsWithAnimation() {
    val anim = AnimationUtils
        .loadAnimation(this, R.anim.fadout)
    google.startAnimation(anim)
    twitter.startAnimation(anim)
    anim.setAnimationListener(object : Animation.AnimationListener {
      override fun onAnimationEnd(animation: Animation?) {
        google.hide()
        twitter.hide()
      }
    
      override fun onAnimationRepeat(animation: Animation?) {
        //no-op
      }
    
      override fun onAnimationStart(animation: Animation?) {
        //no-op
      }
    })
  }
  
  private fun initGoogleLogin() {
    google.clicks()
        .doOnNext {
          spin_kit.show()
          hideButtonsWithAnimation()
        }
        .subscribe {
          signInWithGoogle()
        }
  }
  
  private fun initTwitterLogin() {
    twitter.clicks()
        .doOnNext {
          spin_kit.show()
          hideButtonsWithAnimation()
        }
        .subscribe {
          mTwitterAuthClient.authorize(this, object : Callback<TwitterSession>() {
            override fun success(result: Result<TwitterSession>?) {
              val credential = TwitterAuthProvider.getCredential(
                  result?.data?.authToken?.token!!,
                  result.data?.authToken?.secret!!)
              
              auth?.signInWithCredential(credential)!!
                  .addOnCompleteListener(this@LoginActivity) { task ->
                    if (!task.isSuccessful) {
                      showSnackBar(getString(R.string.label_auth_failed))
                      google.show()
                      twitter.show()
                    } else {
                      onFirebaseAuthSuccess()
                    }
                  }
            }
            
            override fun failure(exception: TwitterException?) {
              showSnackBar(getString(R.string.label_auth_failed))
              google.show()
              twitter.show()
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
        google.show()
        twitter.show()
        showSnackBar(getString(R.string.label_auth_failed))
      }
    } else {
      spin_kit.hide()
      google.show()
    }
  }
  
  private fun onFirebaseAuthSuccess() {
    spin_kit.hide()
    finish()
    val i = Intent(this@LoginActivity, FeedActivity::class.java)
    startActivity(i)
  }
  
  private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
    val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
    auth?.signInWithCredential(credential)!!
        .addOnCompleteListener(this) { task ->
          if (!task.isSuccessful) {
            google.show()
            twitter.show()
            showSnackBar(getString(R.string.label_auth_failed))
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
      getString(R.string.no_internet)
      google.show()
      twitter.show()
    }
  }
  
  override fun onConnectionFailed(p0: ConnectionResult) {
    showSnackBar(getString(R.string.label_auth_failed))
    google.show()
    twitter.show()
  }
}