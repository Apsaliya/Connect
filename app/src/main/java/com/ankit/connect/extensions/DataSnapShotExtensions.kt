@file:Suppress("NOTHING_TO_INLINE")

package com.ankit.connect.extensions

import com.ankit.connect.data.model.Post
import com.ankit.connect.data.model.PostListResult
import com.ankit.connect.store.FirebaseDbHelper.Companion.isPostValid
import com.google.firebase.database.DataSnapshot
import timber.log.Timber
import java.util.*

/**
 * Created by ankit on 14/04/18.
 */
inline fun DataSnapshot.toPostListResult(objectMap: Map<String, Any>): PostListResult {
  val result = PostListResult()
  val list = ArrayList<Post>()
  var isMoreDataAvailable = true
  var lastItemCreatedDate: Long = 0
  
  isMoreDataAvailable = 10 == objectMap.size
  
  for (key in objectMap.keys) {
    val obj = objectMap[key]
    if (obj is Map<*, *>) {
      val mapObj = obj as Map<String, Any>
      
      if (!isPostValid(mapObj)) {
        Timber.e("Invalid post")
        continue
      }
      
      val createdDate = mapObj["createdDate"] as Long
      
      if (lastItemCreatedDate == 0L || lastItemCreatedDate > createdDate) {
        lastItemCreatedDate = createdDate
      }
      
      val post = Post()
      post.id = key
      post.imagePath = (mapObj["imagePath"] as String)
      post.imageTitle = (mapObj["imageTitle"] as String)
      post.authorId = (mapObj["authorId"] as String)
      post.createdDate = (createdDate)
      if (mapObj.containsKey("commentsCount")) {
        post.commentsCount = (mapObj["commentsCount"] as Long)
      }
      if (mapObj.containsKey("likesCount")) {
        post.likesCount = (mapObj["likesCount"] as Long)
      }
      list.add(post)
    }
  }
  
  list.sortWith(Comparator { lhs: Post?, rhs: Post? -> (rhs?.createdDate as Long).compareTo(lhs?.createdDate!!) })
  
  result.setPosts(list)
  result.setLastItemCreatedDate(lastItemCreatedDate)
  result.setMoreDataAvailable(isMoreDataAvailable)
  return result
}