package com.example.market.viewUtils

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ScrollView

class ScrollView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet, deffStyle: Int= 0) : ScrollView(context,attributeSet,deffStyle) {
var canScroll = true
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return canScroll && super.onInterceptTouchEvent(ev)
    }
}