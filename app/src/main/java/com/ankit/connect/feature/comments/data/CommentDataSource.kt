package com.ankit.connect.feature.comments.data

import com.ankit.connect.feature.comments.model.Comment
import io.reactivex.Flowable
import io.reactivex.Single

/**
 * Created by ankit on 19/04/18.
 */
internal interface CommentDataSource {
  fun getComments(id: String): Flowable<List<Comment>>
  fun sendComment(commentText: String, id:String): Single<Boolean>
}