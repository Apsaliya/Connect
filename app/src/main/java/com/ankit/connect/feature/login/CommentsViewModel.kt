package com.ankit.connect.feature.login

import android.arch.lifecycle.MutableLiveData
import android.support.v7.widget.LinearLayoutManager
import com.ankit.connect.data.model.Comment
import com.ankit.connect.data.model.Post
import com.ankit.connect.extensions.showSnackBar
import com.ankit.connect.store.FirebaseDbHelper
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_detail.*
import timber.log.Timber

/**
 * Created by ankit on 19/04/18.
 */
class CommentsViewModel: BaseViewModel() {
  val viewState: MutableLiveData<CommentsViewModel.ViewState> = MutableLiveData()
  
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
    addDisposible(FirebaseDbHelper.getInstance().getCommentsList(id)
        .subscribeOn(Schedulers.io())
        .subscribe({
          viewState.value = ViewState(comments = it)
          
        }, {
          viewState.value = ViewState(showError = true)
        }))
  }
  
  fun sendComment(commentText: String, id: String) {
    addDisposible(FirebaseDbHelper.getInstance().createComment(commentText, id)
        .subscribeOn(Schedulers.io())
        .subscribe({
          Timber.d("Comment added successfully")
          viewState.value = ViewState(reset = it)
        }, {
          viewState.value = ViewState(showError = true)
        }))
  }
}