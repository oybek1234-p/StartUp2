package com.org.ui

import android.Manifest
import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import androidx.core.view.doOnNextLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.market.R
import com.example.market.databinding.ActivityLocationBinding
import com.example.market.databinding.ShippingOfferItemBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.org.market.*
import com.org.net.models.ShippingLocation
import com.org.net.models.ShippingOffer
import com.org.ui.actionbar.BaseFragment
import com.org.ui.actionbar.backgroundTintState
import com.org.ui.actionbar.lottieUrl
import com.org.ui.actionbar.textColorList
import com.org.ui.components.RecyclerListView
import com.org.ui.components.visibleOrGone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationFragment(val onSaved: () -> Unit) :
    BaseFragment<ActivityLocationBinding>(R.layout.activity_location) {

    private var offersAdapter: ShippingTypesRecyclerView? = null
    private var shippingLocation = ShippingLocation()
    private var isPermissionGot = false
    private var isMapGot = false
    private var googleMap: GoogleMap? = null
    private lateinit var sheetBehaviour: BottomSheetBehavior<View>
    private lateinit var mapFragment: SupportMapFragmentMy
    private lateinit var geocoder: Geocoder
    private var currentLocation = LatLng(0.0, 0.0)
        set(value) {
            field = value
            shippingLocation.latLang = value
        }

    private var shipFromLocation = LatLng(41.311081, 69.240562)
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun createActionBar(): View? {
        return null
    }

    override fun onFragmentDestroy() {
        super.onFragmentDestroy()

        requireActivity().supportFragmentManager.beginTransaction().remove(mapFragment).commit()
        offersAdapter = null
        googleMap = null
    }

    private var sheetPeekHeight = dp(64f)

    open class ShippingTypesRecyclerView(val onChecked: (holder: RecyclerListView.BaseViewHolder<ShippingOfferItemBinding>, shippingType: ShippingOffer) -> Unit) :
        RecyclerListView.Adapter<ShippingOfferItemBinding, ShippingOffer>(R.layout.shipping_offer_item,
            object :
                DiffUtil.ItemCallback<ShippingOffer>() {
                override fun areContentsTheSame(
                    oldItem: ShippingOffer,
                    newItem: ShippingOffer,
                ): Boolean {
                    val same = oldItem.cost == newItem.cost
                    return same
                }

                override fun areItemsTheSame(
                    oldItem: ShippingOffer,
                    newItem: ShippingOffer,
                ): Boolean {
                    return oldItem.id == newItem.id
                }
            }) {


        fun notifyChanged() {
            submitList(list.toMutableList())
        }

        fun setEmpty() {
            list.forEach {
                it.cost = -1
            }
            notifyChanged()
        }

        fun setCost(position: Int, cost: Long) {
            try {
                list[position].cost = cost
                val costView =
                    (mRecyclerView?.findViewHolderForLayoutPosition(position) as RecyclerListView.BaseViewHolder<ShippingOfferItemBinding>).binding.shioCost
                costView.text = formatCurrency(cost)
            } catch (e: Exception) {

            }
        }

        val list = ShippingController.list.toMutableList().apply {
            submitList(this)
        }

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            super.onAttachedToRecyclerView(recyclerView)
            recyclerView.doOnNextLayout {
                setChecked(0)
            }
        }

        private var lastChecked = -1
        var currentChecked = -1

        fun getCurrentChecked(): ShippingOffer {
            return list[currentChecked]
        }

        fun setCheckedView(binding: ShippingOfferItemBinding, checked: Boolean, animate: Boolean) {
            binding.apply {
                shioContainer.apply {
                    if (animate) {
                        var scale = if (checked) 1.2f else 1f
                        shioImageview.apply {
                            playAnimation()
                            animate().scaleX(scale).scaleY(scale)
                                .setDuration(if (checked) 300 else 150).start()
                        }
                        scale = if (checked) 1.1f else 1f
                        root.animate().scaleX(scale).scaleY(scale)
                            .setDuration(if (checked) 300 else 150).start()
                    }
                    isSelected = checked
                }
                shioCost.isSelected = checked
                shioName.isSelected = checked
                vibrate(15)
            }
        }

        override fun bind(
            holder: RecyclerListView.BaseViewHolder<ShippingOfferItemBinding>,
            position: Int,
            model: ShippingOffer,
        ) {
            holder.apply {
                val checked = layoutPosition == currentChecked
                setCheckedView(binding, checked, false)
                binding.apply {
                    shioImageview.lottieUrl(model.lottieUrl)
                    shioCost.text = if (model.cost == -1L) "----" else formatCurrency(model.cost)
                    shioName.text = model.type.name
                }
            }
        }

        fun setChecked(layoutPosition: Int) {
            lastChecked = currentChecked
            currentChecked = layoutPosition
            if (lastChecked == currentChecked) {
                return
            }
            val lastHolder =
                mRecyclerView!!.findViewHolderForLayoutPosition(lastChecked) as RecyclerListView.BaseViewHolder<ShippingOfferItemBinding>?
            val currentHolder =
                mRecyclerView!!.findViewHolderForLayoutPosition(layoutPosition) as RecyclerListView.BaseViewHolder<ShippingOfferItemBinding>?


            if (lastHolder != null) {
                setCheckedView(lastHolder.binding, false, true)
            }
            if (currentHolder != null) {
                setCheckedView(currentHolder.binding, true, true)
                onChecked(currentHolder, currentList[layoutPosition])
            }
        }

        override fun onViewHolderCreated(
            holder: RecyclerListView.BaseViewHolder<ShippingOfferItemBinding>,
            type: Int,
        ) {
            holder.apply {
                binding.apply {
                    root.backgroundTintState(R.attr.colorBackground,
                        R.attr.colorSurface,
                        android.R.attr.state_selected)
                    root.setOnClickListener {
                        setChecked(layoutPosition)
                    }
                    shioName.textColorList(R.attr.colorOnSurfaceMedium,
                        R.attr.colorOnSurfaceLow,
                        android.R.attr.state_selected)
                    shioCost.textColorList(R.attr.colorOnSurfaceHigh,
                        R.attr.colorOnSurfaceLow,
                        android.R.attr.state_selected)
                }
            }
        }
    }

    class SupportMapFragmentMy : SupportMapFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            try {
                Thread {
                    super.onCreate(savedInstanceState)
                }.start()
            } catch (e: Exception) {

            }
            try {
                super.onCreate(savedInstanceState)
            } catch (e: Exception) {

            }
        }
    }

    override fun onCreateView(binding: ActivityLocationBinding) {

        isMapGot = false
        isPermissionGot = false

        binding.apply {
            backButton.setOnClickListener {
                closeLastFragment()
            }
            currentLocationView.setOnClickListener {
                moveToCurrentLocation(true)
            }
            mapFragment =
                (requireActivity().supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragmentMy)

            bottomSheet.apply {
                sheetBehaviour = BottomSheetBehavior.from(root).apply {
                    this.peekHeight = sheetPeekHeight
                    addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                        override fun onStateChanged(bottomSheet: View, newState: Int) {
                            if (newState == BottomSheetBehavior.STATE_COLLAPSED || newState == BottomSheetBehavior.STATE_EXPANDED) {
                                if (cameraMoving || lastSheetState == newState) return
                                lastSheetState = newState

                                val offset =
                                    -(displaySize.y - bottomSheet.top - sheetPeekHeight).toFloat() / 2
                                val expanded = newState == BottomSheetBehavior.STATE_EXPANDED

                                marker.animate().translationY(if (expanded) offset else 0f)
                                    .setDuration(if (expanded) 300 else 150).setInterpolator(
                                        decelerateInterpolator).start()

                                mapFragment.requireView().animate()
                                    .translationY(if (expanded) offset else 0f)
                                    .setDuration(if (expanded) 300 else 150).setInterpolator(
                                        decelerateInterpolator).start()
                            }
                        }

                        override fun onSlide(bottomSheet: View, slideOffset: Float) {

                        }
                    })
                }
                shippingTypesRecyclerview.apply {
                    continueButton.apply {
                        backgroundTintState(R.attr.colorSecondary,
                            R.attr.colorBackground,
                            android.R.attr.state_enabled)
                        textColorList(R.attr.colorOnSecondaryHigh,
                            R.attr.colorOnSurfaceLow,
                            android.R.attr.state_enabled)

                        isEnabled = false

                        setOnClickListener {
                            if (isEnabled) {
                                saveLocation()
                            } else {
                                when {
                                    cameraMoving -> {
                                        toast("Please choose location")
                                    }
                                    addressLoading -> {
                                        toast("Loading address please wait")
                                    }
                                    else -> {
                                        toast("Make sure gps enables")
                                    }
                                }
                            }
                        }
                    }

                    offersAdapter = ShippingTypesRecyclerView { h, t ->

                    }.also {
                        adapter = it
                    }
                    layoutManager =
                        LinearLayoutManager(context(), LinearLayoutManager.HORIZONTAL, false)
                }

                geocoder = Geocoder(context())
                fusedLocationProviderClient =
                    LocationServices.getFusedLocationProviderClient(context())

                mapFragment.getMapAsync {
                    isMapGot = true
                    googleMap = it
                    onMapInit()
                }
            }
        }
    }

    private var lastSheetState = BottomSheetBehavior.STATE_COLLAPSED

    private fun saveLocation() {
        shippingLocation.userId = currentUserId()
        val offer = offersAdapter!!.getCurrentChecked()
        shippingLocation.cost = offer.cost
        shippingLocation.type = offer.type
        DataController.saveUserLocation(shippingLocation)
        ShippingController.saveShipping(shippingLocation)
        onSaved()
        closeLastFragment()
    }

    fun isGpsOn() = checkGpsOn(requireActivity())

    fun turnOnGps() {
        val locationSettingRequest =
            LocationSettingsRequest.Builder().setAlwaysShow(true)
                .addLocationRequest(createGpsRequest()).build()

        SettingsClient(context())
            .checkLocationSettings(locationSettingRequest)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    moveToCurrentLocation(true)
                } else {
                    moveCamera(shipFromLocation, true)
                }
            }
    }

    fun createGpsRequest(): com.google.android.gms.location.LocationRequest {
        return com.google.android.gms.location.LocationRequest.create().apply {
            interval = 1000
            fastestInterval = 500
            priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private var cameraMoving = false
        set(value) {
            if (value != field) {
                field = value
                requireBinding().apply {
                    backButton.visibleOrGone(!value, 0)
                    currentLocationView.visibleOrGone(!value, 0)

                    vibrate(10)

                    val markerOffset = if (value) -dp(24f).toFloat() else 0f

                    markerIcon.animate().translationY(markerOffset)
                        .setDuration(if (value) 200 else 500)
                        .setInterpolator(
                            decelerateInterpolator).start()

                    timeShimmerLayout.animate().translationY(markerOffset)
                        .setDuration(if (value) 200 else 500)
                        .setInterpolator(
                            decelerateInterpolator).start()

                    val scale = if (value) 1.4f else 1f
                    markerDot.animate().scaleY(scale).scaleX(scale)
                        .setDuration(if (value) 200 else 500).start()
                    timeTextView.visibleOrGone(!value, 0)
                    bottomSheet.locationAdressView.visibleOrGone(!value, 0)
                    bottomSheet.view13.visibleOrGone(!value, 0)

                    sheetBehaviour.state =
                        if (value) BottomSheetBehavior.STATE_COLLAPSED
                        else BottomSheetBehavior.STATE_EXPANDED

                    sheetBehaviour.addBottomSheetCallback(object :
                        BottomSheetBehavior.BottomSheetCallback() {
                        override fun onStateChanged(bottomSheet: View, newState: Int) {
                            sheetBehaviour.removeBottomSheetCallback(this)
                            if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                                offersAdapter?.setEmpty()
                            }
                        }

                        override fun onSlide(bottomSheet: View, slideOffset: Float) {

                        }
                    })
                }
            }
        }

    private var addressLoading = false
        set(value) {
            if (value != field) {
                field = value
                requireBinding().apply {
                    bottomSheet.apply {
                        if (value) {
                            locationAdressView.text = "Loading..."
                            timeTextView.text = "Detailing"
                            offersAdapter?.setEmpty()
                        }
                        timeShimmerLayout.apply {
                            if (value) {
                                showShimmer(true)
                            } else {
                                runOnUiThread({
                                    hideShimmer()
                                }, (100..300).random().toLong())
                            }
                        }
                        progressBar.visibleOrGone(value, 0)
                        shimmerLayout.apply {
                            if (value) {
                                showShimmer(true)
                            } else {
                                runOnUiThread({
                                    hideShimmer()
                                }, (200..400).random().toLong())
                            }
                        }
                        continueButton.isEnabled = !value
                    }
                }
            }
        }

    fun loadAddress() {
        addressLoading = true
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val address = geocoder.getFromLocation(currentLocation.latitude,
                    currentLocation.longitude,
                    1)[0]?.getAddressLine(0)

                setAddress(address)
            } catch (e: java.lang.Exception) {
                setAddress(null)
            }
        }
    }

    fun setAddress(address: String?) {
        lifecycleScope.launch(Dispatchers.Main) {
            addressLoading = false
            val newAddress = if (address.isNullOrEmpty()) "Unnamed street" else address.also {
                shippingLocation.address = it
            }
            shippingLocation.address = newAddress
            requireBinding().bottomSheet.locationAdressView.text = newAddress
            updateTime()
            updateTypeCost()
        }
    }

    fun updateTypeCost() {
        offersAdapter?.apply {
            list.forEachIndexed { index, shippingOffer ->
                val newCost = ShippingController.getCostForDistance(currentLocation,
                    shipFromLocation,
                    shippingOffer.type)

                setCost(index, newCost)
            }
        }
    }

    fun updateTime() {
        val distance = ShippingController.getDistance(currentLocation, shipFromLocation)
        val time = ShippingController.getDeliveryTime(distance)
        shippingLocation.timeSpendMinute = time
        requireBinding().timeTextView.text = "$time min"
    }

    @SuppressLint("MissingPermission")
    fun onMapInit() {
        googleMap?.apply {
            uiSettings.apply {
                isMyLocationButtonEnabled = false
            }
            setOnCameraMoveListener {
                cameraMoving = true
            }
            setOnCameraIdleListener {
                runOnUiThread({
                    cameraMoving = false
                    currentLocation = cameraPosition.target
                    loadAddress()
                }, 300)
            }

            ShippingController.loadShipping()
            shippingLocation = ShippingController.shippingLocation.also {
                if (it.id != 0L) {
                    moveCamera(it.latLang, false)
                }
            }
            turnOnGps()
            requestPermission {
                isMyLocationEnabled = true
                moveToCurrentLocation(false)
            }
        }
    }

    private fun requestPermission(block: () -> Unit) {
        if (isPermissionGot) {
            block()
            return
        }
        PermissionController.getInstance().requestPermissions(context(),
            0,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION),
            object : PermissionController.PermissionResult {
                override fun onGranted() {
                    isPermissionGot = true
                    block()
                }

                override fun onDenied() {
                    isPermissionGot = false
                }
            })
    }

    @SuppressLint("MissingPermission")
    private fun moveToCurrentLocation(animate: Boolean) {
        requestPermission {
            if (!isGpsOn()) {
                turnOnGps()
            } else {
                fusedLocationProviderClient.lastLocation.addOnCompleteListener {
                    if (it.isSuccessful && it.result != null) {
                        val location = LatLng(it.result.latitude, it.result.longitude)
                        if (!animate) {
                            moveCamera(location, false)
                        } else {
                            val cameraUpdateFactory = CameraUpdateFactory.scrollBy(0f, 1f)
                            googleMap?.moveCamera(cameraUpdateFactory)
                            moveCamera(location, true)
                        }
                    }
                }
            }
        }
    }

    fun moveCamera(location: LatLng, animate: Boolean = true) {
        val update = CameraUpdateFactory.newLatLngZoom(location, 16f)
        if (animate) {
            googleMap?.animateCamera(update)
        } else {
            googleMap?.moveCamera(update)
            cameraMoving = true
            cameraMoving = false
        }
    }
}