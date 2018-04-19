package com.ankit.connect.feature.feed.models

import java.util.*

/**
 * Created by ankit on 12/04/18.
 */
data class Post(var authorId: String? = null) {
  var id: String? = null
  var createdDate: Long = 0
  var imagePath: String? = null
  var imageTitle: String? = null
  var authorName: String? = null
  var commentsCount: Long = 0
  var likesCount: Long = 0
  var isLiked: Boolean? = null
  
  init {
    this.createdDate = Date().time
  }
  
  companion object {
    const val KEY_ID = "id"
    const val KEY_AUTHOR_ID = "authorId"
    const val KEY_CREATED_DATE = "createdDate"
    const val KEY_IMAGEPATH = "imagePath"
    const val KEY_IMAGETITLE = "imageTitle"
    const val KEY_AUTHORNAME = "authorName"
    const val KEY_COMMENT_COUNT = "commentsCount"
    const val KEY_LIKE_COUNT = "likesCount"
  }
}