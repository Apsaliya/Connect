package com.ankit.connect.util

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by ankit on 14/04/18.
 */
object FormatterUtil {
  
  var firebaseDBDate = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
  
  @SuppressLint("SimpleDateFormat")
  fun getFirebaseDateFormat(): SimpleDateFormat {
    val cbDateFormat = SimpleDateFormat(firebaseDBDate)
    cbDateFormat.timeZone = TimeZone.getTimeZone("UTC")
    return cbDateFormat
  }
}