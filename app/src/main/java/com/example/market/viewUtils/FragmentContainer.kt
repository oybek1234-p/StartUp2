package com.example.market.viewUtils
//
//import android.content.Context
//import android.util.AttributeSet
//import android.view.MotionEvent
//import android.widget.FrameLayout
//
//class FragmentContainer @JvmOverloads constructor(context: Context, attributeSet: AttributeSet?=null, deffStyle: Int= 0) : FrameLayout(context,attributeSet,deffStyle) {
//    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
//        return (context as MainActivity).fragmentController!!.onTouchEvent(ev)
//    }
//
//    override fun onTouchEvent(event: MotionEvent?): Boolean {
//        val touch = (context as MainActivity).fragmentController!!.onTouchEvent(event)
//        return !touch
//    }
//}