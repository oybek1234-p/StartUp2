package com.example.market.utils
//import android.widget.TextView
//import androidx.annotation.StringRes
//import com.example.market.MyApplication
//import com.example.market.R
//import java.lang.StringBuilder
//
//
//fun getString(@StringRes id: Int): String {
//    return MyApplication.appContext.getString(id)
//}
//
//fun getShipping(): String {
//    return MyApplication.appContext.getString(R.string.shipping)
//}
//
//fun joinWithSpace(vararg string: String) : String {
//    return StringBuilder().apply {
//        for (index in string.indices) {
//            append(string[index])
//            if (index==string.size-1){
//                continue
//            }
//            append(" ")
//        }
//
//    }.toString()
//}
//
//fun replaceMultiple(text: String,array: Array<Char>,replace: String) : String {
//    val stringBuilder = StringBuilder().apply {
//        text.forEach { c ->
//            if (array.contains(c)) {
//                append(replace)
//            } else {
//                append(c)
//            }
//        }
//    }
//    return stringBuilder.toString()
//}