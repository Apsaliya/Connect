package com.ankit.connect.util

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by ankit on 14/04/18.
 */
object FormatterUtil {
  
  var firebaseDBDate = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
  val NOW_TIME_RANGE = DateUtils.MINUTE_IN_MILLIS * 1 // 1 minutes
  
  @SuppressLint("SimpleDateFormat")
  fun getFirebaseDateFormat(): SimpleDateFormat {
    val cbDateFormat = SimpleDateFormat(firebaseDBDate)
    cbDateFormat.timeZone = TimeZone.getTimeZone("UTC")
    return cbDateFormat
  }
  
  fun getRelativeTimeSpanString(context: Context, time: Long): CharSequence {
    val now = System.currentTimeMillis()
    val range = Math.abs(now - time)
    
    return if (range < NOW_TIME_RANGE) {
      "Just now"
    } else DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS)
    
  }
}