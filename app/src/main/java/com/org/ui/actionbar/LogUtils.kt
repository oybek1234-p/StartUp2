package com.ActionBar

import android.util.Log
import com.example.market.BuildConfig
import java.lang.Exception

fun log(exception: Exception?) {

}
fun log(message: String) {
    Log.i(BuildConfig.APPLICATION_ID,message)
}

fun logW(warning: String) {

}