package com.thn.videoconstruction.extentions

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.View
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator


fun View.fadeInAnimation() {
    val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f)
    ObjectAnimator.ofPropertyValuesHolder(this, alpha).apply {
        interpolator = LinearOutSlowInInterpolator()
        duration = 250
    }.start()
}

fun View.scaleAnimation() {
    val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0.8f, 1f)
    val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.8f, 1f)
    val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0.8f, 1f)
    ObjectAnimator.ofPropertyValuesHolder(this, alpha, scaleX, scaleY).apply {
        interpolator = LinearOutSlowInInterpolator()
        duration = 300
    }.start()
}

