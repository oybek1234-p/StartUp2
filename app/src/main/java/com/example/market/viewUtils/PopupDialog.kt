package com.example.market.viewUtils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.*
import androidx.core.animation.addPauseListener
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import androidx.core.view.setPadding
import androidx.fragment.app.FragmentController
import com.example.market.MainActivity
import com.example.market.MyApplication
import com.example.market.R
import com.example.market.utils.AndroidUtilities
import com.example.market.utils.getDrawable
const val DIALOG_ANIMATION_FROM_ANGLE = 0
const val DIALOG_ANIMATION_ALERT_DIALOG = 1

class PopupDialog (private val popupWindowLayout: PopupWindowLayout, var animtionType: Int = DIALOG_ANIMATION_FROM_ANGLE) : PopupWindow(popupWindowLayout,WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT) {
    init {
        isOutsideTouchable = true
        isFocusable = true
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindowLayout.dissmis = {
            dismiss()
        }
    }

    private var show = false
    private var animation = ValueAnimator.ofFloat(0f,1f).apply {
        duration = 200
        popupWindowLayout.apply {
            addUpdateListener {
                val animatedValue = it.animatedValue as Float
                alpha = animatedValue
                if (animtionType== DIALOG_ANIMATION_FROM_ANGLE) {
                    scaleX = 0.5f + 0.5f * animatedValue
                    scaleY = 0.5f + 0.5f * animatedValue
                    translationY = -100 + 100*animatedValue
                    translationX = 100 + -100*animatedValue
                } else {
                    scaleX = 0.9f + 0.1f * animatedValue
                    scaleY = 0.9f + 0.1f * animatedValue
                }
                dimBehind(0.6f * animatedValue)
                if (animatedValue==0f && !show) {
                    super.dismiss()
                }
            }
        }
    }

    override fun dismiss() {
        show = false
        animation.reverse()
    }

    val windowMargin = AndroidUtilities.dp(48f).toFloat()

    private fun dimBehind(dimAmount: Float = 0.6f) {
        try {
            val c = contentView.rootView
            val context = contentView.context
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val p = c.layoutParams as WindowManager.LayoutParams
            p.flags = p.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
            p.dimAmount = dimAmount
            if (animtionType == DIALOG_ANIMATION_ALERT_DIALOG) {
                p.width = (MyApplication.displaySize.first - windowMargin).toInt()
                p.gravity = Gravity.CENTER
            }
            wm.updateViewLayout(c, p)
        } catch (e: Exception) {

        }
    }

    fun show(view: View,gravity: Int,x: Int,y: Int,dimBehind: Boolean = true) {
        show = true
        showAtLocation(view,gravity,x, y)
        animation.start()
    }
}