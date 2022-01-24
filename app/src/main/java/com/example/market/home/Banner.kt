package com.example.market.home

import androidx.annotation.Keep

@Keep
class Banner {
    var id = ""
    var url = ""
    constructor(id: String,photo: String) {
        this.id = id
        this.url = photo
    }
    constructor()
}