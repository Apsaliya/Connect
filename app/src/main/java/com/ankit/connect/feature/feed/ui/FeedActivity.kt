@file:Suppress("MemberVisibilityCanBePrivate")

package com.ankit.connect.feature.feed.ui

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.ankit.connect.util.Cache
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearLayoutManager.VERTICAL
import com.ankit.connect.App
import com.ankit.connect.R
import com.ankit.connect.feature.feed.models.Post
import com.ankit.connect.extensions.*
import com.ankit.connect.feature.comments.ui.CommentsActivity
import com.ankit.connect.feature.comments.ui.CommentsActivity.Companion.KEY_DATA
import com.ankit.connect.feature.feed.FeedViewModelFactory
import com.ankit.connect.feature.feed.data.RemoteDataSource
import com.ankit.connect.feature.feed.manager.PostManager
import com.google.firebase.auth.FirebaseAuth
import com.sangcomz.fishbun.FishBun
import com.sangcomz.fishbun.adapter.image.impl.GlideAdapter
import com.sangcomz.fishbun.define.Define
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.feed.*
import kotlinx.android.synthetic.main.toolbar_main.*
import java.util.ArrayList
import com.jakewharton.rxbinding2.view.clicks


/**
 * Created by ankit on 14/04/18.
 */
class FeedActivity : AppCompatActivity() {
  
  internal var uris = ArrayList<Uri>()
  internal lateinit var viewModel: FeedViewModel
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.feed)
    viewModel = ViewModelProviders.of(this, FeedViewModelFactory(RemoteDataSource(), application as App)).get(FeedViewModel::class.java)
  
    list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    list.itemAnimator = ItemAnimator()
    fab.clicks()
        .subscribe{
          if (hasInternetConnection()) {
            if (checkAndRequestStoragePermission()) {
              showGallery()
            }
          } else {
            showSnackBar(getString(R.string.no_internet))
          }
        }
    
    spinKitFeed.show()
    viewModel.getAllNotices()
    
    viewModel.viewState.observe(this, Observer {
      if (it?.showError!!) {
        if (it.errorMessage != null) {
          showSnackBar(it.errorMessage)
        } else {
          showSnackBar(getString(R.string.unknown_error))
        }
      }
      
      if (it.posts != null) {
        spinKitFeed.hide()
        val adapter = list.adapter
        if (adapter != null) {
          (adapter as FeedAdapter).dispatchUpdates(it.posts)
        } else {
          val newAdapter = FeedAdapter(this@FeedActivity, it.posts, viewModel)
          list.layoutManager = LinearLayoutManager(this@FeedActivity, VERTICAL, false)
          newAdapter.itemClickListener = listener
          list.itemAnimator = ItemAnimator()
          list.adapter = newAdapter
        }
      }
      
      if (it.single != null && list.adapter != null) {
        if (it.single.second != FeedAdapter.ACTION_DEFAULT) {
          list.adapter.notifyItemChanged(it.single.first, it.single.second)
        } else {
          list.adapter.notifyItemChanged(it.single.first)
        }
      }
    })
  }
  
  internal val listener = object : FeedAdapter.OnItemClickListener {
    override fun onItemClick(clickListener: FeedAdapter.ClickListener) {
      when (clickListener) {
        is FeedAdapter.ClickListener.LikeClick -> viewModel.handleLikeClick(clickListener.post, clickListener.position)
        is FeedAdapter.ClickListener.ImageClick -> viewModel.handleImageClick(clickListener.post, clickListener.position)
        is FeedAdapter.ClickListener.CommentClick -> {
          val i = Intent(this@FeedActivity, CommentsActivity::class.java)
          Cache.put(KEY_DATA, clickListener.post)
          startActivity(i)
        }
      }
    }
  }
  
  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    when (requestCode) {
      1 -> {
        if (grantResults.isNotEmpty()) {
          if (processPermissionResponse()) {
            showGallery()
          }
        }
      }
    }
  }
  
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    when (requestCode) {
      Define.ALBUM_REQUEST_CODE -> if (resultCode == Activity.RESULT_OK) {
        uris = data?.getParcelableArrayListExtra<Uri>(Define.INTENT_PATH) as ArrayList<Uri>
        savePost()
      }
    }
  }
  
  private fun savePost() {
    upLoader.show()
    val post = Post(FirebaseAuth.getInstance().currentUser!!.uid)
    PostManager.getInstance().createOrUpdatePostWithImage(uris[0], post)
        .subscribeOn(Schedulers.io())
        .subscribe({
          upLoader.hide()
        }, {
          upLoader.hide()
          it.printStackTrace()
        })
  }
  
  private fun showGallery() {
    uris.clear()
    FishBun.with(this)
        .setImageAdapter(GlideAdapter())
        .setSelectedImages(uris)
        .prepareActionBar(this)
        .setCounts()
        .setDefaults()
        .setDrawables(this)
        .setTexts(this)
        .startAlbum()
  }
}