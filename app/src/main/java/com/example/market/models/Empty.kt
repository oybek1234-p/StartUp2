package com.example.market.models

data class Empty(
    val title: String = "",
    val subtitle: String = "",
    val buttonText: String = "",
    val buttonClickAction: () -> Unit = {},
    val lottieUrl: String = "",
    val lottieRes: Int = -1,
    val id: Long = System.currentTimeMillis()
    )