package com.example.market.models

class PaymentCard {
    var id = System.currentTimeMillis().toString()
    var userId = ""
    var cardNumber = -1L
    var cvv = -1
    var expiryDate = ""
    var cardHolderName = ""
}