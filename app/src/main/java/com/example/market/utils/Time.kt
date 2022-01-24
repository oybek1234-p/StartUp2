package com.example.market.utils

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

fun getCurrentTime() : Date {
    return Calendar.getInstance().time
}

fun getCurrentTimeString(): String {
    val date = Calendar.getInstance().time
    val dateFormat: DateFormat = SimpleDateFormat("yyyy-mm-dd hh:mm:ss")
    return dateFormat.format(date)
}

fun getCurrentDateTimestamp(): Long {
    return Calendar.getInstance().time.time
}