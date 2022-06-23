package com.org.market

import android.content.Context
import android.content.SharedPreferences

fun getSharedPreference(
    name: String,
    mode: Int = Context.MODE_PRIVATE
): SharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences(name,mode)

const val MAIN_CONFIG = "mainConfig"
fun mainSharedPreferences() = getSharedPreference(MAIN_CONFIG)

var fontSize = 16
var currentTheme = 0
var dayOrNightTheme = false
var configLoaded = false

fun loadMainConfig() {
    if (configLoaded) {
        return
    }

    mainSharedPreferences().apply {
        fontSize = getInt("fontSize",16)
        currentTheme = getInt("currentTheme",0)
        dayOrNightTheme = getBoolean("dayOrNightTheme",true)
    }
    configLoaded = true
}

fun saveConfig() {
    mainSharedPreferences().edit().apply {
        putInt("fontSize", fontSize)
        putInt("currentTheme", currentTheme)
        putBoolean("dayOrNightTheme", dayOrNightTheme)
        apply()
    }
}

fun clearConfig() {
    fontSize = 16
    currentTheme = 0
    dayOrNightTheme = true
    saveConfig()
}

