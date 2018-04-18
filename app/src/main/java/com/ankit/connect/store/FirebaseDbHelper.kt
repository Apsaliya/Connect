package com.ankit.connect.store

import android.net.Uri
import com.ankit.connect.feature.comments.model.Comment
import com.ankit.connect.feature.feed.models.Like
import com.ankit.connect.feature.feed.models.Post
import com.ankit.connect.feature.feed.models.PostListResult
import com.ankit.connect.extensions.toMap
import com.ankit.connect.extensions.toPostListResult
import com.ankit.connect.feature.feed.models.Post.Companion.KEY_CREATED_DATE
import com.ankit.connect.util.Constants.BASE_PATH_IMAGES
import com.ankit.connect.util.Constants.BASE_POSTS_REFRENCE_PATH
import com.ankit.connect.util.Constants.CACHE_CONTROL_STRING
import com.ankit.connect.util.Constants.CHILD_POSTS
import com.ankit.connect.util.Constants.CHILD_POSTS_COMMENTS
import com.ankit.connect.util.Constants.CHILD_POSTS_LIKES
import com.ankit.connect.util.Constants.STORAGE_URL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.UploadTask
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by ankit on 13/04/18.
 */
class FirebaseDbHelper {
  @Suppress("MemberVisibilityCanBePrivate")
  internal var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
  private var database: FirebaseDatabase? = null
  @Suppress("MemberVisibilityCanBePrivate")
  internal var storage: FirebaseStorage
  
  init {
    database = FirebaseDatabase.getInstance()
    database?.setPersistenceEnabled(true)
    storage = FirebaseStorage.getInstance()
    storage.maxUploadRetryTimeMillis = 60000
  }
  
  fun getDatabaseReference(): DatabaseReference {
    return database?.reference!!
  }
  
  fun generatePostId(): String {
    val databaseReference = database?.reference
    return databaseReference?.child(CHILD_POSTS)?.push()?.key!!
  }
  
  fun uploadImage(uri: Uri, imageTitle: String): UploadTask {
    val storageRef = storage.getReferenceFromUrl(STORAGE_URL)
    val riversRef = storageRef.child(BASE_PATH_IMAGES + imageTitle)
    // Create file metadata including the content type
    val metadata = StorageMetadata.Builder()
        .setCacheControl(CACHE_CONTROL_STRING)
        .build()
    
    return riversRef.putFile(uri, metadata)
  }
  
