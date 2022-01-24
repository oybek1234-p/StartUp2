package com.example.market.location

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.graphics.Color
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.market.*
import com.example.market.R
import com.example.market.binding.inflateBinding
import com.example.market.databinding.ActivityLocationBinding
import com.example.market.databinding.AlertDialogBinding
import com.example.market.databinding.LocationBottomSheetBinding
import com.example.market.databinding.ShippingOfferItemBinding
import com.example.market.recycler.databoundrecycler.DataBoundAdapter
import com.example.market.recycler.databoundrecycler.DataBoundViewHolder
import com.example.market.utils.AndroidUtilities
import com.example.market.utils.log
import com.example.market.viewUtils.*
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.util.*

class LocationActivity(private val updateLocationInfo: (() -> Unit?)? =null) : BaseFragment(), OnMapReadyCallback {

    private var mMap: GoogleMap?=null
    private var binding: ActivityLocationBinding?=null
    private var isCreated = false
    private var location:LatLng = LatLng(41.311081, 69.240562)
    private lateinit var locationRequest: LocationRequest
    private var geocoder: Geocoder?=null
    private var lastAddress: String?=null
    private var bottomSheetDialog: BottomSheetDialog?=null
    private var bottomSheetBinding:LocationBottomSheetBinding?=null
    private var offerAdapter: LocationOfferAdapter?=null
    private var mapFragment: SupportMapFragment?=null
    private var bottomSheetHeight = AndroidUtilities.dp(287f)

    override fun onDestroyView() {
        super.onDestroyView()
        mMap = null
        binding = null
        isCreated = false
        geocoder = null
        lastAddress = null
        bottomSheetDialog?.dismiss()
        bottomSheetDialog = null
        bottomSheetBinding = null
        offerAdapter = null
        mapFragment = null

    }

