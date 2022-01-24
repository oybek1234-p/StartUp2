package com.example.market

import android.content.Intent
import android.view.View

data class Alert(
    var title: String,
    var description: String,
    var cancelName: String,
    var actionName: String,
    var cancelOnClick: View.OnClickListener,
    var actionOnClick: View.OnClickListener,
    var iconUrl: String?=null,
    var iconResource: Int?=null
    )