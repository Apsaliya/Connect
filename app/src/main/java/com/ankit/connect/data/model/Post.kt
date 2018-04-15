package com.ankit.connect.data.model

import java.util.*

/**
 * Created by ankit on 12/04/18.
 */
data class Post(var title: String? = null) {
  var id: String? = null
  var description: String? = null
  var createdDate: Long = 0
  var imagePath: String? = null
  var imageTitle: String? = null
  var authorId: String? = null
  var authorName: String? = null
  var commentsCount: Long = 0
  var likesCount: Long = 0
  
  init {
    this.createdDate = Date().time
  }
  
  
}