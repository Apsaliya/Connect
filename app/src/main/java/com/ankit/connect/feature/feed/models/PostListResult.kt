package com.ankit.connect.feature.feed.models

import com.ankit.connect.feature.feed.models.Post
import java.util.ArrayList

/**
 * Created by ankit on 14/04/18.
 */
class PostListResult {
  internal var posts = ArrayList<Post>()
  
  fun getPosts(): ArrayList<Post> {
    return posts
  }
  
  fun setPosts(posts: ArrayList<Post>) {
    this.posts = posts
  }
}