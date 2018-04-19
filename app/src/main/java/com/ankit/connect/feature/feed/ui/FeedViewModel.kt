package com.ankit.connect.feature.feed.ui

import android.annotation.SuppressLint
import android.arch.lifecycle.MutableLiveData
import com.ankit.connect.App
import com.ankit.connect.feature.feed.models.Post
import com.ankit.connect.base.BaseViewModel
import com.ankit.connect.feature.feed.data.RemoteDataSource
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by ankit on 18/04/18.
 */
@SuppressLint("StaticFieldLeak")
internal class FeedViewModel(val remoteDataSource: RemoteDataSource, val application: App) : BaseViewModel(application) {
  val viewState: MutableLiveData<ViewState> = MutableLiveData()
  var totalPosts: Int? = null
  
  init {
    viewState.value = ViewState()
  }
  
  private fun currentViewState(): ViewState = viewState.value!!
  
  data class ViewState(
      val showError: Boolean = false,
      val errorMessage: String? = null,
      val posts: ArrayList<Post>? = null,
      val single: Pair<Int, String>? = null
  )
  
  fun getAllPosts() {
    addDisposible(remoteDataSource.getAllPosts()
        .map { t -> t.posts }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
          if (totalPosts == null || it.size != totalPosts) {
            viewState.value = ViewState(posts = it)
            totalPosts = it.size
          }
        }, {
          viewState.value = ViewState(showError = true)
        }))
  }
  
  fun getLikeRemote(post: Post, position: Int) {
    addDisposible(remoteDataSource.getLikeRemote(post.id!!, FirebaseAuth.getInstance().currentUser?.uid!!)
        .subscribeOn(Schedulers.io())
        .subscribe({
          post.isLiked = it
          viewState.value = ViewState(single = Pair(position, FeedAdapter.ACTION_DEFAULT))
        }, {
          it.printStackTrace()
        }))
  }
  
  private fun handleIncrementLocally(post: Post, position: Int, action: String) {
    post.likesCount++
    post.isLiked = true
    viewState.value = ViewState(single = Pair(position, action))
  }
  
  private fun handleDecrementLocally(post: Post, position: Int, action: String) {
    post.likesCount--
    post.isLiked = false
    viewState.value = ViewState(single = Pair(position, action))
  }
  
  private fun createLike(post: Post, position: Int) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    addDisposible(remoteDataSource.createLike(post.id!!, currentUser?.uid!!)
        .subscribeOn(Schedulers.io())
        .subscribe({
          if (!it) {
            handleDecrementLocally(post, position, FeedAdapter.ACTION_DEFAULT)
          }
        }, {
          handleDecrementLocally(post, position, FeedAdapter.ACTION_DEFAULT)
        }))
  }
  
  private fun removeLike(post: Post, position: Int) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    addDisposible(remoteDataSource.removeLike(post.id!!, currentUser?.uid!!)
        .subscribeOn(Schedulers.io())
        .subscribe({
          if (!it) {
            handleIncrementLocally(post, position, FeedAdapter.ACTION_DEFAULT)
          }
        }, {
          handleIncrementLocally(post, position, FeedAdapter.ACTION_DEFAULT)
        }))
  }
  
  fun handleImageClick(post: Post, position: Int) {
    if (!post.isLiked!!) {
      viewState.value = ViewState(single = Pair(position, FeedAdapter.ACTION_LIKE_IMAGE_CLICKED))
      post.likesCount++
      post.isLiked = true
      createLike(post, position)
    }
  }
  
  fun handleLikeClick(post: Post, position: Int) {
    if (post.isLiked == null || (post.isLiked != null && !post.isLiked!!)) {
      handleIncrementLocally(post, position, FeedAdapter.ACTION_LIKE_BUTTON_CLICKED)
      createLike(post, position)
    } else {
      handleDecrementLocally(post, position, FeedAdapter.ACTION_DEFAULT)
      removeLike(post, position)
    }
  }
}