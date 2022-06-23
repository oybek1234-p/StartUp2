package com.org.ui.cells

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.market.R
import com.org.ui.actionbar.getThemeColor

class ConstraintLayoutCell @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet?=null,
    defStyle: Int = 0
) : ConstraintLayout(context,attr,defStyle) {

    var dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 1f
    }

    var needDivider = false
    set(value) {
        field = value
        dividerPaint.color = getThemeColor(R.attr.colorBackground)
        setWillNotDraw(!value)
        invalidate()
    }

    var dividerMarginStart = 0f
    set(value) {
        field = value
        invalidate()
    }

    var dividerMarginEnd = 0f
    set(value) {
        field = value
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (needDivider) {
            canvas?.drawLine(
                dividerMarginStart,
                (measuredHeight - 1).toFloat(),
                measuredWidth - dividerMarginEnd,
                (measuredHeight - 1).toFloat(),
                dividerPaint
            )
        }
    }
}