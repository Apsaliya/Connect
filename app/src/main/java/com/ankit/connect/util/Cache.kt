package com.ankit.connect.util

import android.support.v4.util.LruCache

/**
 * Created by ankit on 15/04/18.
 */
object Cache {
  private val cacheData = LruCache<String, Any>(80)
    fun put(id :String,data : Any) {
        this.cacheData.put(id, data)
    }
  
    fun get(id  :String)  :Any {
        return this.cacheData.get(id)
    }
}