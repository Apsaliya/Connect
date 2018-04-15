package com.ankit.connect.data.model

import java.util.*

/**
 * Created by ankit on 14/04/18.
 */
class Comment {
  var id: String? = null
  var text: String? = null
  var authorId: String? = null
  private var createdDate: Long = 0
  
  
  fun Comment() {
    // Default constructor required for calls to DataSnapshot.getValue(Comment.class)
  }
  
  fun Comment(text: String) {
    
    this.text = text
    this.createdDate = Calendar.getInstance().timeInMillis
  }
  
  fun getCreatedDate(): Long {
    return createdDate
  }
  
  fun setCreatedDate(createdDate: Long) {
    this.createdDate = createdDate
  }
}