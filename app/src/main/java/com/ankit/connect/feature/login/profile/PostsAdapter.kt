package com.ankit.connect.feature.login.profile

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import com.ankit.connect.R
import android.view.ViewGroup
import com.ankit.connect.data.model.Post
import com.ankit.connect.feature.login.CreatePostViewModel
import com.bumptech.glide.Glide
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.item_feed.view.*
import java.util.concurrent.TimeUnit

/**
 * Created by ankit on 15/04/18.
 */
internal class PostsAdapter(var context: Context, var posts: ArrayList<Post>, val viewModel: CreatePostViewModel) : RecyclerView.Adapter<PostsAdapter.ViewHolder>() {
  
  lateinit var itemClickListener: OnItemClickListener
  
  override fun getItemCount() = posts.size
  
  companion object {
    const val ACTION_LIKE_BUTTON_CLICKED = "action_like_button_button"
    const val ACTION_DEFAULT = "action_default"
    const val ACTION_LIKE_IMAGE_CLICKED = "action_like_image_button"
  }
  
  override fun setHasStableIds(hasStableIds: Boolean) {
    super.setHasStableIds(true)
  }
  
  
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_feed, parent, false)
    return ViewHolder(itemView)
  }
  
  fun dispatchUpdates(newPosts: ArrayList<Post>) {
    posts.clear()
    posts = newPosts
  }
  
  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val post = posts[position]
    Glide.with(context).load(post.imagePath).into(holder.itemView.ivFeedCenter)
    holder.itemView.likesCount.text = post.likesCount.toString()
    if (post.isLiked != null) {
      if (post.isLiked!!) {
        holder.itemView.btnLike.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_heart_red))
      } else {
        holder.itemView.btnLike.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_heart_outline_grey))
      }
    } else {
      viewModel.getLikeRemote(post, position)
    }
    holder.itemView.tag = post
  }
  
  inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    init {
      itemView.ivFeedCenter.clicks()
          .subscribe {
            itemClickListener.onItemClick(ClickListener.ImageClick(itemView.tag as Post, adapterPosition))
          }
      itemView.btnComments.clicks()
          .subscribe {
            itemClickListener.onItemClick(ClickListener.CommentClick(itemView.tag as Post))
          }
      itemView.btnLike.clicks()
          .debounce(300, TimeUnit.MILLISECONDS)
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe {
            itemClickListener.onItemClick(ClickListener.LikeClick(itemView.tag as Post, adapterPosition))
          }
    }
  }
  
  override fun getItemId(position: Int): Long {
    return posts[position].createdDate + position
  }
  
  interface OnItemClickListener {
    fun onItemClick(clickListener: ClickListener)
  }
  
  sealed class ClickListener {
    data class LikeClick(val post: Post, val position: Int): ClickListener()
    data class ImageClick(val post: Post,val position: Int): ClickListener()
    data class CommentClick(val post: Post): ClickListener()
  }
}