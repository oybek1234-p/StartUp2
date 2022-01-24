package com.example.market.model

import androidx.recyclerview.widget.RecyclerView

class Header {
    var id = ""
    var title = ""
    var subtitle = ""
    var hasArrow = false
    var itemType = RecyclerView.INVALID_TYPE
    var category = ""

    constructor(id: String,title: String,subtitle: String,hasArrow: Boolean,itemType: Int,category: String) {
        this.id = id
        this.title = title
        this.subtitle = subtitle
        this.hasArrow = hasArrow
        this.itemType = itemType
        this.category = category
    }
    constructor()
}