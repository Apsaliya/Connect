package com.ankit.connect.data.model

import java.util.*

/**
 * Created by ankit on 12/04/18.
 */
data class Post(var title: String? = null) {
  private var id: String? = null
  private var description: String? = null
  private var createdDate: Long = 0
  private var imagePath: String? = null
  private var imageTitle: String? = null
  private var authorId: String? = null
  private var commentsCount: Long = 0
  private var likesCount: Long = 0
  
  init {
    this.createdDate = Date().time
  }
  
  
}