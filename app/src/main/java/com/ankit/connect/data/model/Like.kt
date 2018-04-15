package com.ankit.connect.data.model

import java.util.*

/**
 * Created by ankit on 15/04/18.
 */
class Like {
  private var id: String? = null
  lateinit var authorId: String
  var createdDate: Long? = null
  
  
  constructor() {
    // Default constructor required for calls to DataSnapshot.getValue(Like.class)
  }
  
  constructor(authorId: String) {
    this.authorId = authorId
    this.createdDate = Calendar.getInstance().timeInMillis
  }
  
  fun getId(): String? {
    return id
  }
  
  fun setId(id: String) {
    this.id = id
  }
}