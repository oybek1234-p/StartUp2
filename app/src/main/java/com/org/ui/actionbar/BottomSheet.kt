package com.org.ui.actionbar

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.databinding.ViewDataBinding
import com.example.market.R
import com.example.market.databinding.BottomSheetContainerBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.org.market.getDrawable
import com.org.ui.components.inflateBinding
import com.org.ui.components.visibleOrGone

open class BottomSheet<T : ViewDataBinding>(context: Context, val layId: Int) :
    BottomSheetDialog(context) {

    var containerBinding: BottomSheetContainerBinding? = null
    fun containerView() = containerBinding!!.bottomSheetContainer

    var sheetElevation = 8f
    var dimAmount = 0.6f

    var backgroundColor = context.getThemeColor(R.attr.colorSurface)
    set(value) {
        field = value
        containerBinding?.root?.backgroundTintList = ColorStateList.valueOf(backgroundColor)
    }

    var binding: T? = null
    fun contentView() = binding!!.root
    fun requireBinding() = binding!!

    var overshootInterpolator = OvershootInterpolator(0.7f)
    var accelerateDecelerateInterpolator = AccelerateDecelerateInterpolator()

    fun title() = containerBinding!!.titleView
    fun button() = containerBinding!!.actionButton

    var hasActionBar = false
        set(value) {
            field = value
            containerBinding?.actionBar?.visibleOrGone(value)
        }

    fun updateThemeStyle() {
        containerBinding?.apply {
            bottomSheetContainer.setBackgroundColor(
                context.getThemeColor(R.attr.colorSurface)
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        containerBinding = inflateBinding<BottomSheetContainerBinding>(
            null,
            R.layout.bottom_sheet_container
        ).apply {
            executePendingBindings()
        }
        hasActionBar = true

        binding = inflateBinding(containerView(), layId)

        containerView().apply {
            addView(contentView())
            onViewCreated(binding!!)
            setContentView(containerView(), contentView().layoutParams)
        }

        window!!.apply {
            setBackgroundDrawable(null)
            setElevation(sheetElevation)
            setDimAmount(dimAmount)
        }
    }

    fun superDismiss() = super.dismiss()

    open fun onViewCreated(binding: T) {}
    open fun onDismiss() {}
    open fun onAnimate(progress: Float, translationY: Int) {}
}