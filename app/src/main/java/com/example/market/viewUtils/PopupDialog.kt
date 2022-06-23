package com.example.market.viewUtils
//
//import android.animation.ValueAnimator
//import android.content.Context
//
//import android.graphics.Color
//import android.graphics.drawable.ColorDrawable
//import android.view.Gravity
//import android.view.View
//import android.view.WindowManager
//import android.widget.*
//import androidx.core.view.children
//import androidx.core.view.doOnNextLayout
//import com.ActionBar.dp
//import com.example.market.MyApplication
//
//const val DIALOG_ANIMATION_FROM_ANGLE = 0
//const val DIALOG_ANIMATION_ALERT_DIALOG = 1
//
//class PopupDialog (private val popupWindowLayout: PopupWindowLayout, var animtionType: Int = DIALOG_ANIMATION_FROM_ANGLE) : PopupWindow(popupWindowLayout,WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT) {
//
//    init {
//        isOutsideTouchable = true
//        isFocusable = true
//
//        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//
//        popupWindowLayout.dissmis = {
//            dismiss()
//        }
//    }
//
//    private var dimBehind = false
//    private var dimAmount = 0f
//
//    companion object {
//        const val DEFAULT_DIM_AMOUNT = 0.6f
//    }
//
//    fun getWindowSize(): Pair<Int,Int> {
//        popupWindowLayout.apply {
//            val displaySize = MyApplication.displaySize
//            measure(View.MeasureSpec.makeMeasureSpec(displaySize.first,View.MeasureSpec.AT_MOST),View.MeasureSpec.makeMeasureSpec(displaySize.second,View.MeasureSpec.AT_MOST))
//            return Pair(measuredWidth,measuredHeight)
//        }
//    }
//
//    private var show = false
//    private var animation = ValueAnimator.ofFloat(0f,1f).apply {
//        duration = 200
//        popupWindowLayout.apply {
//            addUpdateListener {
//                val animatedValue = it.animatedValue as Float
//                alpha = animatedValue
//                if (animtionType== DIALOG_ANIMATION_FROM_ANGLE) {
//                    scaleX = 0.5f + 0.5f * animatedValue
//                    scaleY = 0.5f + 0.5f * animatedValue
//                    translationY = -100 + 100*animatedValue
//                    translationX = 100 + -100*animatedValue
//                } else {
//                    scaleX = 0.9f + 0.1f * animatedValue
//                    scaleY = 0.9f + 0.1f * animatedValue
//                }
//                dimBehind(dimAmount * animatedValue)
//                if (animatedValue==0f && !show) {
//                    super.dismiss()
//                }
//            }
//        }
//    }
//
//    override fun dismiss() {
//        show = false
//        animation.reverse()
//    }
//
//    val windowMargin = dp(48f).toFloat()
//
//    private fun dimBehind(dimAmount: Float = 0.6f): View {
//        val c = contentView.rootView
//        try {
//            if (dimBehind) {
//                val context = contentView.context
//                val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
//                val p = c.layoutParams as WindowManager.LayoutParams
//                p.flags = p.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
//                p.dimAmount = dimAmount
//                if (animtionType == DIALOG_ANIMATION_ALERT_DIALOG) {
//                    p.width = (MyApplication.displaySize.first - windowMargin).toInt()
//                    p.gravity = Gravity.CENTER
//                }
//                wm.updateViewLayout(c, p)
//            }
//        } catch (e: Exception) {
//            toast(e.message)
//        }
//        return c.also { it.requestLayout() }
//    }
//
//    fun show(parentView: View,gravity: Int,viewForLocation: View,dimBehind: Boolean = true,dimAmount: Float = 0f) {
//        show = true
//        this.dimBehind = dimBehind
//        this.dimAmount = dimAmount ?: DEFAULT_DIM_AMOUNT
//        val array = IntArray(2)
//        viewForLocation.getLocationInWindow(array)
//        popupWindowLayout.apply {
//            measure(View.MeasureSpec.makeMeasureSpec(parentView.width,View.MeasureSpec.AT_MOST),View.MeasureSpec.makeMeasureSpec(parentView.height,View.MeasureSpec.AT_MOST))
//            showAtLocation(parentView,gravity,array.first() - measuredWidth + viewForLocation.width,array.last())
//
//            dimBehind(dimAmount).doOnNextLayout {
//                children.forEach {
//                    it.alpha = 0f
//                }
//                this@PopupDialog.animation.start()
//            }
//        }
//    }
//}