package com.ankit.connect.feature.feed.data

import com.ankit.connect.feature.feed.models.PostListResult
import io.reactivex.Flowable
import io.reactivex.Single

/**
 * Created by ankit on 19/04/18.
 */
internal interface FeedDataSource {
  fun getAllNotices(): Flowable<PostListResult>
  fun getLikeRemote(id: String, uid: String): Single<Boolean>
  fun createLike(id: String, uid: String): Single<Boolean>
  fun removeLike(id: String, uid: String): Single<Boolean>
}