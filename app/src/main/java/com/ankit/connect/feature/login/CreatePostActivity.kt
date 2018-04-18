@file:Suppress("MemberVisibilityCanBePrivate")

package com.ankit.connect.feature.login

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import com.ankit.connect.util.Cache
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearLayoutManager.VERTICAL
import android.widget.Toast
import com.ankit.connect.R
import com.ankit.connect.data.model.Post
import com.ankit.connect.extensions.*
import com.ankit.connect.feature.login.profile.PostDetailActivity
import com.ankit.connect.feature.login.profile.PostsAdapter
import com.ankit.connect.util.ItemAnimator
import com.ankit.connect.util.managers.PostManager
import com.google.firebase.auth.FirebaseAuth
import com.sangcomz.fishbun.FishBun
import com.sangcomz.fishbun.adapter.image.impl.GlideAdapter
import com.sangcomz.fishbun.define.Define
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.post.*
import kotlinx.android.synthetic.main.toolbar_main.*
import java.util.ArrayList

/**
 * Created by ankit on 14/04/18.
 */
class CreatePostActivity : AppCompatActivity() {
  
  private var uris = ArrayList<Uri>()
  private lateinit var viewModel: CreatePostViewModel
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.post)
    viewModel = ViewModelProviders.of(this).get(CreatePostViewModel::class.java)
  
    list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    list.itemAnimator = ItemAnimator()
    fab.setOnClickListener {
      if (checkStoragePermission()) {
        showGallery()
      }
    }
    
    spinKitFeed.show()
    viewModel.getAllNotices()
    
    viewModel.viewState.observe(this, Observer {
      if (it?.showError!!) {
        if (it.errorMessage != null) {
          showSnackBar(it.errorMessage)
        } else {
          showSnackBar("Something went wrong")
        }
      }
      
      if (it.posts != null) {
        spinKitFeed.hide()
        val adapter = list.adapter
        if (adapter != null) {
          (adapter as PostsAdapter).dispatchUpdates(it.posts)
        } else {
          val newAdapter = PostsAdapter(this@CreatePostActivity, it.posts, viewModel)
          list.layoutManager = LinearLayoutManager(this@CreatePostActivity, VERTICAL, false)
          newAdapter.itemClickListener = listener
          list.itemAnimator = ItemAnimator()
          list.adapter = newAdapter
        }
      }
      
      if (it.single != null && list.adapter != null) {
        if (it.single.second != PostsAdapter.ACTION_DEFAULT) {
          list.adapter.notifyItemChanged(it.single.first, it.single.second)
        } else {
          list.adapter.notifyItemChanged(it.single.first)
        }
      }
    })
  }
  
  internal val listener = object : PostsAdapter.OnItemClickListener {
    override fun onItemClick(clickListener: PostsAdapter.ClickListener) {
      when (clickListener) {
        is PostsAdapter.ClickListener.LikeClick -> viewModel.handleLikeClick(clickListener.post, clickListener.position)
        is PostsAdapter.ClickListener.ImageClick -> viewModel.handleImageClick(clickListener.post, clickListener.position)
        is PostsAdapter.ClickListener.CommentClick -> {
          val i = Intent(this@CreatePostActivity, PostDetailActivity::class.java)
          Cache.put("data", clickListener.post)
          startActivity(i)
        }
      }
    }
  }
  
  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    when (requestCode) {
      1 -> {
        if (grantResults.isNotEmpty()) {
          if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // permission was granted, yay!
            showGallery()
          } else {
            Toast.makeText(this, R.string.permission_deny_msg, Toast.LENGTH_SHORT).show()
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