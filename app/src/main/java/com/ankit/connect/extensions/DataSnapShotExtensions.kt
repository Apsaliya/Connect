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
import com.ankit.connect.feature.feed.models.PostListResult
import com.google.firebase.database.DataSnapshot
import java.util.*

/**
 * Created by ankit on 14/04/18.
 */
inline fun DataSnapshot.toPostListResult(objectMap: Map<String, Any>): PostListResult {
  val result = PostListResult()
  val list = ArrayList<Post>()
  
  for (key in objectMap.keys) {
    val obj = objectMap[key]
    if (obj is Map<*, *>) {
      val mapObj = obj as Map<String, Any>
      
      val createdDate = mapObj[KEY_CREATED_DATE] as Long
      
      val post = Post()
      post.id = key
      post.imagePath = (mapObj[KEY_IMAGEPATH] as String)
      post.imageTitle = (mapObj[KEY_IMAGETITLE] as String)
      post.authorId = (mapObj[KEY_AUTHOR_ID] as String)
      post.createdDate = (createdDate)
      if (mapObj.containsKey(KEY_COMMENT_COUNT)) {
        post.commentsCount = (mapObj[KEY_COMMENT_COUNT] as Long)
      }
      if (mapObj.containsKey(KEY_AUTHORNAME)) {
        post.authorName = (mapObj[KEY_AUTHORNAME] as String)
      }
      if (mapObj.containsKey(KEY_LIKE_COUNT)) {
        post.likesCount = (mapObj[KEY_LIKE_COUNT] as Long)
      }
      list.add(post)
    }
  }
  
  list.sortWith(Comparator { lhs: Post?, rhs: Post? -> (rhs?.createdDate as Long).compareTo(lhs?.createdDate!!) })
  
  result.setPosts(list)
  return result
}