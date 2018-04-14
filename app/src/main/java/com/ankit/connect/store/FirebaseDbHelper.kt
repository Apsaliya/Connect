package com.ankit.connect.store

import android.net.Uri
import android.util.Log
import com.ankit.connect.data.model.Post
import com.ankit.connect.extensions.toMap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.UploadTask
import java.util.HashMap

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
  
  fun addRegistrationToken(token: String?, userId: String) {
    val databaseReference = getDatabaseReference()
    val task = databaseReference.child("profiles").child(userId).child("notificationTokens").child(token!!).setValue(true)
    task.addOnCompleteListener({ task1 ->
    })
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