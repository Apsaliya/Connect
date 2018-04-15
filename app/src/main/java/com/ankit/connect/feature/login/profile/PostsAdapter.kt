package com.ankit.connect.feature.login.profile

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.support.v4.content.ContextCompat
import android.support.v7.graphics.Palette
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import com.ankit.connect.R
import android.view.ViewGroup
import android.widget.ImageView
import com.ankit.connect.data.model.Post
import com.ankit.connect.feature.login.MainActivity
import com.ankit.connect.store.FirebaseDbHelper
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.item_feed.view.*

/**
 * Created by ankit on 15/04/18.
 */
class PostsAdapter(var context: Context, val posts: List<Post>) : RecyclerView.Adapter<PostsAdapter.ViewHolder>() {
  
  lateinit var itemClickListener: OnItemClickListener
  
  override fun getItemCount() = posts.size
  
  companion object {
    const val ACTION_LIKE_BUTTON_CLICKED = "action_like_button_button"
    const val ACTION_LIKE_IMAGE_CLICKED = "action_like_image_button"
  }
  
  
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_feed, parent, false)
    return ViewHolder(itemView)
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
    }
    FirebaseDbHelper.getInstance().hasCurrentUserLikeSingleValue(post.id!!, FirebaseAuth.getInstance().currentUser?.uid!!)
        .subscribeOn(Schedulers.io())
        .subscribe({
          if (it) {
            holder.itemView.btnLike.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_heart_red))
            val currentTag = holder.itemView.tag as Post
            currentTag.isLiked = true
            holder.itemView.tag = currentTag
          } else {
            holder.itemView.btnLike.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_heart_outline_grey))
            val currentTag = holder.itemView.tag as Post
            currentTag.isLiked = false
            holder.itemView.tag = currentTag
          }
        }, {
          it.printStackTrace()
        })
    holder.itemView.tag = post
  }
  
  inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    
    init {
      itemView.ivFeedCenter.setOnClickListener(this)
      itemView.btnLike.setOnClickListener(this)
      itemView.btnComments.setOnClickListener(this)
    }
    
    override fun onClick(view: View) {
      when {
        view.id == itemView.ivFeedCenter.id -> {
          val adapterPosition = adapterPosition
          val post = itemView.tag as Post
          post.likesCount++
          notifyItemChanged(adapterPosition, ACTION_LIKE_IMAGE_CLICKED)
        }
        view.id == itemView.btnLike.id -> {
          val post = itemView.tag as Post
          val currentUser = FirebaseAuth.getInstance().currentUser
          if (post.isLiked == null || (post.isLiked != null && !post.isLiked!!)) {
            post.likesCount++
            val postAtPosition = posts[adapterPosition]
            postAtPosition.isLiked = true
            notifyItemChanged(adapterPosition, ACTION_LIKE_BUTTON_CLICKED)
            FirebaseDbHelper.getInstance().createOrUpdateLike(post.id!!, currentUser?.uid!!)
                .subscribeOn(Schedulers.io())
                .subscribe({
                  if (!it) {
                    post.likesCount--
                    post.isLiked = false
                  }
                }, {
                  post.likesCount--
                  post.isLiked = false
                })
          } else {
            post.likesCount--
            val postAtPosition = posts[adapterPosition]
            postAtPosition.isLiked = false
            notifyItemChanged(adapterPosition)
            FirebaseDbHelper.getInstance().removeLike(post.id!!, currentUser?.uid!!)
                .subscribeOn(Schedulers.io())
                .subscribe({
                  if (!it) {
                    post.likesCount++
                    post.isLiked = true
                  }
                }, {
                  post.likesCount++
                  post.isLiked = true
                })
          }
        }
        view.id == itemView.btnComments.id -> {
          val i = Intent(context, PostDetailActivity::class.java)
          context.startActivity(i)
        }
      }
    }
  }
  
  interface OnItemClickListener {
    fun onItemClick(view: View, post: Post)
  }
  
  fun setOnItemClickListener(itemClickListener: OnItemClickListener) {
    this.itemClickListener = itemClickListener
  }
}