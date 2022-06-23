package com.example.market.location
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.databinding.DataBindingUtil
//import com.example.market.*
//import com.example.market.databinding.FragmentFillLocationInfoBinding
//import com.example.market.viewUtils.toast
//import com.google.android.gms.maps.model.LatLng
//
//class FillLocationInfoFragment : BaseFragment<FragmentFillLocationInfoBinding>(R.layout.fragment_fill_location_info) {
//    private var data: ShippingLocation?=null
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?,
//        binding: FragmentFillLocationInfoBinding
//    ) {
//        if (bundleData==null){
//            closeLastFragment()
//        }
//        binding.apply {
//            getBundleData()
//            backButton.setOnClickListener {
//                closeLastFragment()
//            }
//            fliContinueButtonView.setOnClickListener {
//                uploadLocation()
//            }
//            data?.apply {
//                fliLocationView.shippingAdressAdressView.text = adress
//            }
//        }
//    }
//    private fun uploadLocation(){
//
//    }
//    private fun getBundleData(){
//        bundleData?.apply {
//            val mlat = getDouble("lat")
//            val mLng = getDouble("long")
//            val mAdress = getString("address")
//            val mId = System.currentTimeMillis().toString()
//            data = ShippingLocation().apply {
//
//                latLang = com.example.market.LatLng(mlat,mLng)
//                mAdress?.let {
//                    adress = it
//                }
//            }
//        }
//    }
//}