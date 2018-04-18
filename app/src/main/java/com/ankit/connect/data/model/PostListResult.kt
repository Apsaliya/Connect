package com.ankit.connect.data.model

import java.util.ArrayList

/**
 * Created by ankit on 14/04/18.
 */
class PostListResult {
  internal var isMoreDataAvailable: Boolean = false
  internal var posts = ArrayList<Post>()
  internal var lastItemCreatedDate: Long = 0
  
  fun isMoreDataAvailable(): Boolean {
    return isMoreDataAvailable
  }
  
  fun setMoreDataAvailable(moreDataAvailable: Boolean) {
    isMoreDataAvailable = moreDataAvailable
  }
  
  fun getPosts(): ArrayList<Post> {
    return posts
  }
  
  fun setPosts(posts: ArrayList<Post>) {
    this.posts = posts
  }
  
  fun getLastItemCreatedDate(): Long {
    return lastItemCreatedDate
  }
  
  fun setLastItemCreatedDate(lastItemCreatedDate: Long) {
    this.lastItemCreatedDate = lastItemCreatedDate
  }
}