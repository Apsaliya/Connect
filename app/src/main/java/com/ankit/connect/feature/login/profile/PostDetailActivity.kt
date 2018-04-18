package com.ankit.connect.feature.login.profile

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import com.ankit.connect.R
import android.view.inputmethod.InputMethodManager
import com.ankit.connect.data.model.Comment
import com.ankit.connect.data.model.Post
import com.ankit.connect.extensions.showSnackBar
import com.ankit.connect.feature.login.CommentsAdapter
import com.ankit.connect.feature.login.CommentsViewModel
import com.ankit.connect.feature.login.CreatePostViewModel
import com.ankit.connect.store.FirebaseDbHelper
import com.ankit.connect.util.Cache
import com.bumptech.glide.Glide
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.item_feed.*
import timber.log.Timber

/**
 * Created by ankit on 15/04/18.
 */
class PostDetailActivity : AppCompatActivity() {
  lateinit var post: Post
  private lateinit var viewModel: CommentsViewModel
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_detail)
    post = Cache.get("data") as Post
    viewModel = ViewModelProviders.of(this).get(CommentsViewModel::class.java)
  
    viewModel.getCommnets(post.id!!)
    
    viewModel.viewState.observe(this, Observer {
      if (it?.comments != null) {
        if (comments.adapter == null) {
          val adapter = CommentsAdapter(it.comments)
          comments.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
          comments.adapter = adapter
        } else {
          val currentAdapter = comments.adapter as CommentsAdapter
          currentAdapter.dispatchUpdates(it.comments)
        }
      }
      
      if (it?.reset != null && it.reset) {
        addComment.text = null
        addComment.clearFocus()
      }
      
      if (it?.showError!!) {
        if (it.errorMessage == null) {
          showSnackBar("Something went wrong .Could not fetch comments.")
        } else {
          showSnackBar(it.errorMessage)
        }
      }
    })
    
    sendComment.setOnClickListener {
      val commentText = addComment.text.toString()
      
      if (commentText.isNotEmpty()) {
        viewModel.sendComment(commentText, post.id!!)
      }
    }
  }
  
}