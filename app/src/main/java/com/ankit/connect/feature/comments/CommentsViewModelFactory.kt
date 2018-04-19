package com.ankit.connect.feature.comments

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.ankit.connect.App
import com.ankit.connect.feature.comments.data.RemoteDataSource
import com.ankit.connect.feature.comments.ui.CommentsViewModel

internal class CommentsViewModelFactory(val remoteDataSource: RemoteDataSource, val app: App) : ViewModelProvider.Factory {
  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(CommentsViewModel::class.java)) {
      return modelClass.getConstructor(RemoteDataSource::class.java, App::class.java).newInstance(remoteDataSource, app)
    }
    throw IllegalStateException("Unknown ViewModel class.")
  }
}