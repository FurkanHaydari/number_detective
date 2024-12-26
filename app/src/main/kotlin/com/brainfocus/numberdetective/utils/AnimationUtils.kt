package com.brainfocus.numberdetective.utils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

object AnimationUtils {
    fun playButtonClickAnimation(view: View) {
        AnimatorSet().apply {
            play(createScaleAnimation(view, "scaleX", 0.9f, 1f))
                .with(createScaleAnimation(view, "scaleY", 0.9f, 1f))
            interpolator = AccelerateDecelerateInterpolator()
            duration = 150
            start()
        }
    }
    
    fun playSuccessAnimation(view: View) {
        AnimatorSet().apply {
            playSequentially(
                createBounceAnimation(view),
                createRotationAnimation(view)
            )
            start()
        }
    }

    private fun createScaleAnimation(view: View, property: String, from: Float, to: Float) =
        ObjectAnimator.ofFloat(view, property, from, to)

    private fun createBounceAnimation(view: View) =
        ObjectAnimator.ofFloat(view, "translationY", 0f, -30f, 0f).apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
        }

    private fun createRotationAnimation(view: View) =
        ObjectAnimator.ofFloat(view, "rotation", 0f, 360f).apply {
            duration = 500
            interpolator = AccelerateDecelerateInterpolator()
        }
} 