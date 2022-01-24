package com.example.market.viewUtils

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import com.example.market.MainActivity
import com.example.market.model.MESSAGE_TYPE_LIKE
import com.example.market.model.MESSAGE_TYPE_MESSAGE
import com.example.market.model.MESSAGE_TYPE_SUBSCRIBE
import com.example.market.navigation.FragmentController
import com.example.market.navigation.bottomNavVisiblity
import com.example.market.utils.heartDrawableConfeti
import com.google.android.material.bottomnavigation.BottomNavigationView

class FragmentContainer @JvmOverloads constructor(context: Context, attributeSet: AttributeSet?=null, deffStyle: Int= 0) : FrameLayout(context,attributeSet,deffStyle) {
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return (context as MainActivity).fragmentController!!.onTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touch = (context as MainActivity).fragmentController!!.onTouchEvent(event)
        return !touch
    }
}