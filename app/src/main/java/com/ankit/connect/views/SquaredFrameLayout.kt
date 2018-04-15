package com.ankit.connect.views

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 * Created by ankit on 15/04/18.
 */
class SquaredFrameLayout : FrameLayout {
  
  constructor(context: Context) : super(context)
  
  constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
  
  constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
  
  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)
  
  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    super.onMeasure(widthMeasureSpec, widthMeasureSpec)
  }
}