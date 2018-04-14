package com.ankit.connect.data.model

import java.util.*

/**
 * Created by ankit on 14/04/18.
 */
class Comment {
  private var id: String? = null
  private var text: String? = null
  private var authorId: String? = null
  private var createdDate: Long = 0
  
  
  fun Comment() {
    // Default constructor required for calls to DataSnapshot.getValue(Comment.class)
  }
  
  fun Comment(text: String) {
    
    this.text = text
    this.createdDate = Calendar.getInstance().timeInMillis
  }
  
  fun getId(): String? {
    return id
  }
  
  fun setId(id: String) {
    this.id = id
  }
  
  fun getText(): String? {
    return text
  }
  
  fun setText(text: String) {
    this.text = text
  }
  
  fun getAuthorId(): String? {
    return authorId
  }
  
  fun setAuthorId(authorId: String) {
    this.authorId = authorId
  }
  
  fun getCreatedDate(): Long {
    return createdDate
  }
  
  fun setCreatedDate(createdDate: Long) {
    this.createdDate = createdDate
  }
}