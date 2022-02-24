package com.example.market


import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable

import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.market.auth.LoginFragment
import com.example.market.binding.inflateBinding
import com.example.market.camera.DispatchQueue
import com.example.market.cart.KorzinaFragment
import com.example.market.comment.CommentsFragment
import com.example.market.databinding.*
import com.example.market.home.HomeListAdapter
import com.example.market.home.HomeListDecoration
import com.example.market.home.VIEW_TYPE_SIMPLE_TEXT
import com.example.market.location.LocationActivity
import com.example.market.location.LocationController
import com.example.market.location.LocationProvider
import com.example.market.models.Product
import com.example.market.navigation.FragmentController
import com.example.market.navigation.bottomNavVisiblity
import com.example.market.profile.ProfileFragmentSeller
import com.example.market.recycler.RecyclerItemClickListener
import com.example.market.recycler.databoundrecycler.DataBoundAdapter
import com.example.market.recycler.databoundrecycler.DataBoundViewHolder
import com.example.market.utils.*
import com.example.market.viewUtils.*
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList

class DetailsFragment(var product: Product) : BaseFragment<FragmentDetailsBinding>(R.layout.fragment_details) {

    private var photosAdapter: DetailsPagerAdapter? = null
    private var photosList: ArrayList<String>? = ArrayList()
    private var nestedBinding: DetailsNastedLayoutBinding? = null
    private var bottomLayoutBinding: DetailsBottomLayoutBinding? = null

    private var mMap: GoogleMap? = null
    private var mapFragment: SupportMapFragment? = null
    private var isCreated = false
    private var lastLocation: Location? = null
    private var locationRequest: LocationRequest? = null
    private var geocoder: Geocoder? = null
    private var lastAddress: String? = null
    private var locationController: LocationController? = null
    private var bottomSheetLocationBinding: LocationActivityModernBinding? = null
    private var shippingOfferAdapter: LocationOffer? = null
    private var mLike: Like?=null

    private var specificationNameColor: Int = 0
    private var specificationValueColor: Int = 0
    private var specificationSheet: BottomSheetDialog?=null
    private var bottomShLocation: BottomSheetDialog? = null

    private var sellerInfoDialog: BottomSheetDialog? = null
    private var sheetBinding: DetailsSellerInfoBottomSheetViewBinding? = null
    private var sellerInfo: User? = null
    private var subscribedToThisSeller = false

    private var subscribeDrawale: Drawable?=null

    var like = false
    var hasPendingLikes = false

    private var commentsDialog: CommentsFragment?=null
    var openAndSelectCommentId: String?=null

    private var details: Details?=null

    private var moreProductsAdapter: HomeListAdapter?=null

    private var moreProductsList: ArrayList<Product>? = ArrayList()

    override fun onDestroyView() {
        super.onDestroyView()
        like = false
        hasPendingLikes = false
        commentsDialog = null
        openAndSelectCommentId = null
        details = null
        moreProductsAdapter = null
        moreProductsList = null
        subscribedToThisSeller = false
        sellerInfo = null
        specificationSheet = null
        bottomShLocation = null
        sellerInfoDialog = null
        sheetBinding = null
        photosList = null
        photosAdapter = null
        nestedBinding = null
        bottomLayoutBinding = null
        mapFragment = null
        mMap =  null
        isCreated = false
        lastLocation = null
        locationRequest = null
        geocoder = null
        lastAddress = null
        locationController = null
        bottomSheetLocationBinding = null
        //shippingOfferAdapter = null
        mLike = null
    }

    companion object {
        var costPerKmFast = 1800
        var costPerKmUltra = 2950
        val latLngTashkent = LatLng(41.311081, 69.240562)

        val threadQueue = DispatchQueue("Details")
    }

    override fun onBeginSlide() {
    }

    override fun isSwapBackEnabled(): Boolean {
        return true
    }

    override fun onConnectionChanged(state: Boolean) {

    }

    override fun onBackPressed() {

    }

    override fun onViewFullyVisible() {
        addToCartFragment?.dialog?.window?.decorView?.visibility = View.VISIBLE
    }

    override fun onViewFullyHiden() {

    }
    override fun onViewAttachedToParent() {
        subscribedToThisSeller = checkSubscribed(product.sellerId)

        sellerInfo?.let {
            applySellerInfo(it)
        }

    }
    private var addToCartFragment: AddToCartFragment ?= null
    override fun onViewDetachedFromParent() {

    }
    private var isLocationAsked = false

    fun checkLocation() = isLocationAsked && currentUser?.shippingLocation != null

    override fun canBeginSlide(): Boolean {
        return true
    }

