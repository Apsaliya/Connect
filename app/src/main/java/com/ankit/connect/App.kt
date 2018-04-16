package com.ankit.connect

import android.app.Application
import android.content.Context
import android.support.multidex.MultiDex

/**
 * Created by ankit on 17/04/18.
 */
class App : Application() {
  override fun attachBaseContext(base: Context?) {
    super.attachBaseContext(base)
    MultiDex.install(this)
  }
}