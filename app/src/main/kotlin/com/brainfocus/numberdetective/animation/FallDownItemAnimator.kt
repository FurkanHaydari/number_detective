package com.brainfocus.numberdetective.animation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

class FallDownItemAnimator : DefaultItemAnimator() {
    init {
        // Reduce animation duration for better performance
        addDuration = 200
        removeDuration = 200
        // Disable change animations for better performance
        supportsChangeAnimations = false
    }

    override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
        dispatchAddStarting(holder)
        
        // Reset view properties
        holder.itemView.alpha = 0f
        holder.itemView.translationY = -holder.itemView.height * 0.15f

        // Use a single ValueAnimator for better performance
        val animator = ObjectAnimator.ofFloat(holder.itemView, View.TRANSLATION_Y, 0f).apply {
            duration = addDuration
            interpolator = DecelerateInterpolator()
        }
        
        val alphaAnimator = ObjectAnimator.ofFloat(holder.itemView, View.ALPHA, 1f).apply {
            duration = addDuration
            interpolator = DecelerateInterpolator()
        }

        val animatorSet = android.animation.AnimatorSet().apply {
            playTogether(animator, alphaAnimator)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    dispatchAddFinished(holder)
                }
                override fun onAnimationCancel(animation: Animator) {
                    clearAnimatedValues(holder.itemView)
                }
            })
        }
        
        animatorSet.start()
        return true
    }

    override fun animateRemove(holder: RecyclerView.ViewHolder): Boolean {
        dispatchRemoveStarting(holder)
        
        val animator = ObjectAnimator.ofFloat(holder.itemView, View.TRANSLATION_Y, holder.itemView.height.toFloat()).apply {
            duration = removeDuration
            interpolator = DecelerateInterpolator()
        }
        
        val alphaAnimator = ObjectAnimator.ofFloat(holder.itemView, View.ALPHA, 0f).apply {
            duration = removeDuration
            interpolator = DecelerateInterpolator()
        }

        val animatorSet = android.animation.AnimatorSet().apply {
            playTogether(animator, alphaAnimator)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    dispatchRemoveFinished(holder)
                }
                override fun onAnimationCancel(animation: Animator) {
                    clearAnimatedValues(holder.itemView)
                }
            })
        }
        
        animatorSet.start()
        return true
    }

    private fun clearAnimatedValues(view: View) {
        view.alpha = 1f
        view.translationY = 0f
        view.scaleX = 1f
        view.scaleY = 1f
    }
}
