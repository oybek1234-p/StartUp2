package com.example.market.utils
import androidx.annotation.StringRes
import com.example.market.MyApplication
import com.example.market.R
import com.example.market.binding.formatCurrency
import com.example.market.model.Product
import java.lang.StringBuilder
import kotlin.math.cos



fun getString(@StringRes id: Int): String {
    return MyApplication.appContext.getString(id)
}

fun getShipping(): String {
    return MyApplication.appContext.getString(R.string.shipping)
}

fun joinWithSpace(vararg string: String) : String {
    return StringBuilder().apply {
        for (index in string.indices) {
            append(string[index])
            if (index==string.size-1){
                continue
            }
            append(" ")
        }

    }.toString()
}