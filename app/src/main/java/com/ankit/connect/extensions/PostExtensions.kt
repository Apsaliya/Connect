@file:Suppress("NOTHING_TO_INLINE")

package com.ankit.connect.extensions

import com.ankit.connect.data.model.Post
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
  
  title?.let { result.put("title", it) }
  description?.let { result.put("description", it) }
  createdDate.let { result.put("createdDate", it) }
  imagePath?.let { result.put("imagePath", it) }
  imageTitle?.let { result.put("imageTitle", it) }
  authorId?.let { result.put("authorId", it) }
  result["commentsCount"] = commentsCount
  result["likesCount"] = likesCount
  result["createdDateText"] = FormatterUtil.getFirebaseDateFormat().format(Date(createdDate))
  
  return result
}