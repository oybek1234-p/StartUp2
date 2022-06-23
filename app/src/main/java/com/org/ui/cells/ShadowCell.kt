package com.org.ui.cells

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import com.example.market.R
import com.org.market.getDrawable

class ShadowCell constructor(context: Context) : View(context) {
    init {
        background = shadowDrawable
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), shadowDrawable.intrinsicHeight)
    }

    companion object {
        val shadowDrawable:Drawable = getDrawable(R.drawable.header_shadow)!!
    }
}