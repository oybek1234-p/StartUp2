package com.org.ui.components

import android.animation.Animator
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import java.text.DecimalFormat


/**
 * Currency formatter
 */ val formatter = DecimalFormat("#,###")

/**
 * Base inflater for all views
 */
lateinit var appInflater: LayoutInflater
/**
 * Inflates binding types
 */
fun <T: ViewDataBinding> inflateBinding(
    parent: ViewGroup?,
    @LayoutRes layId: Int,
    attachToParent: Boolean = false,
) :T {
    return DataBindingUtil.inflate(
        appInflater,
        layId,
        parent,
        attachToParent
    )
}

/**
 *Anim types:
 * 0 - Fade
 * 1 - Scale
 */
@BindingAdapter("visibleOrGone","type", requireAll = false)
fun View.visibleOrGone(visibleOrGone: Boolean,type: Int = -1) {
    if (type!=-1) {
        animate().cancel()
        visibility = View.VISIBLE
        scaleY = 1f
        scaleX = 1f
        var anim = animate().alpha(if (visibleOrGone) 1f else 0f).setDuration(if (visibleOrGone) 300 else 150)
        if (type == 1) {
            val scale = if (visibleOrGone) 1f else 0.8f
            anim = anim.scaleX(scale).scaleY(scale).setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {

                }

                override fun onAnimationEnd(animation: Animator?) {
                    if (!visibleOrGone) {
                        visibility = View.GONE
                    }
                }

                override fun onAnimationCancel(animation: Animator?) {

                }

                override fun onAnimationRepeat(animation: Animator?) {

                }
            })
        }
        anim.start()

    } else {
        visibility = if (visibleOrGone) View.VISIBLE else View.GONE
    }
}