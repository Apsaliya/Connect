package com.ankit.connect.feature.feed

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.ankit.connect.App
import com.ankit.connect.feature.feed.data.RemoteDataSource
import com.ankit.connect.feature.feed.ui.FeedViewModel

internal class FeedViewModelFactory(private val remoteDataSource: RemoteDataSource, val app: App) : ViewModelProvider.Factory {
  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(FeedViewModel::class.java)) {
      return modelClass.getConstructor(RemoteDataSource::class.java, App::class.java).newInstance(remoteDataSource, app)
    }
    throw IllegalStateException("Unknown ViewModel class.")
  }
}