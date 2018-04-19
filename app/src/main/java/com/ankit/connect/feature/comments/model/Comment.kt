package com.ankit.connect.feature.comments.model

import java.util.*

/**
 * Created by ankit on 14/04/18.
 */
class Comment {
  var id: String? = null
  var text: String? = null
  var authorId: String? = null
  var authorName: String? = null
  private var createdDate: Long = 0
  
  
  constructor(){
    // Default constructor required for calls to DataSnapshot.getValue(Comment.class)
  }
  
  constructor(text: String) {
    
    this.text = text
    this.createdDate = Calendar.getInstance().timeInMillis
  }
  
  fun getCreatedDate(): Long {
    return createdDate
  }
}