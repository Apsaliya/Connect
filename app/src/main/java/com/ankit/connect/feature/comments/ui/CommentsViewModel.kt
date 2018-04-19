package com.ankit.connect.feature.comments.ui

import android.annotation.SuppressLint
import android.arch.lifecycle.MutableLiveData
import com.ankit.connect.App
import com.ankit.connect.feature.comments.model.Comment
import com.ankit.connect.base.BaseViewModel
import com.ankit.connect.feature.comments.data.RemoteDataSource
import io.reactivex.schedulers.Schedulers

/**
 * Created by ankit on 19/04/18.
 */
@SuppressLint("StaticFieldLeak")
class CommentsViewModel(val dataSource: RemoteDataSource, val application: App): BaseViewModel(application) {
  val viewState: MutableLiveData<ViewState> = MutableLiveData()
  
  init {
    viewState.value = ViewState()
  }
  
  private fun currentViewState(): ViewState = viewState.value!!
  
  data class ViewState(
      val showError: Boolean = false,
      val errorMessage: String? = null,
      val comments: List<Comment>? = null,
      val reset: Boolean = false
  )
  
  fun getCommnets(id: String) {
    addDisposible(dataSource.getComments(id)
        .subscribeOn(Schedulers.io())
        .subscribe({
          viewState.value = ViewState(comments = it)
          
        }, {
          viewState.value = ViewState(showError = true)
        }))
  }
  
  fun sendComment(commentText: String, id: String) {
    addDisposible(dataSource.sendComment(commentText, id)
        .subscribeOn(Schedulers.io())
        .subscribe({
          viewState.value = ViewState(reset = it)
        }, {
          viewState.value = ViewState(showError = true)
        }))
  }
}