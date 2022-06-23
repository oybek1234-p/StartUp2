package com.org.market

import android.content.Context
import com.example.market.R
import com.example.market.databinding.HomeProductItemBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.org.net.models.*
import com.org.ui.components.load
import com.org.ui.components.visibleOrGone
import java.text.DecimalFormat

enum class Currency(val currency: String) {
    USZ("UZS"),
    USD("USD")
}

var dollarRate = 0.000093
val formatter = DecimalFormat("#,###")
val currency = Currency.USZ

fun formatCurrency(string: String?): String {
    return try {
        if (string == null) return ""
        formatCurrency(string.toLong())
    } catch (e: Exception) {
        ""
    }
}

fun formatCurrency(long: Long): String {
    return if (long == 0L)
        getApplicationContext().getString(R.string.free)
    else formatter.format(
        if (currency == Currency.USZ) long
        else (long.toDouble() * dollarRate)) + " ${currency.currency}"
}

fun getDiscount(cost: Long, percent: Float) = formatCurrency(cost)

fun getDiscountPercent(percentValue: Float) = "-${percentValue}%"

fun getShippingText(cost: String?) = "Shipping: ${formatCurrency(cost)}"

fun getUserStatus(status: Int) = if (status == 0) "offline" else "online"

fun getHashTag(text: String) = "#${text.trim()}"

fun getMessageTextForType(type: Int): String {
    return when(type) {
        MESSAGE_TYPE_PRODUCT_LIKED -> "liked your product"
        MESSAGE_TYPE_SUBSCRIBED -> "started following you"
        MESSAGE_TYPE_MESSAGE -> "received new message"
        MESSAGE_TYPE_COMMENTED -> "commented on you"
        MESSAGE_TYPE_ALL -> "unusual message"
        else -> getMessageTextForType(MESSAGE_TYPE_MESSAGE)
    }
}

fun getDateText(type: Int): String {
    return getString(when (type) {
        DATE_NOW -> R.string.now
        DATE_TODAY -> R.string.today
        DATE_YESTERDAY -> R.string.yesterday
        DATE_THIS_WEEK -> R.string.this_week
        DATE_THIS_MONTH -> R.string.this_month
        DATE_THIS_YEAR -> R.string.this_year
        DATE_NEW -> R.string.new_
        else -> R.string.long_time_ago
    })
}


