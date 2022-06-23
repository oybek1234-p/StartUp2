package com.example.market.viewUtils
//
//import android.os.Bundle
//import com.example.market.utils.log
//import com.google.android.gms.maps.SupportMapFragment
//
//class AsyncMap : SupportMapFragment() {
//    /**
//     * Create map on backgorund thread in order not block the main thread :)
//     * May throw exception, not guaranted
//     */
//    override fun onCreate(savedInstanceState: Bundle?) {
//        try {
//                Thread {
//                    super.onCreate(null)
//                }.run()
//            } catch (e:Exception) {
//                log(e.message)
//            }
//    }
//}