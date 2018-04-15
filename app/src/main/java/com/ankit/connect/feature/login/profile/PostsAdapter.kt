package com.ankit.connect.feature.login.profile

import android.content.Context
import android.graphics.Bitmap
import android.support.v4.content.ContextCompat
import android.support.v7.graphics.Palette
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import com.ankit.connect.R
import android.view.ViewGroup
import com.ankit.connect.data.model.Post
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.android.synthetic.main.item_feed.view.*
import kotlinx.android.synthetic.main.row_places.view.*

/**
 * Created by ankit on 15/04/18.
 */
class PostsAdapter(var context: Context, val posts: List<Post>) : RecyclerView.Adapter<PostsAdapter.ViewHolder>() {
  
  lateinit var itemClickListener: OnItemClickListener
  
  override fun getItemCount() = posts.size
  
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_feed, parent, false)
    return ViewHolder(itemView)
  }
  
  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val post = posts[position]
    holder.itemView.tag = post
    Glide.with(context).load(post.imagePath).into(holder.itemView.ivFeedCenter)
  }
  
  inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    
    init {
      itemView.ivFeedCenter.setOnClickListener(this)
    }
    
    override fun onClick(view: View) = itemClickListener.onItemClick(itemView, itemView.tag as Post)
  }
  
  interface OnItemClickListener {
    fun onItemClick(view: View, post: Post)
  }
  
  fun setOnItemClickListener(itemClickListener: OnItemClickListener) {
    this.itemClickListener = itemClickListener
  }
}