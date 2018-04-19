package com.ankit.connect.feature.comments.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.ankit.connect.App
import com.ankit.connect.R
import com.ankit.connect.feature.feed.models.Post
import com.ankit.connect.extensions.hide
import com.ankit.connect.extensions.show
import com.ankit.connect.extensions.showSnackBar
import com.ankit.connect.feature.comments.CommentsViewModelFactory
import com.ankit.connect.feature.comments.data.RemoteDataSource
import com.ankit.connect.util.Cache
import kotlinx.android.synthetic.main.activity_detail.*

/**
 * Created by ankit on 15/04/18.
 */
class CommentsActivity : AppCompatActivity() {
  lateinit var post: Post
  private lateinit var viewModel: CommentsViewModel
  
  companion object {
    const val KEY_DATA = "data"
  }
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_detail)
    post = Cache.get(KEY_DATA) as Post
    viewModel = ViewModelProviders.of(this, CommentsViewModelFactory(RemoteDataSource(), application as App)).get(CommentsViewModel::class.java)
  
    spinKitComment.show()
    viewModel.getCommnets(post.id!!)
    
    viewModel.viewState.observe(this, Observer {
      if (it?.comments != null) {
        spinKitComment.hide()
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
          showSnackBar(getString(R.string.unknown_error))
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