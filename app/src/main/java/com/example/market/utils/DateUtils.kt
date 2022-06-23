package com.example.market.utils
//
//import android.text.format.DateUtils
//import com.example.market.models.Date
//
//object DateUtils : DateUtils() {
//
//    /*
//    l1 - days
//    l2 - min
//    l4 - seconds
//    paramLong - hours
//     */
//    private fun calculateDifference(param: Long): Date {
//        var paramLong = param
//        val l1 = paramLong / 86400000L
//        var l2 = paramLong % 86400000L
//        paramLong = l2 / 3600000L
//        var l3 = l2 % 3600000L
//        l2 = l3 / 60000L
//        l3 %= 60000L
//        val l4 = l3 / 1000L
//
//        return Date().apply {
//            days = l1
//            hours = paramLong
//            minutes = l2
//            seconds = l4
//        }
//    }
//
//    fun calculateDifference(currentTime: Long,lastTime: Long): Date {
//        return calculateDifference(currentTime - lastTime)
//    }
//}