package com.example.market.utils

import android.graphics.Canvas
import com.example.market.R
import com.google.android.material.shape.MaterialShapeDrawable

class CustomBadgeDrawable : MaterialShapeDrawable() {
    val drawable = getDrawable(R.drawable.heart_confetti)
    override fun draw(canvas: Canvas) {
     //   super.draw(canvas)

        drawable?.apply {
            colorFilter = this@CustomBadgeDrawable.colorFilter
            tintList = this@CustomBadgeDrawable.tintList
            bounds = this@CustomBadgeDrawable.bounds
            alpha = this@CustomBadgeDrawable.alpha
            draw(canvas)
        }
    }
}