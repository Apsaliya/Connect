package com.ankit.connect.util.managers

import android.net.Uri
import android.util.Log
import com.ankit.connect.data.model.Post
import com.ankit.connect.extensions.generateImageTitle
import com.ankit.connect.store.FirebaseDbHelper
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.UploadTask
import io.reactivex.Single
import timber.log.Timber

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
        Timber.d("successful upload image, image url: " + downloadUrl.toString())
    
        post.imagePath = downloadUrl.toString()
        post.imageTitle = imageTitle
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