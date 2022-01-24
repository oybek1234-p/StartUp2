package com.example.market.utils

import android.text.format.DateUtils

object DateUtils : DateUtils() {
    private fun calculateDifference(param: Long): LongArray {
        var paramLong = param
        val l1 = paramLong / 86400000L
        var l2 = paramLong % 86400000L
        paramLong = l2 / 3600000L
        var l3 = l2 % 3600000L
        l2 = l3 / 60000L
        l3 %= 60000L
        val l4 = l3 / 1000L
        return longArrayOf(l1, paramLong, l2, l4)
    }

}