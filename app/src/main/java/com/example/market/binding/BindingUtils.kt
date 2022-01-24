package com.example.market.binding

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.example.market.CURRENCY_UZS
import com.example.market.utils.currency
import com.example.market.utils.getCurrency
import com.example.market.viewUtils.toast
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
    attachToParent: Boolean = false
) :T {
    return DataBindingUtil.inflate(
        appInflater,
        layId,
        parent,
        attachToParent
    )
}

/**
 * Checks texts same or is empty
 * */
fun checkIsNotEmpty(
    oldText: CharSequence?,
    newText: String?
) : Boolean {
    return (oldText.toString()==newText|| TextUtils.isEmpty(newText))
}
var dollarRate = 0.000093

/**
 * Formats currency
 */
fun formatCurrency(long: Long): String {
    return if (long==0L) "Bepul" else formatter.format(if (currency== CURRENCY_UZS) long else (long.toDouble() * dollarRate)) + " ${getCurrency()}"
}
