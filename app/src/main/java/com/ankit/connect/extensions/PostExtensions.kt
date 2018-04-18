@file:Suppress("NOTHING_TO_INLINE")

package com.ankit.connect.extensions

import com.ankit.connect.feature.feed.models.Post
import com.ankit.connect.feature.feed.models.Post.Companion.KEY_AUTHORNAME
import com.ankit.connect.feature.feed.models.Post.Companion.KEY_AUTHOR_ID
import com.ankit.connect.feature.feed.models.Post.Companion.KEY_COMMENT_COUNT
import com.ankit.connect.feature.feed.models.Post.Companion.KEY_CREATED_DATE
import com.ankit.connect.feature.feed.models.Post.Companion.KEY_IMAGEPATH
import com.ankit.connect.feature.feed.models.Post.Companion.KEY_IMAGETITLE
import com.ankit.connect.feature.feed.models.Post.Companion.KEY_LIKE_COUNT
import com.ankit.connect.util.FormatterUtil
import java.util.*

/**
 * Created by ankit on 14/04/18.
 */
inline fun Post.generateImageTitle() : String {
  return if (id != null) {
    "post_$id"
  } else id.toString() + Date().time
  
}

inline fun Post.toMap(): Map<String, Any> {
  val result = HashMap<String, Any>()
  
  createdDate.let { result.put(KEY_CREATED_DATE, it) }
  imagePath?.let { result.put(KEY_IMAGEPATH, it) }
  imageTitle?.let { result.put(KEY_IMAGETITLE, it) }
  authorId?.let { result.put(KEY_AUTHOR_ID, it) }
  authorName?.let { result.put(KEY_AUTHORNAME, it) }
  result[KEY_COMMENT_COUNT] = commentsCount
  result[KEY_LIKE_COUNT] = likesCount
  
  return result
}
