package com.ankit.connect.data.model

import java.util.ArrayList

/**
 * Created by ankit on 14/04/18.
 */
class PostListResult {
  internal var isMoreDataAvailable: Boolean = false
  internal var posts: List<Post> = ArrayList()
  internal var lastItemCreatedDate: Long = 0
  
  fun isMoreDataAvailable(): Boolean {
    return isMoreDataAvailable
  }
  
  fun setMoreDataAvailable(moreDataAvailable: Boolean) {
    isMoreDataAvailable = moreDataAvailable
  }
  
  fun getPosts(): List<Post> {
    return posts
  }
  
  fun setPosts(posts: List<Post>) {
    this.posts = posts
  }
  
  fun getLastItemCreatedDate(): Long {
    return lastItemCreatedDate
  }
  
  fun setLastItemCreatedDate(lastItemCreatedDate: Long) {
    this.lastItemCreatedDate = lastItemCreatedDate
  }
}