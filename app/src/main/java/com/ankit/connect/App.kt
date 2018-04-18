package com.ankit.connect

import android.app.Application
import android.content.Context
import android.support.multidex.MultiDex
import com.ankit.connect.util.Constants.TWITTER_KEY
import com.ankit.connect.util.Constants.TWITTER_SECRET
import com.twitter.sdk.android.core.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import com.twitter.sdk.android.core.TwitterConfig
import timber.log.Timber

/**
 * Created by ankit on 17/04/18.
 */
class App : Application() {
  
  val authConfig = TwitterAuthConfig(TWITTER_KEY,
      TWITTER_SECRET)
  
  override fun onCreate() {
    super.onCreate()
    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
    }
    
    val twitterConfig = TwitterConfig.Builder(this)
        .twitterAuthConfig(authConfig)
        .build()
  
    Twitter.initialize(twitterConfig)
  }
  
  override fun attachBaseContext(base: Context?) {
    super.attachBaseContext(base)
    MultiDex.install(this)
  }
}