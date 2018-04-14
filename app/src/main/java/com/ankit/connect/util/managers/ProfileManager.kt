package com.ankit.connect.util.managers

import android.content.Context
import android.net.Uri
import com.ankit.connect.store.FirebaseDbHelper
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.UploadTask
import io.reactivex.Single
import java.util.HashMap

/**
 * Created by ankit on 13/04/18.
 */
class ProfileManager private constructor() {
  private val databaseHelper: FirebaseDbHelper = FirebaseDbHelper.getInstance()
  private val activeListeners = HashMap<Context, List<ValueEventListener>>()
  
  fun getProfileExists(id: String): Single<Boolean> {
    val databaseReference = databaseHelper.getDatabaseReference().child("profiles").child(id)
    return Single.create<Boolean> {
      databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
          it.onSuccess(dataSnapshot.exists())
          //
        }
    
        override fun onCancelled(databaseError: DatabaseError) {
          it.onSuccess(false)
        }
      })
    }
  }
  
  companion object {
    
    private var singleton: ProfileManager? = null
    
    
    fun getInstance(): ProfileManager {
      if (singleton == null) {
        singleton = ProfileManager()
      }
      
      return singleton!!
    }
  }
}