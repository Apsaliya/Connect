package com.ankit.connect.feature.comments.data

import com.ankit.connect.feature.comments.model.Comment
import com.ankit.connect.store.FirebaseDbHelper
import io.reactivex.Flowable
import io.reactivex.Single

/**
 * Created by ankit on 19/04/18.
 */
class RemoteDataSource : CommentDataSource {
  override fun getComments(id: String): Flowable<List<Comment>> = FirebaseDbHelper.getInstance().getCommentsList(id)
  
  override fun sendComment(commentText: String, id: String): Single<Boolean> = FirebaseDbHelper.getInstance().createComment(commentText, id)
}