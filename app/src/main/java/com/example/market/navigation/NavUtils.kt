package com.example.market.navigation

import android.animation.ValueAnimator
import android.content.Context
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.addPauseListener
import com.example.market.MainActivity
import com.example.market.utils.AndroidUtilities

private val decelerateInterpolator = DecelerateInterpolator(1.5f)
fun bottomNavVisiblity(context: Context?,visible: Boolean) {

    val animator = ValueAnimator.ofInt(AndroidUtilities.dp(56f),0)
    animator.duration = 200
    animator.interpolator = decelerateInterpolator
        (context as MainActivity).apply {
            val mVisiblity = if (visible) View.VISIBLE else View.INVISIBLE

            animator.addUpdateListener {
                bottomNavigationView.translationY = (it.animatedValue as Int).toFloat()
            }
            animator.addPauseListener{
                bottomNavigationView.visibility = mVisiblity
            }
        }

    if (!visible){
        animator.reverse()
    }else {
        animator.start()
    }


}
