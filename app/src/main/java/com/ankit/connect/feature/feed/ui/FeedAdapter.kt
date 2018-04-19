package com.ankit.connect.feature.feed.ui

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import com.ankit.connect.R
import android.view.ViewGroup
import com.ankit.connect.feature.feed.models.Post
import com.ankit.connect.extensions.hide
import com.ankit.connect.extensions.show
import com.ankit.connect.util.FormatterUtil.getRelativeTimeSpanString
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.item_feed.view.*
import java.util.concurrent.TimeUnit

/**
 * Created by ankit on 15/04/18.
 */
internal class FeedAdapter(var context: Context, var posts: ArrayList<Post>, val viewModel: FeedViewModel) : RecyclerView.Adapter<FeedAdapter.ViewHolder>() {
  
  lateinit var itemClickListener: OnItemClickListener
  
  override fun getItemCount() = posts.size
  
  companion object {
    const val ACTION_LIKE_BUTTON_CLICKED = "action_like_button_button"
    const val ACTION_DEFAULT = "action_default"
    const val ACTION_LIKE_IMAGE_CLICKED = "action_like_image_button"
  }
  
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_feed, parent, false)
    return ViewHolder(itemView)
  }
  
  fun dispatchUpdates(newPosts: ArrayList<Post>) {
    posts = newPosts
    notifyDataSetChanged()
  }
  
  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val post = posts[position]
    holder.itemView.spinKitImage.show()
    Glide.with(context)
        .load(post.imagePath)
        .listener(object : RequestListener<Drawable> {
          override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
            holder.itemView.spinKitImage.hide()
            return false
          }
          
          override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
            holder.itemView.spinKitImage.hide()
            return false
          }
        })
        .into(holder.itemView.ivFeedCenter)
    holder.itemView.likesCount.text = post.likesCount.toString()
    holder.itemView.ivUserProfile.text = post.authorName
    holder.itemView.timeStamp.text = getRelativeTimeSpanString(context, post.createdDate)
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
  
  override fun onViewRecycled(holder: ViewHolder) {
    super.onViewRecycled(holder)
    holder.itemView.spinKitImage.hide()
    holder.itemView.ivFeedCenter.setImageDrawable(null)
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
  
  interface OnItemClickListener {
    fun onItemClick(clickListener: ClickListener)
  }
  
  sealed class ClickListener {
    data class LikeClick(val post: Post, val position: Int) : ClickListener()
    data class ImageClick(val post: Post, val position: Int) : ClickListener()
    data class CommentClick(val post: Post) : ClickListener()
  }
}