package com.ankit.connect.extensions

import android.content.Context
import android.support.v4.content.ContextCompat
import com.ankit.connect.R
import com.sangcomz.fishbun.FishBunCreator

/**
 * Created by ankit on 11/04/18.
 */

@Suppress("NOTHING_TO_INLINE")
inline fun FishBunCreator.prepareActionBar(context: Context): FishBunCreator {
  return setActionBarColor(ContextCompat.getColor(context, R.color.colorPrimary), ContextCompat.getColor(context, R.color.colorPrimaryDark), false)
      .setActionBarTitleColor(ContextCompat.getColor(context, R.color.colorAccent))
}

@Suppress("NOTHING_TO_INLINE")
inline fun FishBunCreator.setTexts(context: Context): FishBunCreator {
  return setAllViewTitle(context.getString(R.string.label_all))
      .setActionBarTitle(context.getString(R.string.label_select_image))
      .textOnNothingSelected(context.getString(R.string.label_no_select))
}

@Suppress("NOTHING_TO_INLINE")
inline fun FishBunCreator.setDrawables(context: Context): FishBunCreator {
  return setHomeAsUpIndicatorDrawable(ContextCompat.getDrawable(context, R.drawable.back))
      .setOkButtonDrawable(ContextCompat.getDrawable(context, R.drawable.done))
}

@Suppress("NOTHING_TO_INLINE")
inline fun FishBunCreator.setDefaults(): FishBunCreator {
  return setButtonInAlbumActivity(false)
      .setReachLimitAutomaticClose(true)
}

@Suppress("NOTHING_TO_INLINE")
inline fun FishBunCreator.setCounts(): FishBunCreator {
  return setMaxCount(1)
      .setPickerSpanCount(5)
      .setReachLimitAutomaticClose(true)
      .setAlbumSpanCount(2, 3)
}