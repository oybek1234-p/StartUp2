package com.example.market.utils

import com.example.market.CURRENCY_UZS
import com.example.market.MyApplication
import com.example.market.R

/**
 * Currency type
 */
var currency = CURRENCY_UZS

/**
 * Get currency value
 */
fun getCurrency(): String {
    return if (currency == CURRENCY_UZS)
        MyApplication.appContext.getString(R.string.sum)
    else
        MyApplication.appContext.getString(R.string.dollar)
}

