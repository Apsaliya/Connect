package com.ankit.connect.store

import android.net.Uri
import com.ankit.connect.data.model.Comment
import com.ankit.connect.data.model.Like
import com.ankit.connect.data.model.Post
import com.ankit.connect.data.model.PostListResult
import com.ankit.connect.extensions.toMap
import com.ankit.connect.extensions.toPostListResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.UploadTask
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import timber.log.Timber
import java.util.*

/**
 * Created by ankit on 13/04/18.
 */
class FirebaseDbHelper {
  internal var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
  private var database: FirebaseDatabase? = null
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
    return databaseReference?.child("posts")?.push()?.key!!
  }
  
  fun uploadImage(uri: Uri, imageTitle: String): UploadTask {
    val storageRef = storage.getReferenceFromUrl("gs://connect-1d914.appspot.com")
    val riversRef = storageRef.child("images/$imageTitle")
    // Create file metadata including the content type
    val metadata = StorageMetadata.Builder()
        .setCacheControl("max-age=7776000, Expires=7776000, public, must-revalidate")
        .build()
    
    return riversRef.putFile(uri, metadata)
  }
  
  fun createOrUpdatePost(post: Post) {
    try {
      val databaseReference = database?.reference
      
      val postValues = post.toMap()
      val childUpdates = HashMap<String, Any>()
      childUpdates["/posts/" + post.id] = postValues
      
      databaseReference?.updateChildren(childUpdates)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
  
  fun hasCurrentUserLikeSingleValue(postId: String, userId: String) : Single<Boolean> {
    return Single.create<Boolean> {
      val databaseReference = database?.getReference("post-likes")?.child(postId)?.child(userId)
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
      val mLikesReference = database?.reference?.child("post-likes")?.child(postId)?.child(authorId)
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
                mutableData.setValue(0)
              } else {
                mutableData.setValue(currentValue - 1)
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
        val mLikesReference = database?.getReference()?.child("post-likes")?.child(postId)?.child(authorId)
        mLikesReference?.push()
        val id = mLikesReference?.push()?.key
        val like = Like(authorId)
        like.setId(id!!)
    
        mLikesReference.child(id).setValue(like, object : DatabaseReference.CompletionListener {
          override fun onComplete(databaseError: DatabaseError?, databaseReference: DatabaseReference) {
            if (databaseError == null) {
              val postRef = database?.getReference("posts/$postId/likesCount")
              incrementLikesCount(postRef!!)
          
              val profileRef = database?.getReference("profiles/$postAuthorId/likesCount")
              incrementLikesCount(profileRef!!)
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
      val databaseReference = database?.getReference("post-comments")?.child(postId)
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
    //activeListeners.put(valueEventListener, databaseReference)
    //return valueEventListener
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
                Timber.d("Updating comments count transaction is completed.")
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
      val databaseReference = database?.getReference("posts")
      val postsQuery: Query
      postsQuery = if (date == 0L) {
        databaseReference?.limitToLast(10)?.orderByChild("createdDate")!!
      } else {
        databaseReference?.limitToLast(10)?.endAt(date.toDouble())?.orderByChild("createdDate")!!
      }
      
      postsQuery.keepSynced(true)
      Timber.d("Adding data listener.")
      postsQuery.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
          Timber.d("onDataChange event received.")
          val objectMap = dataSnapshot.value as Map<String, Any>?
          val result = dataSnapshot.toPostListResult(objectMap = objectMap!!)
          
          if (result.getPosts().isEmpty() && result.isMoreDataAvailable()) {
            getPostList(result.getLastItemCreatedDate() - 1)
          } else {
            Timber.d("list is not empty. invoking subscriber")
            it.onNext(result)
          }
        }
        
        override fun onCancelled(databaseError: DatabaseError) {
          Timber.d("onCancelled event received.")
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