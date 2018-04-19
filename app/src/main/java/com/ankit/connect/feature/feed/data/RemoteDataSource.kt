package com.ankit.connect.feature.feed.data

import com.ankit.connect.feature.feed.models.Post
import com.ankit.connect.feature.feed.models.PostListResult
import com.ankit.connect.store.FirebaseDbHelper
import io.reactivex.Flowable
import io.reactivex.Single

/**
 * Created by ankit on 19/04/18.
 */
class RemoteDataSource : FeedDataSource {
  override fun getAllPosts(): Flowable<PostListResult> = FirebaseDbHelper.getInstance().getPostList(0)
  
  override fun getLikeRemote(id: String, uid: String): Single<Boolean> = FirebaseDbHelper.getInstance().hasCurrentUserLikeSingleValue(id, uid)
  
  override fun createLike(id: String, uid: String): Single<Boolean> = FirebaseDbHelper.getInstance().createOrUpdateLike(id, uid)
  
  override fun removeLike(id: String, uid: String): Single<Boolean> = FirebaseDbHelper.getInstance().removeLike(id, uid)
}