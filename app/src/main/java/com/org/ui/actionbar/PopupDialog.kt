package com.org.ui.actionbar

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.*
import com.org.market.*

const val DIALOG_ANIMATION_FROM_ANGLE = 0
const val DIALOG_ANIMATION_ALERT_DIALOG = 1

class PopupDialog(
    private val popupWindowLayout: PopupWindowLayout,
    var animtionType: Int = DIALOG_ANIMATION_FROM_ANGLE,
) : PopupWindow(popupWindowLayout,
    WindowManager.LayoutParams.WRAP_CONTENT,
    WindowManager.LayoutParams.WRAP_CONTENT) {

    private var lastStartedChild = 0
    private var show = false
    private var isAnimating = false
    var dimBehind = true
    var dimAmount = 0.6f
    var popupGravity = Gravity.NO_GRAVITY
    val windowMargin = dp(48f).toFloat()

    init {
        isFocusable = true
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindowLayout.dismiss = Runnable {
            dismiss()
        }
    }

    private var animation = ValueAnimator.ofFloat(0f, 1f)
        .apply {
        interpolator = decelerateInterpolator

        popupWindowLayout.apply {
            doOnStart {
                isAnimating = true
                duration = if (show) 200 else 150
                lastStartedChild = if (show) 0 else childCount
            }
            doOnEnd { isAnimating = false }
            addUpdateListener {
                isAnimating = true
                val animatedValue = it.animatedValue as Float
                alpha = 1f * animatedValue

                if (animtionType == DIALOG_ANIMATION_FROM_ANGLE) {
                    if (show) {
                        scaleX = 0.8f + 0.2f * animatedValue
                        scaleY = 0.8f + 0.2f * animatedValue
                    }
                    translationY = -20 + 20 * animatedValue
                } else {
                    scaleX = 0.9f + 0.1f * animatedValue
                    scaleY = 0.9f + 0.1f * animatedValue
                }

                if (animtionType == DIALOG_ANIMATION_FROM_ANGLE) {
                background?.apply {
                    this.alpha = (150 + (105 * animatedValue).toInt())
                    val drHeight = (height * animatedValue).toInt()

                    setBounds(0, 0, width, drHeight)
                    invalidateSelf()

                        forEachIndexed loop@{ index, child ->
                            child.apply {
                                val childOffset = y + measuredHeight
                                if (show) {
                                    if (index < lastStartedChild) return@loop

                                    if (drHeight > childOffset) {
                                        lastStartedChild = index + 1
                                        startChildAnimation(this, true)
                                    }
                                } else {
                                    if (index >= lastStartedChild) return@loop

                                    if (drHeight < childOffset && drHeight > y) {
                                        lastStartedChild = index
                                        startChildAnimation(this, false)
                                    }
                                } } } } }
                if (animatedValue == 0f && !show) {
                    super.dismiss()
                }
            }
        }
    }

    override fun dismiss() {
        if (isAnimating && !show) {
            return
        }
        show = false
        dimBehind(0f)
        animation?.reverse()
    }

    private fun dimBehind(dimAmount: Float) {
        try {
            val view = contentView.rootView
            val windowLayoutParams = (view.layoutParams as WindowManager.LayoutParams)
                .apply {
                    flags = flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
                    this.dimAmount = dimAmount
                    if (animtionType == DIALOG_ANIMATION_ALERT_DIALOG) {
                        width = (displaySize.x - windowMargin).toInt()
                        gravity = Gravity.CENTER
                    }
                }
            val windowManager =
                (view.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
            windowManager.updateViewLayout(view, windowLayoutParams)

        } catch (e: Exception) {

        }
    }

    fun show(viewForLocation: View? = null,dimAmount: Float = this.dimAmount) {
        if (isAnimating) return
        show = true
        popupWindowLayout.apply {
            val parentView = findActivity(context)!!.window.decorView
            var offsetX = 0
            var offsetY = 0
            val viewForLocationWidth = 0

            if (animtionType == DIALOG_ANIMATION_FROM_ANGLE) {
                viewForLocation?.let {
                    val array = IntArray(2)
                    it.getLocationInWindow(array)
                    offsetX = array.first()
                    offsetY = array.last()
                    measure(View.MeasureSpec.makeMeasureSpec(parentView.width,
                        View.MeasureSpec.AT_MOST),
                        View.MeasureSpec.makeMeasureSpec(parentView.height,
                            View.MeasureSpec.AT_MOST))
                }
            }
            runOnUiThread({
                showAtLocation(
                    parentView,
                    popupGravity,
                    offsetX - (measuredWidth / 1.5f).toInt() + viewForLocationWidth,
                    offsetY
                )

                dimBehind(dimAmount)
                this@PopupDialog.animation?.start()
            })
        }
    }

}