package com.brainfocus.numberdetective.ui

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

class FallDownItemAnimator : DefaultItemAnimator() {
    override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
        holder.itemView.alpha = 0f
        holder.itemView.translationY = -holder.itemView.height.toFloat()
        
        val animatorSet = AnimatorSet()
        val translateAnimator = ObjectAnimator.ofFloat(holder.itemView, View.TRANSLATION_Y, 0f)
        val alphaAnimator = ObjectAnimator.ofFloat(holder.itemView, View.ALPHA, 1f)
        
        animatorSet.playTogether(translateAnimator, alphaAnimator)
        animatorSet.duration = 300
        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                dispatchAddFinished(holder)
            }
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        animatorSet.start()
        return true
    }
    
    override fun animateRemove(holder: RecyclerView.ViewHolder): Boolean {
        val animatorSet = AnimatorSet()
        val translateAnimator = ObjectAnimator.ofFloat(holder.itemView, View.TRANSLATION_Y, holder.itemView.height.toFloat())
        val alphaAnimator = ObjectAnimator.ofFloat(holder.itemView, View.ALPHA, 0f)
        
        animatorSet.playTogether(translateAnimator, alphaAnimator)
        animatorSet.duration = 300
        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                dispatchRemoveFinished(holder)
            }
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        animatorSet.start()
        return true
    }
}
