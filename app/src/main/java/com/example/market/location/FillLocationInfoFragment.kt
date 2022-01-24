package com.example.market.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.market.*
import com.example.market.databinding.FragmentFillLocationInfoBinding
import com.example.market.viewUtils.toast
import com.google.android.gms.maps.model.LatLng

class FillLocationInfoFragment : BaseFragment() {
    private var binding: FragmentFillLocationInfoBinding?=null

    override fun onBeginSlide() {

    }

    override fun isSwapBackEnabled(): Boolean {
        return false
    }

    override fun onConnectionChanged(state: Boolean) {

    }

    override fun onBackPressed() {

    }

    override fun onViewFullyVisible() {

    }

    override fun onViewFullyHiden() {

    }

    override fun onViewAttachedToParent() {

    }

    override fun onViewDetachedFromParent() {

    }
    
    override fun canBeginSlide(): Boolean {
        return false
    }

    private var data: ShippingLocation?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_fill_location_info, container, false)
        if (bundleData==null){
            closeLastFragment()
        }
        binding?.apply {
            getBundleData()
            backButton.setOnClickListener {
                closeLastFragment()
            }
            fliContinueButtonView.setOnClickListener {
                uploadLocation()
            }
            data?.apply {
                fliLocationView.shippingAdressAdressView.text = adress
            }
        }
        return binding!!.root
    }
    private fun uploadLocation(){

    }

    private fun getBundleData(){
        bundleData?.apply {
            val mlat = getDouble("lat")
            val mLng = getDouble("long")
            val mAdress = getString("address")
            val mId = System.currentTimeMillis().toString()
            data = ShippingLocation().apply {

                latLang = com.example.market.LatLng(mlat,mLng)
                mAdress?.let {
                    adress = it
                }
            }
        }
    }
}