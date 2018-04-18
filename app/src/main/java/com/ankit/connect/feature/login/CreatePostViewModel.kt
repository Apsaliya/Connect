package com.ankit.connect.feature.login

import android.arch.lifecycle.MutableLiveData
import com.ankit.connect.data.model.Post
import com.ankit.connect.feature.login.profile.PostsAdapter
import com.ankit.connect.store.FirebaseDbHelper
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

/**
 * Created by ankit on 18/04/18.
 */
internal class CreatePostViewModel : BaseViewModel() {
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
  
  fun getAllNotices() {
    addDisposible(FirebaseDbHelper.getInstance().getPostList(0)
        .map { t -> t.posts }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
          it.hashCode()
          if (totalPosts == null || it.size != totalPosts) {
            viewState.value = ViewState(posts = it)
            totalPosts = it.size
          }
        }, {
          it.printStackTrace()
        }))
  }
  
  fun getLikeRemote(post: Post, position: Int) {
    addDisposible(FirebaseDbHelper.getInstance().hasCurrentUserLikeSingleValue(post.id!!, FirebaseAuth.getInstance().currentUser?.uid!!)
        .subscribeOn(Schedulers.io())
        .subscribe({
          post.isLiked = it
          Timber.d("get remote like.")
          viewState.value = ViewState(single = Pair(position, PostsAdapter.ACTION_DEFAULT))
        }, {
          it.printStackTrace()
        }))
  }
  
  private fun handleIncrement(post: Post, position: Int, action: String) {
    post.likesCount++
    post.isLiked = true
    Timber.d("local increment.")
    viewState.value = ViewState(single = Pair(position, action))
  }
  
  private fun handleDecrement(post: Post, position: Int, action: String) {
    post.likesCount--
    post.isLiked = false
    Timber.d("local decrement.")
    viewState.value = ViewState(single = Pair(position, action))
  }
  
  private fun createLike(post: Post, position: Int) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    addDisposible(FirebaseDbHelper.getInstance().createOrUpdateLike(post.id!!, currentUser?.uid!!)
        .subscribeOn(Schedulers.io())
        .subscribe({
          if (!it) {
            handleDecrement(post, position, PostsAdapter.ACTION_DEFAULT)
          }
        }, {
          handleDecrement(post, position, PostsAdapter.ACTION_DEFAULT)
        }))
  }
  
  private fun removeLike(post: Post, position: Int) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    addDisposible(FirebaseDbHelper.getInstance().removeLike(post.id!!, currentUser?.uid!!)
        .subscribeOn(Schedulers.io())
        .subscribe({
          if (!it) {
            handleIncrement(post, position, PostsAdapter.ACTION_DEFAULT)
          }
        }, {
          handleIncrement(post, position, PostsAdapter.ACTION_DEFAULT)
        }))
  }
  
  fun handleImageClick(post: Post, position: Int) {
    if (!post.isLiked!!) {
      Timber.d("image.")
      viewState.value = ViewState(single = Pair(position, PostsAdapter.ACTION_LIKE_IMAGE_CLICKED))
      post.likesCount++
      post.isLiked = true
      createLike(post, position)
    }
  }
  
  fun handleLikeClick(post: Post, position: Int) {
    if (post.isLiked == null || (post.isLiked != null && !post.isLiked!!)) {
      handleIncrement(post, position, PostsAdapter.ACTION_LIKE_BUTTON_CLICKED)
      createLike(post, position)
    } else {
      handleDecrement(post, position, PostsAdapter.ACTION_DEFAULT)
      removeLike(post, position)
    }
  }
}