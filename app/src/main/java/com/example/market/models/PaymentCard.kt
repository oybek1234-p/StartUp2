package com.example.market.models
//
//class PaymentCard {
//    var id = System.currentTimeMillis().toString()
//    var userId = ""
//    var cardNumber = -1L
//    var cvv = -1
//    var expiryDate = ""
//    var cardHolderName = ""
//    var payCardName: PayCardNames = PayCardNames.Uzcard
//}
//
//fun getPayCardName(name: PayCardNames) : String {
//    return when(name) {
//        PayCardNames.Click -> "Click"
//        PayCardNames.Paynet -> "Paynet"
//        PayCardNames.Payme -> "Payme"
//        PayCardNames.Uzcard -> "Uzcard"
//    }
//}
//
//fun getPayCardPhotoUrl(name: PayCardNames): String {
//    return when(name) {
//        PayCardNames.Click -> "https://click.uz/click/images/clickog.png"
//        PayCardNames.Paynet -> "https://play-lh.googleusercontent.com/dtFWxsFLOgbLcpQCBMVuNRyfpgkAg9eUZccuKL5nZJVHoQUG9mS-V_Ui50taLAp-Kos"
//        PayCardNames.Payme -> "https://storage.kun.uz/source/4/S4953ZyJnGPaaUgLesfs_jDXvpHaTwqM.jpg"
//        PayCardNames.Uzcard -> "http://static.norma.uz/images/144352_2b4c8789226d85bced8b7a465a7e.jpg"
//    }
//}
//
//enum class PayCardNames() {
//    Uzcard,
//    Click,
//    Payme,
//    Paynet
//}