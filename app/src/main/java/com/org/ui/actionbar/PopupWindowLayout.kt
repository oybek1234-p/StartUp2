package com.org.ui.actionbar

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.market.R
import com.org.market.dp
import com.org.market.getDrawable

class PopupWindowLayout @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0,
) : LinearLayout(context, attributeSet, defStyle) {
    var items = HashMap<Int, Item>()
    var dismiss: Runnable? = null

    var backgroundColorTint = Color.WHITE
        set(value) {
            field = value
            backgroundTintMode = PorterDuff.Mode.MULTIPLY
            backgroundTintList = ColorStateList.valueOf(value)
        }

    var itemsIconTint = Color.rgb(103, 106, 111)
        set(value) {
            field = value
            val colorList = ColorStateList.valueOf(value)
            items.values.forEach {
                it.iconView.imageTintList = colorList
            }
        }

    var itemTextColor: Int = Color.BLACK
        set(value) {
            field = value
            items.forEach {
                it.value.titleView.setTextColor(value)
            }
        }

    fun setColors() {
        backgroundColorTint = getThemeColor(R.attr.colorSurface)
        itemsIconTint = getThemeColor(R.attr.colorOnSurfaceMedium)
        itemTextColor = getThemeColor(R.attr.colorOnSurfaceHigh)
    }

    init {
        background = getDrawable(R.drawable.popup_fixed_alert2)
        setColors()

        orientation = VERTICAL
        layoutParams = WindowManager.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    fun startChildAnimation(child: View, show: Boolean) {
        child.apply {
            if (show) {
                alpha = 0f
                translationY = -dp(6f).toFloat()
            }
            animate().alpha(if (show) 1f else 0f).translationY(if (show) 0f else -dp(6f).toFloat())
                .setDuration(if (show) 180 else 70).start()
        }
    }

    fun addItem(id: Int, title: String, iconRes: Int, onClick: (v: View) -> Unit) {
        val item = Item(context).apply {
            iconView.imageTintList = ColorStateList.valueOf(itemsIconTint)
            titleView.setTextColor(itemTextColor)

            setData(title, iconRes)
            setOnClickListener {
                onClick(it)
                dismiss?.run()
            }
            items[id] = this
        }
        addView(item)
    }

    class Item(context: Context) : LinearLayout(context) {
        private val m = dp(18f)

        init {
            background = createSelectorDrawable(Color.GRAY, 4, 10)
            orientation = HORIZONTAL
            setPadding(m, dp(12f), m, dp(12f))
            isClickable = true
            isFocusable = true
        }

        fun setData(title: String, iconRes: Int) {
            titleView.text = title
            iconView.setImageResource(iconRes)
        }

        var iconView = ImageView(context).apply {
            addView(this, LayoutParams(dp(24f), dp(24f)).apply {
                gravity = Gravity.CENTER
            })
        }

        var titleView = TextView(context).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
            addView(this, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                marginStart = dp(12f)
                gravity = Gravity.CENTER_VERTICAL
            })
        }
    }
}