    private fun saveLocation() {
        if (locationLoading) {
            val alertDialogView = inflateBinding<AlertDialogBinding>(null,R.layout.alert_dialog)
            val popupDialog = PopupDialog(PopupWindowLayout(requireContext()).apply { addView(alertDialogView.root) }, DIALOG_ANIMATION_ALERT_DIALOG)
            val alert = Alert("Do you want stop loading?","We are loading your location if you want stop it press STOP button","CANCEL","STOP",
                {popupDialog.dismiss()},
                {
                    popupDialog.dismiss()
                    closeLastFragment()
                }, iconResource = R.drawable.msg_location)
            alertDialogView.data = alert
            alertDialogView.executePendingBindings()
            binding?.root?.let {
                popupDialog.show(it,Gravity.CENTER,0,0,true)
            }
        } else {
            if (lastAddress!=null) {
                val shippingLocation = ShippingLocation().apply {
                    location.let {
                        latLang = com.example.market.LatLng(it.latitude,it.longitude)
                        adress = lastAddress
                        offerAdapter?.let {
                            shippingCost = it.selectedShipping.cost.toString()
                            shippingType = it.selectedShipping.name
                        }
                    }
                }
                setShippingLocation(shippingLocation,null)

                updateLocationInfo?.let { it() }

                closeLastFragment()
                bottomSheetDialog?.dismiss()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.activity_location,container,false)

        binding?.apply {
            mapFragment = (childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment).apply {
                getMapAsync(this@LocationActivity)
            }

            backButton.setOnClickListener {
                closeLastFragment()
                bottomSheetDialog?.dismiss()
            }

            bottomSheetDialog = BottomSheetDialog(requireContext()).apply {
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.peekHeight = AndroidUtilities.dp(38f)
                (inflateBinding(null,R.layout.location_bottom_sheet) as LocationBottomSheetBinding).apply {
                    bottomSheetBinding = this

                    offerAdapter = LocationOfferAdapter.also {
                        shippingTypesRecyclerview.adapter = it
                    }

                        window?.apply {
                            setupBottomSheet()

                            setOnShowListener {
                                setupBottomSheet()
                            }
                        }

                        continueButton.setOnClickListener {
                            saveLocation()
                        }
                        //val savedLocation = currentUser?.shippingLocation
                        setContentView(root)

                }
            }
        }

        return binding!!.root
    }

    fun setupBottomSheet() {
        bottomSheetDialog?.apply {
            window?.apply {
                clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
                attributes.gravity = Gravity.BOTTOM
                setLayout(WindowManager.LayoutParams.MATCH_PARENT,bottomSheetHeight)
                findViewById<View>(R.id.design_bottom_sheet)?.setBackgroundColor(Color.TRANSPARENT)
            }
        }
    }
    private fun start() {
        try {
            geocoder = Geocoder(requireContext(), Locale.getDefault())

            setCurrentLocation(location)
            addMarkerListener()
            isCreated = true
        } catch (e: Exception) {
            log(e)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        start()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch (Dispatchers.IO){
            locationRequest = LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000)
                .setFastestInterval(1000)

            turnOnGps()
        }

    }

    private fun turnOnGps() {
        val locationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()

        val client = LocationServices.getSettingsClient(requireActivity())

        val locationSettingsResponse = client.checkLocationSettings(locationSettingsRequest)

        locationSettingsResponse.addOnSuccessListener {

        }.addOnFailureListener {
            if (it is ResolvableApiException) {
                try {
                    it.startResolutionForResult(requireActivity(),
                        0x1)
                } catch (sendEx: IntentSender.SendIntentException) {
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==0x1){
            if (resultCode== RESULT_OK){

            } else {
                closeLastFragment()
            }
        }
    }
    private fun showProgress(show: Boolean) {
        bottomSheetBinding?.apply {
            val visiblity = if (show) View.VISIBLE else View.GONE
            progressBar.visibility = visiblity

            shimmerLayout.apply {
                if (show) {
                    showShimmer(true)
                } else {
                    hideShimmer()
                }
            }
        }
    }

    private fun getAddress(latLng: LatLng) {
        locationLoading = true
        showProgress(true)
        lifecycleScope.launch (Dispatchers.IO){
            try {
                geocoder?.getFromLocation(latLng.latitude, latLng.longitude, 1)?.get(0)?.getAddressLine(0)?.let {
                    lastAddress = it
                    locationLoading = false
                    withContext(Dispatchers.Main){
                        showProgress(false)
                        setAdressText(it)
                        updateOfferCost(latLng)
                    }
                }
            }catch (e:Exception) {

            }
        }

    }

    private fun updateOfferCost(latLong: LatLng) {
        offerAdapter?.apply {
            for (i in list.indices) {
                if (i > 0) {
                    try {
                        list[i].cost = getCostForDistance(
                            latLong, if (i == 1) DetailsFragment.costPerKmFast else DetailsFragment.costPerKmUltra)

                    } catch (e: Exception) {
                        log(e.message)
                    }
                }
            }
            notifyDataSetChanged()
        }
    }

    private fun setAdressText(text:String) {
        bottomSheetBinding?.locationAdressView?.text = text
        binding?.adressView?.text = "Your adress: \n$text"
    }

    private fun setCurrentLocation(newLocation: LatLng) {
        val camera = CameraUpdateFactory.newLatLngZoom(newLocation, 11F)
        mMap?.animateCamera(camera)
        addCurrentMarker(newLocation)
    }

    private fun addCurrentMarker(latLong: LatLng){
        mMap?.apply {
            clear()
            addCircle(bigRadius.center(latLong))
            addCircle(smallRadius.center(latLong))
        }
    }

    private fun onCameraChanged(move: Boolean) {
        binding?.apply {
            backButton.animate().alpha(if (move) 0f else 1f).setDuration(300).start()
            marker.animate().translationY(if (move) -AndroidUtilities.dp(12f).toFloat() else 0f).setDuration(300).start()
            currentLocationView.animate().alpha(if (move) 0f else 1f).setDuration(300).start()
        }
        bottomSheetDialog?.apply {
            if (move) {
                hide()
            } else {
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                show()
            }

        }
    }

    private var onCameraChangedRunnable:Runnable?=null

    @SuppressLint("MissingPermission")
    private fun addMarkerListener(){
        mMap?.apply {
            isMyLocationEnabled = true
            binding?.currentLocationView?.setOnClickListener {
                try {
                    myLocation.let {
                        setCurrentLocation(LatLng(it.latitude,it.longitude))
                    }
                }catch (e: Exception) {

                }
            }

            setOnCameraMoveListener {
                if (!markerMoving) {
                    AndroidUtilities.cancelRunOnUIThread(onCameraChangedRunnable)
                    onCameraChanged(true)
                }
                markerMoving = true
            }
            setOnCameraIdleListener {
                    if (markerMoving) {
                        AndroidUtilities.cancelRunOnUIThread(onCameraChangedRunnable)
                        onCameraChangedRunnable = Runnable {
                            onCameraChanged(false)
                        }
                        AndroidUtilities.runOnUIThread(onCameraChangedRunnable,500)
                    }
                    cameraPosition.target.let {
                        location = it
                        getAddress(it)
                    }

                markerMoving = false
            }
        }
    }
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
        return true
    }
    companion object {
        private val markerColor = ContextCompat.getColor(MyApplication.appContext,R.color.marker)
        private val markerCenterColor = ContextCompat.getColor(MyApplication.appContext,R.color.markerCenter)

        val bigRadius: CircleOptions = CircleOptions()
            .fillColor(markerColor)
            .strokeWidth(1.000F)
            .strokeColor(markerCenterColor)
            .radius(100.000)

        val smallRadius: CircleOptions = CircleOptions()
            .fillColor(markerCenterColor)
            .radius(10.000)
            .strokeWidth(8F)
            .strokeColor(markerCenterColor)
    }
    private var markerMoving = false
    private var locationLoading = false

    object LocationOfferAdapter : DataBoundAdapter<ShippingOfferItemBinding, ShippingOffer>(R.layout.shipping_offer_item) {
        private const val SHIPPING_TYPE_START = "Start"
        private const val SHIPPING_TYPE_FAST = "Standart"
        private const val SHIPPING_TYPE_ULTRA = "Ultra"
        /**
         * Offers list
         */
        var list = arrayListOf(
            ShippingOffer(R.drawable.yandex_car_icon, SHIPPING_TYPE_START, 0L),
            ShippingOffer(R.drawable.yandex_car_icon, SHIPPING_TYPE_FAST, 7999L),
            ShippingOffer(R.drawable.yandex_car_icon, SHIPPING_TYPE_ULTRA, 13550L)
        )

        init {
            dataList = list
        }
        /**
         * Selected positions
         */
        private var selectedPostition = 0
        private var lastSelectedPosition = -1
        var selectedShipping = list[0]

        override fun onCreateViewHolder(
            viewHolder: DataBoundViewHolder<ShippingOfferItemBinding>?,
            viewType: Int,
        ) {
            viewHolder?.apply {
                binding?.apply {
                    root.apply {
                        setOnClickListener {

                            val vibrator = MyApplication.appContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE))
                            } else {
                                vibrator.vibrate(15)
                            }

                            lastSelectedPosition = selectedPostition
                            selectedPostition = adapterPosition
                            selectedShipping = list[selectedPostition]
                            if (lastSelectedPosition == selectedPostition) return@setOnClickListener
                            notifyItemChanged(lastSelectedPosition)
                            notifyItemChanged(selectedPostition)
                        }
                    }
                }
            }
        }

        override fun bindItem(
            holder: DataBoundViewHolder<ShippingOfferItemBinding>?,
            position: Int,
            model: ShippingOffer?,
        ) {
            holder?.binding?.apply {
                data = list[position]
                shioContainer.cardElevation = if (selectedPostition == position) AndroidUtilities.dp(8f).toFloat() else 0f
            }
        }
    }
}