    //Safe
    private fun start() {

        try {
            lifecycleScope.launch(Dispatchers.IO) {
                locationRequest?.let {
                    locationController = LocationController(requireContext(),
                        object : LocationProvider {
                            override fun onLocationChanged(location: Location) {
                                lastLocation = location }
                                                  }, it).apply { start() }
                }
                geocoder = Geocoder(requireContext(), Locale.getDefault())
                addMarkerListener()
                setCurrentLocation(latLngTashkent)
                isCreated = true
            }
        } catch (e: Exception) {
            log(e.message)
        }
    }
    //Safe
    private fun addCurrentMarker(latLong: LatLng) {
        try {
            mMap?.apply {
                clear()
                addCircle(LocationActivity.bigRadius.center(latLong))
                addCircle(LocationActivity.smallRadius.center(latLong))
            }
        }catch (e: Exception) {

        }
    }

    //Safe
    private fun getAddress(latLng: LatLng) {
        locationLoading = true
        locationProgressBarShow(true)
        try {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val stringBuilder = StringBuilder()
                    stringBuilder.append("")

                    geocoder?.getFromLocation(latLng.latitude, latLng.longitude, 1)?.get(0)?.apply {
                            getAddressLine(0)?.let {
                                    lastAddress = it
                                    stringBuilder.append(it)
                                }
                            }
                            lastAddress = stringBuilder.toString()

                            withContext(Dispatchers.Main) {
                                locationLoading = false
                                locationProgressBarShow(false)
                                setAdressText(stringBuilder.toString())
                                updateOfferCost(latLng)
                            }

                } catch (e: Exception) {
                    log(e.message)
                }
            }
        } catch (e: Exception) {

        }
    }

    //Safe
    private fun setAdressText(text: String) {
        bottomSheetLocationBinding?.apply {
            locationAdressView.text = text
        }
    }

    //Safe
    private fun addMarkerListener() {
        AndroidUtilities.runOnUIThread {
            mMap?.apply {
               setOnCameraIdleListener {
                    try {
                        if (markerMoving) {
                            startLocationIconAnimation(false)
                            startContinueButtonAnimation(true)
                        }
                        markerMoving = false
                        bottomSheetLocationBinding?.root?.parent?.requestDisallowInterceptTouchEvent(false)

                        val currentCameraPosition = cameraPosition.target
                        lastLocation?.apply {
                            latitude = currentCameraPosition.latitude
                            longitude = currentCameraPosition.longitude
                        }
                        getAddress(currentCameraPosition)
                    }catch (e: Exception) {

                    }
                }
            }
        }
    }

    //Safe
    private fun setCurrentLocation(l: LatLng? = null) {
        AndroidUtilities.runOnUIThread {
            var latLang: LatLng?=null
            if (l != null) {
                latLang = LatLng(l.latitude,l.longitude)
            } else {
                lastLocation?.apply {
                    latLang = LatLng(latitude,longitude)
                }
            }
            latLang?.let {
                val camera = CameraUpdateFactory.newLatLngZoom(it, 15F)
                mMap?.animateCamera(camera)
                addCurrentMarker(it)
            }
        }
    }
    //Safe
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x1) {
            if (resultCode == Activity.RESULT_OK) {
                start()
            } else {
                toast(requireContext(), "Result not ok")
            }
        }
    }
    //Safe
    private fun turnOnGps() {
        try {
            if (locationRequest == null) {
                locationRequest = createLocationRequest()
            }
        locationRequest?.let { it ->
            val locationSettingsRequest = LocationSettingsRequest.Builder()
                .addLocationRequest(it)
                .build()

            val client = LocationServices.getSettingsClient(requireActivity())

            val locationSettingsResponse = client.checkLocationSettings(locationSettingsRequest)

            locationSettingsResponse.addOnSuccessListener {
                start()
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
        }catch (e: Exception) {

        }
    }

    //Safe
    private fun updateOfferCost(latLong: LatLng) {
        shippingOfferAdapter?.apply {
            for (i in list.indices) {
                if (i > 0) {
                    try {
                        list[i].cost = getCostForDistance(
                            latLong, if (i == 1) costPerKmFast else costPerKmUltra)

                    } catch (e: Exception) {
                        log(e.message)
                    }
                }
            }
            notifyDataSetChanged()
        }
    }

    //Safe
    private fun onDestroyMapFragment() {
        mMap = null
        mapFragment?.let {
            requireFragmentManager().beginTransaction().remove(it).commit()
        }
        mapFragment = null
        lastLocation = null
        locationRequest = null
        geocoder = null
        lastAddress = null
        locationController = null
        bottomSheetLocationBinding = null
    }

    //Safe
    private fun subscribeToSeler() {
        sellerInfo?.let {
            subscribedToThisSeller = !subscribedToThisSeller
            notifySubscribed()

                subscibeToStore(
                    it.id,
                    subscribedToThisSeller,
                    object : Result {
                    override fun onSuccess(any: Any?) {
                    }
                })

        }
    }

    //Safe
    private fun notifySubscribed() {
        sheetBinding?.apply {
            detailsSellerInfoSubscribeView.apply {
                text = if (subscribedToThisSeller) "Subscribed" else "Subscribe"
                background?.apply {
                    setTint(if (subscribedToThisSeller) resources.getColor(R.color.gray_999999) else resources.getColor(R.color.A_orange))
                }
            }
        }
    }

    //Safe
    private fun openSellerInfo() {
        sellerInfoDialog = BottomSheetDialog(requireContext()).apply {
            if (window==null) return

            sheetBinding = inflateBinding<DetailsSellerInfoBottomSheetViewBinding>(
                window!!.decorView as ViewGroup,
                R.layout.details_seller_info_bottom_sheet_view
            ).apply {

                setContentView(root)

                sellerInfo?.let {s->
                    data = s
                    detailsSellerInfoCallView.setOnClickListener {
                        openCallView(s.phone,requireContext())
                    }
                    detailsSellerInfoSubscribeView.setOnClickListener {
                        subscribeToSeler()
                    }
                }
                detailsSellerInfoViewView.setOnClickListener {
                    dismiss()
                    openSellerShop()
                }
                detailsSellerInfoBackView.setOnClickListener {
                    dismiss()
                }
            }
            setOnDismissListener {
                sellerInfoDialog = null
                sheetBinding = null
                subscribeDrawale = null
            }
            dismissVisibleDialog()
            visibleDialog = this
            show()
            notifySubscribed()
        }
    }
    private var moreListHasDelayedNotify = false

    private var moreProductsScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                if (moreListHasDelayedNotify) {
                    notifyMoreProductsAdapter()
                }
            }
        }
    }

    private fun notifyMoreProductsAdapter() {
        if (nestedBinding?.moreProductsListView?.scrollState != RecyclerView.SCROLL_STATE_IDLE) {
            moreListHasDelayedNotify = true
            log("Details has delayed notify")
            return
        }
        moreProductsAdapter?.submitList(null)
        moreProductsAdapter?.submitList(moreProductsList)
        nestedBinding?.moreProductsListView?.requestLayout()
        moreListHasDelayedNotify = false
    }

    var viewCountModel = Product().apply {
        id = System.currentTimeMillis().toString()
        type = VIEW_TYPE_SIMPLE_TEXT
    }

    private var triedToGetWithoutFilter = false
    //Safe
    private fun getMoreProducts(filterByCategory: Boolean=true){
        try {
            var query = FirebaseFirestore
                .getInstance()
                .collection(PRODUCTS)
                .limit(6)

            query = query.whereEqualTo("kategoriya",if (filterByCategory) product.kategoriya else "popular")

            moreProductsList?.add(viewCountModel)
            notifyMoreProductsAdapter()

            query.whereNotEqualTo("id",product.id)
                    .get()
                    .addOnCompleteListener { it ->
                        if (it.result!=null&&it.result!!.documents.isNotEmpty()) {
                          it.result.documents.let {
                            try {
                                moreProductsList = parseDocumentSnapshot(it,Product::class.java)
                                moreProductsList?.add(viewCountModel)
                                notifyMoreProductsAdapter()
                            }catch (e: Exception){
                                throw e
                            }
                        }
                        }
                }
        }catch (e: Exception){
            throw e
        }
    }

    private fun onMoreProductsEmpty() {
        toast("OnMoreProducts")
        if (triedToGetWithoutFilter) {
            toast("Already Tried to get else")
            moreProductsList = ArrayList()
            moreProductsList?.add(
                Product().apply {
                    id = System.currentTimeMillis().toString()
                    type = VIEW_TYPE_EMPTY_LIST
                }
            )
            nestedBinding?.moreProductsListView?.apply {
                try {
                    removeItemDecorationAt(0)
                    layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.VERTICAL }
                }catch (e: Exception){

                }
            }
            moreProductsAdapter?.submitList(moreProductsList!!.toMutableList())
        } else {
            toast("Tried to get")
            triedToGetWithoutFilter = true
            getMoreProducts(false)
        }
    }

    //Safe
    private fun createMoreProductsList() {
        nestedBinding?.apply {
            moreProductsListView.apply {
                adapter = HomeListAdapter().apply {
                    moreProductsAdapter = this
                    isNestedScrollingEnabled = false
                    layoutManager?.isAutoMeasureEnabled = true
                    addItemDecoration(HomeListDecoration)
                    addOnScrollListener(moreProductsScrollListener)
                    setOnItemClickListener(object : RecyclerItemClickListener{
                        override fun onClick(position: Int, type: Int) {
                            when(type) {
                                R.layout.home_product_item -> {
                                        currentList[position]?.let {
                                            presentFragmentRemoveLast(DetailsFragment(it), removeLast = false)
                                        }
                                }
                                R.layout.home_main_category_item -> {

                                }
                            }
                        }
                    })
                    viewsCount = this@DetailsFragment.product.viewsCount
                    notifyMoreProductsAdapter()
                }
            }
        }
    }
    //Safe
    private fun openSellerShop() {
        presentFragmentRemoveLast(
            SellerShopFragment().apply { bundleAny = sellerInfo},
            removeLast = false
        )
    }

    //Safe
    private fun applySellerInfo(sellerInfo: User) {
        nestedBinding?.detailsInfoSellerInfoLayout?.apply {
            data = sellerInfo.apply {
                phone = "${likes} likes | ${gifts} gifts | ${subscribers} subscribers"

                if (name.isEmpty()) {
                    name = "No name"
                }
                if (photo.isEmpty()) {
                    photo = "https://image.shutterstock.com/image-vector/default-avatar-profile-icon-vector-260nw-1745180411.jpg"
                }
            }
            subscribeButton.apply {
                if (subscribedToThisSeller) {
                    visibility = View.GONE
                } else {
                    visibility = View.VISIBLE
                    scaleX = 1f
                    scaleY = 1f
                    alpha = 1f
                }
            }
        }
    }

    //Safe
    private fun showSpecifications(specifications: ArrayList<Specification>) {
        specificationSheet = BottomSheetDialog(requireContext())
            .apply {
            setContentView(createSpecificationLayout(
                specifications
            ))
                dismissVisibleDialog(this)
            show()
        }
    }
    //Safe
    private fun createSpecificationLayout(specifications: ArrayList<Specification>): LinearLayout {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL

            val marginHorizontal = AndroidUtilities.dp(18f)
            val marginVertical = AndroidUtilities.dp(4f)
            setPadding(0,AndroidUtilities.dp(8f),0,AndroidUtilities.dp(80f))

            val actionBar = FrameLayout(context).apply {
                setPadding(0,AndroidUtilities.dp(8f),0,AndroidUtilities.dp(8f))
                val title = TextView(context).apply {
                    setTextSize(TypedValue.COMPLEX_UNIT_DIP,18f)
                    typeface = MyApplication.robotoMedium
                    setTextColor(Color.BLACK)
                    textAlignment = TextView.TEXT_ALIGNMENT_VIEW_START
                    text = "Specification"
                }
                addView(title,FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT).apply {
                    gravity = Gravity.CENTER
                })
                val closeButton = ImageView(context).apply {
                    setImageResource(R.drawable.pip_close)
                    imageTintList = ColorStateList.valueOf(Color.BLACK)
                    setOnClickListener {
                        specificationSheet?.dismiss()
                    }
                }
                addView(closeButton,FrameLayout.LayoutParams(AndroidUtilities.dp(24f),AndroidUtilities.dp(24f)).apply {
                    gravity = Gravity.END
                    marginEnd = marginHorizontal
                })
            }
            addView(actionBar,LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT))

            for (i in specifications) {
                val text = getSpecificationSpan(i.name,i.value)

                val textView = TextView(requireContext()).apply {
                    setTextSize(TypedValue.COMPLEX_UNIT_DIP,16f)
                    typeface = MyApplication.robotoRegular
                    textAlignment = TextView.TEXT_ALIGNMENT_VIEW_START
                    setText(text)
                }

                addView(textView,
                    LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(marginHorizontal,marginVertical,marginHorizontal,marginVertical)
                    })
            }
        }
    }
    //Safe
    private fun getSpecificationSpan(name: String,value: String): SpannableString {
        val text = StringBuilder().apply {
            append(name)
            append(": ")
            append(value)
        }.toString()
        val valueStart = name.length + 2
        return SpannableString(text).apply {
            setSpan(
                ForegroundColorSpan(specificationNameColor),
                0,
                valueStart,
                SpannableString.SPAN_EXCLUSIVE_INCLUSIVE
            )
            setSpan(
                ForegroundColorSpan(specificationValueColor),
                valueStart,
                text.length,
                SpannableString.SPAN_EXCLUSIVE_INCLUSIVE
            )
        }
    }

    //Safe
    private fun likeProduct() {
        if (!hasPendingLikes&&checkCurrentUser()) {
            toast(hasPendingLikes.toString())

           product.apply {
            like = !isProductLiked(this)

            if (like) {

                likesCount += 1

                mLike = Like().apply {
                    userId = currentUser!!.id
                    this@DetailsFragment.product.let {
                        product = it
                        productId = it.id
                    }
                    id = currentUser!!.id + this@DetailsFragment.product.id
                    addLikedProductToCache(this)
                }

            } else {
                likesCount -= 1
                mLike = getLikeForProduct(this)
                mLike?.let {
                    removeFromUserLikesCache(it)
                }
            }
            setLikesCount()

            hasPendingLikes = true

            mLike?.let {
                likeProduct(product,like,it,object : Result {
                    override fun onSuccess(any: Any?) {
                        snackBar(view,if (like)"Succesfully liked" else "Successfully disliked")
                        hasPendingLikes = false
                    }

                    override fun onFailed() {
                        super.onFailed()
                        toast("onFailed")
                        hasPendingLikes = false
                    }

                    override fun onFailed(message: String?) {
                        super.onFailed(message)
                        toast("onFailed")
                        hasPendingLikes = false
                    }
                })
            }
        }
        }
    }

    //Safe
    private fun setLikesCount(){
        nestedBinding?.apply {
            detailsLikeView.drawable?.apply {
                setTint(if (like) Color.RED else Color.LTGRAY)
            }
            detailsLikeCountView.text = this@DetailsFragment.product.likesCount.toString()
        }
    }
    //Safe
    fun createDetails() {
        binding?.detailsCoordinator?.apply {
            nestedBinding = inflateBinding(this, R.layout.details_nasted_layout)

            nestedBinding?.apply {
                product = this@DetailsFragment.product

                detailsLikeView.setOnClickListener {
                    if (currentUser!=null) {
                        likeProduct()
                    }else{
                        presentFragmentRemoveLast(LoginFragment(),false)
                    }
                }
                setLikesCount()

                detailsCommentIconView.setOnClickListener {
                    for (i in 0..10){
                        openComments()
                    }
                }

                details?.apply {
                    fullInfo = description
                    if (specification!=null){
                        detailsSpecification.setOnClickListener {
                            showSpecifications(specification!!)
                        }
                    } else {
                        detailsSpecification.visibility = View.GONE
                    }
                }

                detailsInfoSellerInfoLayout.apply {

                    root.background = null
                    sellerInfoOnClick = View.OnClickListener {
                        sellerInfo?.id?.let {
                            presentFragmentRemoveLast(ProfileFragmentSeller(it),false)
                        }
                    }
                    subscribeButton.apply {
                        visibility = if (subscribedToThisSeller) View.GONE else View.VISIBLE
                        setOnClickListener {
                            if (currentUser==null) {
                                presentFragmentRemoveLast(LoginFragment(),false)
                                return@setOnClickListener
                            }
                            animate().alpha(0f).scaleX(0f).scaleY(0f).setDuration(500).setUpdateListener {
                                if (!it.isRunning){
                                    visibility = View.GONE
                                }
                            }.start()

                            subscribeToSeler()
                        }
                    }
                }
                sellerInfo?.let {
                    applySellerInfo(it)
                }

                detailsInfoSellerInfoShippingView.root.setOnClickListener {
                    presentFragmentRemoveLast(LocationActivity {
                        toast("Get location info")
                        detailsInfoSellerInfoShippingView.apply {
                            this.invalidateAll()
                        }
                        isLocationAsked = true
                    },false)
                }

                val layoutParams =
                    CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.MATCH_PARENT,
                        CoordinatorLayout.LayoutParams.MATCH_PARENT)

                layoutParams.behavior = AppBarLayout.ScrollingViewBehavior()
                layoutParams.bottomMargin = AndroidUtilities.dp(56f)
                addView(root, layoutParams)

                bottomLayoutBinding = inflateBinding(this.root as ViewGroup, R.layout.details_bottom_layout)

                bottomLayoutBinding?.apply {
                    openStoreButton.setOnClickListener { openSellerInfo() }
                    addToCartButton.setOnClickListener {openAddToCartFragment(true)}
                    buyNowButton.setOnClickListener {openAddToCartFragment(false)}
                    //
                    addView(root,CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.MATCH_PARENT,AndroidUtilities.dp(56f)).apply {
                        gravity = Gravity.BOTTOM
                    })
                }
                createMoreProductsList()

            }
        }
    }

    fun openAddToCartFragment(cart: Boolean) {
        fragmentController?.fragmentManager?.let {
            addToCartFragment = AddToCartFragment(this@DetailsFragment.product,checkLocation(),cart).apply {
                dismissVisibleDialog()
                show(it,null)

                dialog?.setOnDismissListener {
                    addToCartFragment = null
                }
            }
        }
    }
    //Safe
    private fun createLocationRequest(): LocationRequest {
        return LocationRequest()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(1000)
            .setFastestInterval(1000)
    }
    private var mapFragmentOpened = false

    private var locationLoading = false

    private fun startContinueButtonAnimation(show: Boolean) {
        bottomSheetLocationBinding?.continueButton?.animate()?.alpha(if (show) 1f else 0.5f)?.setDuration(300L)?.start()
    }

    private fun saveLocation() {
        if (locationLoading) {
            val alertDialogView = inflateBinding<AlertDialogBinding>(null,R.layout.alert_dialog)
            val popupDialog = PopupDialog(PopupWindowLayout(requireContext()).apply { addView(alertDialogView.root) }, DIALOG_ANIMATION_ALERT_DIALOG)
            val alert = Alert("Do you want stop loading?","We are loading your location if you want stop it press STOP button","CANCEL","STOP",
                {popupDialog.dismiss()},
                {
                    popupDialog.dismiss()
                    bottomShLocation?.dismiss()
                }, iconResource = R.drawable.msg_location)
            alertDialogView.data = alert
            alertDialogView.executePendingBindings()
            bottomSheetLocationBinding?.root?.let {
                popupDialog.show(it,Gravity.CENTER,0,0,true)
            }
        } else {
            if (lastAddress!=null&&lastLocation!=null) {
                val shippingLocation = ShippingLocation().apply {
                    lastLocation?.let {
                        latLang = com.example.market.LatLng(it.latitude,it.longitude)
                        adress = lastAddress
                        shippingOfferAdapter?.let {
                            cost = it.selectedShipping.cost.toString()
                            type = it.selectedShipping.name
                        }
                    }
                }
                setShippingLocation(shippingLocation,null)
                bottomShLocation?.dismiss()
            }
        }
    }

    private var markerMoving = false

    fun locationProgressBarShow(show: Boolean) {
        bottomSheetLocationBinding?.progressBar?.visibility = if (show) View.VISIBLE else View.GONE
    }
    fun startLocationIconAnimation(move: Boolean) {
        bottomSheetLocationBinding?.lamMarker?.animate()?.translationY((if (move) AndroidUtilities.dp(-8f) else 0).toFloat())?.setDuration(300L)?.start()
    }
    //Safe
    private fun onCreateMapFragment() {
        bottomShLocation = BottomSheetDialog(requireContext())
        bottomShLocation?.apply {

            window?.decorView?.let {
                bottomSheetLocationBinding = DataBindingUtil.inflate(layoutInflater,
                    R.layout.location_activity_modern,
                    it as ViewGroup,
                    false)
            }

            bottomSheetLocationBinding?.apply {
                setContentView(root)

                chooseShippingAdressExit.setOnClickListener {
                    dismiss()
                }

                setOnDismissListener {
                    mapFragmentOpened = false
                    onDestroyMapFragment()
                }
                val savedLocation = currentUser?.shippingLocation
                val savedLocationAdress = savedLocation?.adress

                val lastLocationContainer = lastSavedLocation.root
                lastLocationContainer.visibility =
                    if (savedLocation != null) View.VISIBLE else View.GONE
                lastSavedLocation.text.text = savedLocationAdress

                if (savedLocation!=null) {
                  lastLocationContainer.setOnClickListener {
                      locationAdressView.text = savedLocationAdress

                      val startRect = Rect()
                      val endRect = Rect()

                      lastLocationContainer.getGlobalVisibleRect(startRect)
                      locationAdressView.getGlobalVisibleRect(endRect)

                      val endX = endRect.left - startRect.left
                      val endY = endRect.top - startRect.top

                      val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
                      valueAnimator.interpolator = FragmentController.decelerateInterpolator
                      valueAnimator.duration = 300
                      valueAnimator.addUpdateListener {
                          val value = it.animatedValue as Float
                          lastLocationContainer.translationX = endX * value
                          lastLocationContainer.translationY = endY * value
                          lastLocationContainer.alpha = 1f - value
                          locationAdressView.alpha = value

                          if (value==1f) {
                              savedLocation.latLang?.let {l-> setCurrentLocation(LatLng(l.latitude,l.longtitude)) }
                          }
                      }
                      valueAnimator.start()
                  }
                }

                shippingTypesRecyclerview.adapter = LocationOffer.also {
                    shippingOfferAdapter = it
                }

                continueButton.setOnClickListener {
                    saveLocation()
                    startContinueButtonAnimation(false)
                }
                try {
                    mapFragment = requireFragmentManager().findFragmentById(R.id.lam_map) as SupportMapFragment?

                    mapFragment?.apply {
                        turnOnGps()
                        getMapAsync {
                            mMap = it.apply {
                                isMyLocationEnabled = true

                                setOnCameraMoveListener {
                                    if (!markerMoving) {
                                        startLocationIconAnimation(true)
                                        startContinueButtonAnimation(false)
                                    }
                                    markerMoving = true

                                    bottomSheetLocationBinding?.root?.parent?.requestDisallowInterceptTouchEvent(true)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {

                }
            }
            dismissVisibleDialog(this@apply)
            mapFragmentOpened = true
            show()
        }
    }

    //Safe
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        threadQueue.cleanupQueue()
        product.apply {
            threadQueue.postRunnable {
                checkIsProductLiked(product, object : ResultCallback<Boolean> {
                    override fun onSuccess(result: Boolean?) {
                        result?.let {
                            like = it
                            setLikesCount()
                        }
                    }
                })
            }
            threadQueue.postRunnable {
                getUser(sellerId,object :ResultCallback<User>{
                    override fun onSuccess(result: User?) {
                        result?.let {
                            sellerInfo = it.also {
                                applySellerInfo(it)
                            }
                        }
                    }
                })
            }
            threadQueue.postRunnable {
                currentUser?.let {
                    checkSubscribed(it.id,sellerId,object : ResultCallback<Boolean>{
                        override fun onSuccess(result: Boolean?) {
                            result?.let {
                                subscribedToThisSeller = it
                            }
                        }
                    })
                }
            }
            threadQueue.postRunnable {
                getDetailsForProduct(id, object : Result {
                    override fun onSuccess(any: Any?) {
                        (any as Details?)?.let { it ->
                            details = it
                            createDetails()

                            it.photos?.let {
                                photosList = it.also {
                                    photosAdapter?.setDataList(it)
                                }
                            }

                        }
                    }

                    override fun onFailed(message: String?) {
                        toast(requireContext(), message)
                    }
                })
            }
            threadQueue.postRunnable {
                getMoreProducts()
            }
            threadQueue.postRunnable {
                increaseProductViewCount(product.id)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
        binding: FragmentDetailsBinding
    ) {
        bottomNavVisiblity(context, false)

        setLightStatusBar(requireActivity(), true)

        specificationNameColor = requireContext().getColor(R.color.gray_999999)
        specificationValueColor = requireContext().getColor(R.color.black_333333)

        subscribeDrawale = getDrawable(R.drawable.account_add)

        binding.apply {
            detailsBackView.apply {
                isFocusable = true
                setOnClickListener {
                    closeLastFragment()
                }
            }

            detailsNavigateToCart.setOnClickListener {
                presentFragmentRemoveLast(
                    KorzinaFragment(),
                    removeLast = false)
            }

            detailsShareView.setOnClickListener {
                val currentItem = detailsPhotoViewPager.currentItem
                if (photosList!!.size > 0 && currentItem >= 0) {
                    lifecycleScope.launch {
                        shareProduct(
                            product,
                            requireContext()
                        )
                    }
                }

            }
            detailsOthersView.setOnClickListener {
                showOption(it)
            }

            photosAdapter = null

            photosList?.apply {
                clear()
                add(product.photo)
            }

            photosAdapter = DetailsPagerAdapter.apply {
                setDataList(photosList)
            }

            detailsPhotoViewPager.apply {
                adapter = photosAdapter
                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)

                        photosAdapter?.let {
                            val text = "${position + 1}/${photosList!!.size}"
                            detailsPhotosCountView.text = text

                        }
                    }
                })
            }

        }
        if (openAndSelectCommentId!=null) {
            AndroidUtilities.runOnUIThread({openComments()},600)
        }
    }

    private fun showOption(view: View) {
        PopupWindowLayout(requireContext()).apply {

            addItem(0,"Delete product",R.drawable.msg_delete) {
                val alertLayout = PopupWindowLayout(requireContext())
                val dialog = PopupDialog(alertLayout,DIALOG_ANIMATION_ALERT_DIALOG)

                val alert = inflateBinding<AlertDialogBinding>(null,R.layout.alert_dialog)

                alert.apply {
                    data = Alert("Delete product?","Are you sure you want to delete the your product?","CANCEL","DELETE PRODUCT",{
                           dialog.dismiss()
                    },{
                        dialog.dismiss()
                        products.remove(product)
                        closeLastFragment()

                    },product.photo)

                    alertLayout.addView(root,LinearLayout.LayoutParams(MyApplication.displaySize.first-AndroidUtilities.dp(48f),LinearLayout.LayoutParams.WRAP_CONTENT))
                    dialog.show(view,Gravity.CENTER,0,0,true)
                }
            }

            addItem(1,"Open profile",R.drawable.msg_openprofile) {
                presentFragmentRemoveLast(ProfileFragmentSeller().apply {
                    currentUser?.let {
                        bundleAny = it.id
                    }
                }, false)
            }
            PopupDialog(this).apply {
                measure(View.MeasureSpec.makeMeasureSpec(requireView().measuredWidth,View.MeasureSpec.UNSPECIFIED),View.MeasureSpec.makeMeasureSpec(requireView().measuredHeight,View.MeasureSpec.UNSPECIFIED))
                show(view,0,MyApplication.displaySize.first-measuredWidth,AndroidUtilities.dp(8f),true)
            }
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        menu.apply {
            menu.add("Delete").setOnMenuItemClickListener {
                deleteProduct(product,object : Result{
                    override fun onSuccess(any: Any?) {
                        view?.let {
                            snackBar(it,"Deleted successfully")
                        }
                    }
                })
                closeLastFragment()
                return@setOnMenuItemClickListener true
            }
            menu.add("Profile").setOnMenuItemClickListener {
                presentFragmentRemoveLast(ProfileFragmentSeller().apply {
                   currentUser?.let {
                       bundleAny = it.id
                   } },false)
                return@setOnMenuItemClickListener true
            }
        }
        super.onCreateContextMenu(menu, v, menuInfo)
    }
    //Safe
     private fun openComments() {
         if (commentsDialog==null){

             commentsDialog = CommentsFragment(this@DetailsFragment.product) { count: Int ->
                 product.commentCount = count
                 nestedBinding?.detailsCommentCountView?.text = count.toString()
                 return@CommentsFragment true

             }.apply {
                 selectedCommentId = openAndSelectCommentId
             }
         }

         commentsDialog?.apply {
             dismissVisibleDialog()
             fragmentController?.removeIfContainsAlready(this)
             fragmentController?.fragmentManager?.let {
                 show(it,null)
             }
         }
    }

    object DetailsPagerAdapter :
        DataBoundAdapter<DetailsPhotoPagerItemBinding, String>(R.layout.details_photo_pager_item) {
        override fun onCreateViewHolder(
            viewHolder: DataBoundViewHolder<DetailsPhotoPagerItemBinding>?,
            viewType: Int,
        ) {

        }

        override fun bindItem(
            holder: DataBoundViewHolder<DetailsPhotoPagerItemBinding>,
            binding: DetailsPhotoPagerItemBinding,
            position: Int,
            model: String,
        ) {

        }

    }

    object LocationOffer : DataBoundAdapter<ShippingOfferItemBinding, ShippingOffer>(R.layout.shipping_offer_item) {
        /**
         * Offers list
         */
        var list = arrayListOf(
            ShippingOffer(R.drawable.car_svg_start, SHIPPING_TYPE_START, 0L),
            ShippingOffer(R.drawable.car_svg_fast, SHIPPING_TYPE_FAST, 7999L),
            ShippingOffer(R.drawable.ultra, SHIPPING_TYPE_ULTRA, 13550L)
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
                                vibrator.vibrate(VibrationEffect.createOneShot(15,VibrationEffect.DEFAULT_AMPLITUDE))
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
            holder: DataBoundViewHolder<ShippingOfferItemBinding>,
            binding: ShippingOfferItemBinding,
            position: Int,
            model: ShippingOffer,
        ) {
            binding.apply {
                data = list[position]
                shioContainer.cardElevation = if (selectedPostition == position) AndroidUtilities.dp(8f).toFloat() else 0f
            }
        }
    }
}

enum class Currency {
    UZS,
    DOLLAR
}

class LatLng {
    var latitude = 0.0
    var longtitude = 0.0

    constructor(latitude: Double,longtitude:Double){
        this.latitude = latitude
        this.longtitude = longtitude
    }
    constructor()
}

class ShippingLocation {
    var id = System.currentTimeMillis()
    var latLang:com.example.market.LatLng?=null
    var adress: String?=null
    var type = SHIPPING_TYPE_START
    var cost = "0"
    var timeSpendMinute = 45
}
const val SHIPPING_TYPE_START = "Start"
const val SHIPPING_TYPE_FAST = "Fast"
const val SHIPPING_TYPE_ULTRA = "Ultra"
data class ShippingOffer(val photo: Int, val name: String, var cost: Long)

const val PICASSO_PRODUCT_DETAILS = "of4j4iopfj"

const val VIEW_TYPE_EMPTY_LIST = -8