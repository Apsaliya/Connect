@file:Suppress("NOTHING_TO_INLINE")

package com.ankit.connect.extensions

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE

/**
 * Created by ankit on 16/04/18.
 */
inline fun View.show() {
  visibility = VISIBLE
}

inline fun View.hide() {
  visibility = GONE
}