package com.example.market.recycler
import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import com.example.market.utils.log

class MeasureRecyclerView @JvmOverloads constructor(context: Context,attributeSet: AttributeSet?=null,deffstyle:Int = 0):RecyclerView(context,attributeSet,deffstyle)  {
    private var measureCount = 0

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        super.onMeasure(widthSpec, heightSpec)
        measureCount ++
        log("Recyclerview measure count $measureCount")
    }
}