package com.example.market.viewUtils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.animation.addPauseListener
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import androidx.core.view.setPadding
import androidx.fragment.app.FragmentController
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
        interpolator = com.example.market.navigation.FragmentController.decelerateInterpolator
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
                    scaleX = 0.8f + 0.2f * animatedValue
                    scaleY = 0.8f + 0.2f * animatedValue
                }
                if (animatedValue==0f&&!show) {
                    super.dismiss()
                }
            }
        }
    }

    override fun dismiss() {
        show = false
        animation.reverse()
    }
    private fun dimBehind() {
        val c = contentView.rootView
        val context = contentView.context
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val p = c.layoutParams as WindowManager.LayoutParams
        p.flags = p.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
        p.dimAmount = 0.2f
        wm.updateViewLayout(c, p)
    }

    fun show(view: View,gravity: Int,x: Int,y: Int,dimBehind: Boolean) {
        show = true
        showAtLocation(view,gravity,x, y)
        animation.start()
        if (dimBehind){
            dimBehind()
        }
    }
}