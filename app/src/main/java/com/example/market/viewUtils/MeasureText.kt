package com.example.market.viewUtils

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet

class MeasureText  @JvmOverloads constructor(context: Context, attributeSet: AttributeSet, deffStyle: Int= 0) : androidx.appcompat.widget.AppCompatTextView(context,attributeSet,deffStyle) {
    init {
        isSelected = true
    }
}

