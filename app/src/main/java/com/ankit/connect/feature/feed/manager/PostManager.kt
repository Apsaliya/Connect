package com.ankit.connect.feature.feed.manager

import android.net.Uri
import com.ankit.connect.feature.feed.models.Post
import com.ankit.connect.extensions.generateImageTitle
import com.ankit.connect.store.FirebaseDbHelper
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.Single

/**
 * Created by ankit on 14/04/18.
 */
class PostManager {
  
  fun createOrUpdatePostWithImage(imageUri: Uri, post: Post) : Single<Boolean> {
    return Single.create<Boolean> { e ->
      // Register observers to listen for when the download is done or if it fails
      val databaseHelper = FirebaseDbHelper.getInstance()
      if (post.id == null) {
        post.id = databaseHelper.generatePostId()
      }
  
      val imageTitle = post.generateImageTitle()
      val uploadTask = databaseHelper.uploadImage(imageUri, imageTitle)
  
      uploadTask.addOnFailureListener({
        // Handle unsuccessful uploads
        e.onError(it)
      }).addOnSuccessListener({ taskSnapshot ->
        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
        val downloadUrl = taskSnapshot.downloadUrl
    
        post.imagePath = downloadUrl.toString()
        post.imageTitle = imageTitle
        post.authorName = FirebaseAuth.getInstance().currentUser?.displayName
        createOrUpdatePost(post)
        e.onSuccess(true)
      })
    }
  }
  
  fun createOrUpdatePost(post: Post) {
    FirebaseDbHelper.getInstance().createOrUpdatePost(post)
  }
  
  companion object {
    
    private var singleton: PostManager? = null
    fun getInstance(): PostManager {
      if (singleton == null) {
        singleton = PostManager()
      }
      
      return singleton!!
    }
  }
}