package com.example.market.viewUtils

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.market.MyApplication
import com.example.market.utils.log

class RecyclerView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet, deffStyle: Int= 0) : RecyclerView(context,attributeSet,deffStyle) {
    var canScroll = true

    override fun onInterceptTouchEvent(e: MotionEvent?): Boolean {
        return canScroll && super.onInterceptTouchEvent(e)
    }
}