package com.example.market.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

fun openCallView(mobileNumber: String,context: Context) {
    val intent = Intent(Intent.ACTION_DIAL)
    intent.data = Uri.parse("tel:$mobileNumber")
    context.startActivity(intent)
}