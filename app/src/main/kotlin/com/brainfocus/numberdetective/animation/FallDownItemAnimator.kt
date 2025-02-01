package com.brainfocus.numberdetective.animation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

class FallDownItemAnimator : DefaultItemAnimator() {
    override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
        holder.itemView.alpha = 0f
        holder.itemView.translationY = -holder.itemView.height * 0.2f
        holder.itemView.scaleX = 1.05f
        holder.itemView.scaleY = 1.05f

        val animatorSet = android.animation.AnimatorSet()
        
        val translateAnimator = ObjectAnimator.ofFloat(holder.itemView, View.TRANSLATION_Y, 0f)
        translateAnimator.interpolator = DecelerateInterpolator()
        
        val alphaAnimator = ObjectAnimator.ofFloat(holder.itemView, View.ALPHA, 1f)
        alphaAnimator.interpolator = DecelerateInterpolator()
        
        val scaleXAnimator = ObjectAnimator.ofFloat(holder.itemView, View.SCALE_X, 1f)
        scaleXAnimator.interpolator = DecelerateInterpolator()
        
        val scaleYAnimator = ObjectAnimator.ofFloat(holder.itemView, View.SCALE_Y, 1f)
        scaleYAnimator.interpolator = DecelerateInterpolator()

        animatorSet.playTogether(translateAnimator, alphaAnimator, scaleXAnimator, scaleYAnimator)
        animatorSet.duration = 300
        
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                dispatchAddStarting(holder)
            }

            override fun onAnimationEnd(animation: Animator) {
                dispatchAddFinished(holder)
            }

            override fun onAnimationCancel(animation: Animator) {
                clearAnimatedValues(holder.itemView)
            }
        })

        animatorSet.start()
        return true
    }

    private fun clearAnimatedValues(view: View) {
        view.alpha = 1f
        view.translationY = 0f
        view.scaleX = 1f
        view.scaleY = 1f
    }

    override fun animateRemove(holder: RecyclerView.ViewHolder): Boolean {
        dispatchRemoveFinished(holder)
        return false
    }
}
