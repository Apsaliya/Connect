package com.ankit.connect.feature.login

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.ankit.connect.R
import com.ankit.connect.data.model.Comment
import com.ankit.connect.data.model.Post
import com.ankit.connect.feature.login.profile.PostsAdapter
import kotlinx.android.synthetic.main.item_comment.view.*
import kotlinx.android.synthetic.main.item_feed.view.*

/**
 * Created by ankit on 15/04/18.
 */
class CommentsAdapter(val comments:  List<Comment>) : RecyclerView.Adapter<CommentsAdapter.ViewHolder>() {
  override fun onBindViewHolder(holder: CommentsAdapter.ViewHolder, position: Int) {
    val comment = comments[position]
    holder.itemView.author_name.text = comment.authorName
    holder.itemView.comment_text.text = comment.text
    holder.itemView.timestamp.text = comment.getCreatedDate().toString()
  }
  
  override fun getItemCount() = comments.size
  
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsAdapter.ViewHolder {
    val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
    return ViewHolder(itemView)
  }
  
  inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
  
}