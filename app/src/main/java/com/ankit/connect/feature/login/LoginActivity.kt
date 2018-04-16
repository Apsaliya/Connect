package com.ankit.connect.feature.login

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.ankit.connect.R
import com.ankit.connect.util.helpers.GAuthHelper
import com.ankit.connect.util.managers.ProfileManager
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
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.TwitterAuthProvider
import com.twitter.sdk.android.core.*
import com.twitter.sdk.android.core.identity.TwitterAuthClient




/**
 * Created by ankit on 13/04/18.
 */
class LoginActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {
  private var mAuth: FirebaseAuth? = null
  private var mAuthListener: FirebaseAuth.AuthStateListener? = null
  private var mGoogleApiClient: GoogleApiClient? = null
  val authConfig = TwitterAuthConfig("kPy9COcD6RfCOTzmK4N2oVTqU",
      "FlDVCqQMGLy0oaEM8mIfBTx4rm1BfFJhElZ7RjX30BpofHePNb")
  
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)
    Timber.plant(Timber.DebugTree())
  
    val twitterConfig = TwitterConfig.Builder(this)
        .twitterAuthConfig(authConfig)
        .build()
  
    Twitter.initialize(twitterConfig)
  
    val mTwitterAuthClient = TwitterAuthClient()
  
  
    val preferences = getPreference()
    val authRequired = !preferences.getBoolean(LOGGED_IN, false)
    if (!authRequired) {
      val i = Intent(this, CreatePostActivity::class.java)
      startActivity(i)
      finish()
    }
  
    mAuth = FirebaseAuth.getInstance()
    mGoogleApiClient = GAuthHelper.createGoogleApiClient(this)
    google.setOnClickListener {
      spin_kit.visibility = VISIBLE
      val anim = AnimationUtils
          .loadAnimation(this, R.anim.fadout)
      it.startAnimation(anim)
      anim.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationEnd(animation: Animation?) {
          it.visibility = GONE
        }
  
        override fun onAnimationRepeat(animation: Animation?) {
          //no-op
        }
  
        override fun onAnimationStart(animation: Animation?) {
          //no-op
        }
      })
      signInWithGoogle()
    }
    
    
    twitter.setOnClickListener {
      spin_kit.show()
      mTwitterAuthClient.authorize(this, object : Callback<TwitterSession>() {
        override fun success(result: Result<TwitterSession>?) {
          val credential = TwitterAuthProvider.getCredential(
              result?.data?.authToken?.token!!,
              result.data?.authToken?.secret!!)
  
          mAuth?.signInWithCredential(credential)!!
              .addOnCompleteListener(this@LoginActivity) { task ->
      
                // If sign in fails, display a message to the user. If sign in succeeds
                // the auth state listener will be notified and logic to handle the
                // signed in user can be handled in the listener.
                Timber.d("signInWithCredential task completed.")
      
                if (!task.isSuccessful) {
                  Timber.d("signInWithCredential task unsuccessfull.")
                  showSnackBar("Auth error")
                } else {
                  spin_kit.hide()
                  finish()
                  val i = Intent(this@LoginActivity, CreatePostActivity::class.java)
                  startActivity(i)
                }
              }
        }
  
        override fun failure(exception: TwitterException?) {
          showSnackBar("Could not log you in.")
        }
      })
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
              val preference = getPreference()
              preference.edit {
                putBoolean(LOGGED_IN, true)
              }
            }, {
             it.printStackTrace()
              showSnackBar("Could not log you in.")
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
        firebaseAuthWithGoogle(account!!)
      } else {
        spin_kit.visibility = GONE
        showSnackBar("Auth error")
      }
    } else {
      spin_kit.visibility = GONE
      google.visibility = VISIBLE
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
            spin_kit.visibility = GONE
            finish()
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