  fun createOrUpdatePost(post: Post) {
    try {
      val databaseReference = database?.reference
      
      val postValues = post.toMap()
      val childUpdates = HashMap<String, Any>()
      childUpdates[BASE_POSTS_REFRENCE_PATH + post.id] = postValues
      
      databaseReference?.updateChildren(childUpdates)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
  
  fun hasCurrentUserLikeSingleValue(postId: String, userId: String) : Single<Boolean> {
    return Single.create<Boolean> {
      val databaseReference = database?.getReference(CHILD_POSTS_LIKES)?.child(postId)?.child(userId)
      databaseReference?.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
          it.onSuccess(dataSnapshot.exists())
        }
    
        override fun onCancelled(databaseError: DatabaseError) {
          it.onError(databaseError.toException())
        }
      })
    }
  }
  
  fun removeLike(postId: String, postAuthorId: String): Single<Boolean> {
    return Single.create<Boolean> {
      val authorId = firebaseAuth.currentUser!!.uid
      val mLikesReference = database?.reference?.child(CHILD_POSTS_LIKES)?.child(postId)?.child(authorId)
      mLikesReference?.removeValue(object : DatabaseReference.CompletionListener {
        override fun onComplete(databaseError: DatabaseError?, databaseReference: DatabaseReference) {
          if (databaseError == null) {
            val postRef = database?.getReference("posts/$postId/likesCount")
            decrementLikesCount(postRef!!)
        
            val profileRef = database?.getReference("profiles/$postAuthorId/likesCount")
            decrementLikesCount(profileRef!!)
          } else {
            it.onError(databaseError.toException())
          }
        }
    
        private fun decrementLikesCount(postRef: DatabaseReference) {
          postRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
              val currentValue = mutableData.getValue(Long::class.java)
              if (currentValue == null) {
                mutableData.value = 0
              } else {
                mutableData.value = currentValue - 1
              }
          
              return Transaction.success(mutableData)
            }
        
            override fun onComplete(databaseError: DatabaseError?, b: Boolean, dataSnapshot: DataSnapshot) {
             it.onSuccess(b)
            }
          })
        }
      })
    }
  }
  
  fun createOrUpdateLike(postId: String, postAuthorId: String): Single<Boolean> {
    return Single.create<Boolean> {
      try {
        val authorId = firebaseAuth.currentUser!!.uid
        val mLikesReference = database?.reference?.child(CHILD_POSTS_LIKES)?.child(postId)?.child(authorId)
        mLikesReference?.push()
        val id = mLikesReference?.push()?.key
        val like = Like(authorId)
        like.setId(id!!)
    
        mLikesReference.child(id).setValue(like, object : DatabaseReference.CompletionListener {
          override fun onComplete(databaseError: DatabaseError?, databaseReference: DatabaseReference) {
            if (databaseError == null) {
              val postRef = database?.getReference("posts/$postId/likesCount")
              incrementLikesCount(postRef!!)
            } else {
              it.onError(databaseError.toException())
            }
          }
      
          private fun incrementLikesCount(postRef: DatabaseReference) {
            postRef.runTransaction(object : Transaction.Handler {
              override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val currentValue = mutableData.getValue(Int::class.java)
                if (currentValue == null) {
                  mutableData.value = 1
                } else {
                  mutableData.value = currentValue + 1
                }
            
                return Transaction.success(mutableData)
              }
          
              override fun onComplete(databaseError: DatabaseError?, b: Boolean, dataSnapshot: DataSnapshot) {
                it.onSuccess(b)
              }
            })
          }
      
        })
      } catch (e: Exception) {
        it.onError(e)
      }
    }
  }
  
  fun getCommentsList(postId: String): Flowable<List<Comment>> {
    
    return Flowable.create<List<Comment>>({
      val databaseReference = database?.getReference(CHILD_POSTS_COMMENTS)?.child(postId)
      val valueEventListener = databaseReference?.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
          val list = ArrayList<Comment>()
          for (snapshot in dataSnapshot.children) {
            val comment = snapshot.getValue(Comment::class.java)
            list.add(comment!!)
          }
  
          list.sortWith(Comparator { lhs, rhs -> rhs.getCreatedDate().compareTo(lhs.getCreatedDate()) })
      
          it.onNext(list)
      
        }
    
        override fun onCancelled(databaseError: DatabaseError) {
          it.onError(databaseError.toException())
        }
      })
    }, BackpressureStrategy.BUFFER)
  }
  
  fun createComment(commentText: String, postId: String) : Single<Boolean> {
    return Single.create<Boolean> {
      try {
        val authorId = firebaseAuth.currentUser!!.uid
        val mCommentsReference = database?.reference?.child("post-comments/$postId")
        val commentId = mCommentsReference?.push()?.key
        val comment = Comment(commentText)
        comment.id = commentId
        comment.authorId = authorId
        comment.authorName = FirebaseAuth.getInstance().currentUser?.displayName
    
        mCommentsReference?.child(commentId)?.setValue(comment, object : DatabaseReference.CompletionListener {
          override fun onComplete(databaseError: DatabaseError?, databaseReference: DatabaseReference) {
            if (databaseError == null) {
              incrementCommentsCount(postId)
            } else {
              it.onError(databaseError.toException())
            }
          }
      
          private fun incrementCommentsCount(postId: String) {
            val postRef = database?.getReference("posts/$postId/commentsCount")
            postRef?.runTransaction(object : Transaction.Handler {
              override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val currentValue = mutableData.getValue(Int::class.java)
                if (currentValue == null) {
                  mutableData.value = 1
                } else {
                  mutableData.value = currentValue + 1
                }
            
                return Transaction.success(mutableData)
              }
          
              override fun onComplete(databaseError: DatabaseError?, b: Boolean, dataSnapshot: DataSnapshot) {
                it.onSuccess(true)
              }
            })
          }
        })
      } catch (e: Exception) {
        e.printStackTrace()
        it.onError(e)
      }
    }
  }
  
  fun getPostList(date: Long): Flowable<PostListResult> {
    return Flowable.create<PostListResult>({
      val databaseReference = database?.getReference(CHILD_POSTS)
      val postsQuery: Query
      postsQuery = databaseReference?.orderByChild(KEY_CREATED_DATE)!!
      
      postsQuery.keepSynced(true)
      postsQuery.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
          val objectMap = dataSnapshot.value as Map<String, Any>?
          val result = dataSnapshot.toPostListResult(objectMap = objectMap!!)
  
          it.onNext(result)
        }
        
        override fun onCancelled(databaseError: DatabaseError) {
          it.onError(databaseError.toException())
        }
      })
    }, BackpressureStrategy.BUFFER)
  }
  
  companion object {
    private var singleton: FirebaseDbHelper? = null
    
    fun getInstance(): FirebaseDbHelper {
      if (singleton == null) {
        singleton = FirebaseDbHelper()
      }
      
      return singleton!!
    }
  }
}