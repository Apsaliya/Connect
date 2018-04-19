package com.ankit.connect.extensions

import android.support.v7.widget.AppCompatEditText
import android.text.Editable
import android.text.TextWatcher

/**
 * Created by ankit on 19/04/18.
 */
inline fun AppCompatEditText.onTextChanged(crossinline action: (s: CharSequence?) -> Unit) {
  addTextChangedListener(object : TextWatcher {
    override fun afterTextChanged(s: Editable?) {
      //no-op
    }
    
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
      //no-op
    }
    
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
      action(s)
    }
  })
}