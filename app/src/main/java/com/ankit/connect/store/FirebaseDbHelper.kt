package com.ankit.connect.store

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

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