package com.ankit.connect.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import com.ankit.connect.extensions.getScreenHeight
import com.ankit.connect.feature.login.profile.PostsAdapter
import kotlinx.android.synthetic.main.item_feed.view.*
import java.util.HashMap
import com.ankit.connect.R
import com.ankit.connect.data.model.Post

/**
 * Created by ankit on 15/04/18.
 */
class ItemAnimator: DefaultItemAnimator() {
  private val DECCELERATE_INTERPOLATOR = DecelerateInterpolator()
  private val ACCELERATE_INTERPOLATOR = AccelerateInterpolator()
  private val OVERSHOOT_INTERPOLATOR = OvershootInterpolator(4f)
  
  internal var likeAnimationsMap: MutableMap<RecyclerView.ViewHolder, AnimatorSet> = HashMap()
  internal var heartAnimationsMap: MutableMap<RecyclerView.ViewHolder, AnimatorSet> = HashMap()
  
  private var lastAddAnimatedItem = -2
  
  override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
    return true
  }
  
  override fun recordPreLayoutInformation(state: RecyclerView.State,
                                          viewHolder: RecyclerView.ViewHolder,
                                          changeFlags: Int, payloads: List<Any>): RecyclerView.ItemAnimator.ItemHolderInfo {
    if (changeFlags == RecyclerView.ItemAnimator.FLAG_CHANGED) {
      for (payload in payloads) {
        if (payload is String) {
          return FeedItemHolderInfo(payload)
        }
      }
    }
    
    return super.recordPreLayoutInformation(state, viewHolder, changeFlags, payloads)
  }
  
  override fun animateAdd(viewHolder: RecyclerView.ViewHolder): Boolean {
      if (viewHolder.layoutPosition > lastAddAnimatedItem) {
        lastAddAnimatedItem++
        runEnterAnimation(viewHolder as PostsAdapter.ViewHolder)
        return false
    }
    
    dispatchAddFinished(viewHolder)
    return false
  }
  
  private fun runEnterAnimation(holder: PostsAdapter.ViewHolder) {
    val screenHeight = holder.itemView.context.getScreenHeight()
    holder.itemView.translationY = screenHeight.toFloat()
    holder.itemView.animate()
        .translationY(0.toFloat())
        .setInterpolator(DecelerateInterpolator(3f))
        .setDuration(700)
        .setListener(object : AnimatorListenerAdapter() {
          override fun onAnimationEnd(animation: Animator) {
            dispatchAddFinished(holder)
          }
        })
        .start()
  }
  
  override fun animateChange(oldHolder: RecyclerView.ViewHolder,
                             newHolder: RecyclerView.ViewHolder,
                             preInfo: RecyclerView.ItemAnimator.ItemHolderInfo,
                             postInfo: RecyclerView.ItemAnimator.ItemHolderInfo): Boolean {
    cancelCurrentAnimationIfExists(newHolder)
    
    if (preInfo is FeedItemHolderInfo) {
      val holder = newHolder as PostsAdapter.ViewHolder
      val post = holder.itemView.tag as Post
      
      animateHeartButton(holder)
      //updateLikesCounter(holder, post.likesCount.toInt())
      if (PostsAdapter.ACTION_LIKE_IMAGE_CLICKED == preInfo.updateAction) {
        animatePhotoLike(holder)
      }
    }
    
    return false
  }
  
  private fun cancelCurrentAnimationIfExists(item: RecyclerView.ViewHolder) {
    if (likeAnimationsMap.containsKey(item)) {
      likeAnimationsMap[item]?.cancel()
    }
    if (heartAnimationsMap.containsKey(item)) {
      heartAnimationsMap[item]?.cancel()
    }
  }
  
  private fun animateHeartButton(holder: PostsAdapter.ViewHolder) {
    val animatorSet = AnimatorSet()
    
    val rotationAnim = ObjectAnimator.ofFloat(holder.itemView.btnLike, "rotation", 0f, 360f)
    rotationAnim.duration = 300
    rotationAnim.interpolator = ACCELERATE_INTERPOLATOR
    
    val bounceAnimX = ObjectAnimator.ofFloat(holder.itemView.btnLike, "scaleX", 0.2f, 1f)
    bounceAnimX.duration = 300
    bounceAnimX.interpolator = OVERSHOOT_INTERPOLATOR
    
    val bounceAnimY = ObjectAnimator.ofFloat(holder.itemView.btnLike, "scaleY", 0.2f, 1f)
    bounceAnimY.duration = 300
    bounceAnimY.interpolator = OVERSHOOT_INTERPOLATOR
    bounceAnimY.addListener(object : AnimatorListenerAdapter() {
      override fun onAnimationStart(animation: Animator) {
        holder.itemView.btnLike.setImageResource(R.drawable.ic_heart_red)
      }
      
      override fun onAnimationEnd(animation: Animator) {
        heartAnimationsMap.remove(holder)
        dispatchChangeFinishedIfAllAnimationsEnded(holder)
      }
    })
    
    animatorSet.play(bounceAnimX).with(bounceAnimY).after(rotationAnim)
    animatorSet.start()
    
    heartAnimationsMap[holder] = animatorSet
  }
  
  private fun animatePhotoLike(holder: PostsAdapter.ViewHolder) {
    holder.itemView.vBgLike.visibility = View.VISIBLE
    holder.itemView.ivLike.visibility = View.VISIBLE
  
    holder.itemView.vBgLike.scaleY = 0.1f
    holder.itemView.vBgLike.scaleX = 0.1f
    holder.itemView.vBgLike.alpha = 1f
    holder.itemView.ivLike.scaleY = 0.1f
    holder.itemView.ivLike.scaleX = 0.1f
    
    val animatorSet = AnimatorSet()
    
    val bgScaleYAnim = ObjectAnimator.ofFloat(holder.itemView.vBgLike, "scaleY", 0.1f, 1f)
    bgScaleYAnim.duration = 200
    bgScaleYAnim.interpolator = DECCELERATE_INTERPOLATOR
    val bgScaleXAnim = ObjectAnimator.ofFloat(holder.itemView.vBgLike, "scaleX", 0.1f, 1f)
    bgScaleXAnim.duration = 200
    bgScaleXAnim.interpolator = DECCELERATE_INTERPOLATOR
    val bgAlphaAnim = ObjectAnimator.ofFloat(holder.itemView.vBgLike, "alpha", 1f, 0f)
    bgAlphaAnim.duration = 200
    bgAlphaAnim.startDelay = 150
    bgAlphaAnim.interpolator = DECCELERATE_INTERPOLATOR
    
    val imgScaleUpYAnim = ObjectAnimator.ofFloat(holder.itemView.ivLike, "scaleY", 0.1f, 1f)
    imgScaleUpYAnim.duration = 300
    imgScaleUpYAnim.interpolator = DECCELERATE_INTERPOLATOR
    val imgScaleUpXAnim = ObjectAnimator.ofFloat(holder.itemView.ivLike, "scaleX", 0.1f, 1f)
    imgScaleUpXAnim.duration = 300
    imgScaleUpXAnim.interpolator = DECCELERATE_INTERPOLATOR
    
    val imgScaleDownYAnim = ObjectAnimator.ofFloat(holder.itemView.ivLike, "scaleY", 1f, 0f)
    imgScaleDownYAnim.duration = 300
    imgScaleDownYAnim.interpolator = ACCELERATE_INTERPOLATOR
    val imgScaleDownXAnim = ObjectAnimator.ofFloat(holder.itemView.ivLike, "scaleX", 1f, 0f)
    imgScaleDownXAnim.duration = 300
    imgScaleDownXAnim.interpolator = ACCELERATE_INTERPOLATOR
    
    animatorSet.playTogether(bgScaleYAnim, bgScaleXAnim, bgAlphaAnim, imgScaleUpYAnim, imgScaleUpXAnim)
    animatorSet.play(imgScaleDownYAnim).with(imgScaleDownXAnim).after(imgScaleUpYAnim)
    
    animatorSet.addListener(object : AnimatorListenerAdapter() {
      override fun onAnimationEnd(animation: Animator) {
        likeAnimationsMap.remove(holder)
        resetLikeAnimationState(holder)
        dispatchChangeFinishedIfAllAnimationsEnded(holder)
      }
    })
    animatorSet.start()
    
    likeAnimationsMap[holder] = animatorSet
  }
  
  private fun dispatchChangeFinishedIfAllAnimationsEnded(holder: PostsAdapter.ViewHolder) {
    if (likeAnimationsMap.containsKey(holder) || heartAnimationsMap.containsKey(holder)) {
      return
    }
    
    dispatchAnimationFinished(holder)
  }
  
  private fun resetLikeAnimationState(holder: PostsAdapter.ViewHolder) {
    holder.itemView.vBgLike.visibility = View.INVISIBLE
    holder.itemView.ivLike.visibility = View.INVISIBLE
  }
  
  override fun endAnimation(item: RecyclerView.ViewHolder) {
    super.endAnimation(item)
    cancelCurrentAnimationIfExists(item)
  }
  
  override fun endAnimations() {
    super.endAnimations()
    for (animatorSet in likeAnimationsMap.values) {
      animatorSet.cancel()
    }
  }
  
  class FeedItemHolderInfo(var updateAction: String) : RecyclerView.ItemAnimator.ItemHolderInfo()
}