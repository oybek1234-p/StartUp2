package com.org.ui

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.market.R
import com.example.market.databinding.DetailsPhotoPagerItemBinding
import com.example.market.databinding.FragmentDetailsBinding
import com.example.market.databinding.HomeSearchBarBinding
import com.example.market.databinding.SpecificationsLayoutBinding
import com.org.market.*
import com.org.net.models.*
import com.org.ui.actionbar.*
import com.org.ui.adapters.HomeListAdapter
import com.org.ui.components.RecyclerListView
import com.org.ui.components.inflateBinding
import com.org.ui.components.load
import com.org.ui.components.visibleOrGone
import com.stfalcon.imageviewer.StfalconImageViewer

class DetailsFragment(val product: Product) :
    BaseFragment<FragmentDetailsBinding>(R.layout.fragment_details) {

    private var photoAdapter:ListAdapter?=null

    class ListAdapter: RecyclerListView.SimpleAdapter<DetailsPhotoPagerItemBinding,Photo>(R.layout.details_photo_pager_item){
        override fun onViewHolderCreated(
            holder: RecyclerListView.BaseViewHolder<DetailsPhotoPagerItemBinding>,
            type: Int
        ) {
            holder.apply {
                binding.apply {

                }
            }
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            val holder = recyclerView.findViewHolderForLayoutPosition(0) as RecyclerListView.BaseViewHolder<DetailsPhotoPagerItemBinding>
            holder.binding.playerView.player!!.release()
        }

        override fun onViewDetachedFromWindow(holder: RecyclerListView.BaseViewHolder<DetailsPhotoPagerItemBinding>) {
            super.onViewDetachedFromWindow(holder)
            holder.binding.playerView.player!!.stop()
        }
        override fun bind(
            holder: RecyclerListView.BaseViewHolder<DetailsPhotoPagerItemBinding>,
            position: Int,
            model: Photo
        ) {
            holder.binding.apply {
                val isVideo = model.id == "Video"
                if (isVideo) {
                    detailsPhotoView.visibleOrGone(false)
                    playerView.apply {
                        visibleOrGone(true)
//                        val item = MediI
//                        player!!.apply {
//                            setMediaItem(item)
//                            prepare()
//                            play()
//                        }
                    }
                } else {
                    detailsPhotoView.visibleOrGone(true)
                    detailsPhotoView.load(model.url, thumbnail = true, fade = true)
                }
            }
        }
    }

    private var photos = arrayListOf<Photo>().apply {
        val photo = Photo()
        if (product.videoUrl.isNotEmpty()) {
            photo.id = "Video"
            photo.url = product.videoUrl
        } else {
            photo.url = product.photo
        }
        add(photo)
    }
    private var photosIsLoading = false

    private var moreAdapter: HomeListAdapter? = null
    private var moreProductsEmpty = false
        set(value) {
            if (field != value) {
                field = value
                getNestedLayout().moreEmptyView.visibleOrGone(value, 1)
            }
        }

    private var specifications = arrayListOf<Specification>()

    override fun createActionBar(): View {
        return inflateBinding<HomeSearchBarBinding>(null, R.layout.home_search_bar).apply {
            backButton.setOnClickListener {
                onBackPressed()
            }
            isSearch = true
            showBackButton = true
            menuView.setImageResource(R.drawable.msg_actions)
            menuView.setOnClickListener {
                val popupWindow = PopupWindowLayout(requireActivity()).apply {
                    addItem(0, "Profile", R.drawable.profile_icon) {
                        presentFragment(ProfileFragment(currentUserId()), false)
                    }
                    addItem(1, "Categories", R.drawable.msg_list) {
                        presentFragment(CategoriesFragment(), false)
                    }
                }
                val popupDialog = PopupDialog(popupWindow)
                popupDialog.show(menuView, 0.2f)
            }
        }.root
    }

    fun updatePhotosCount() {
        val current = requireBinding().detailsPhotoViewPager.currentItem + 1
        requireBinding().countView.text = "$current/${photos.size}"
    }

    override fun onCreateView(binding: FragmentDetailsBinding) {
        binding.apply {
            detailsPhotoViewPager.apply {
                adapter = ListAdapter().apply {
                    photoAdapter = this
                    setOnItemClickListener(object :
                        RecyclerListView.RecyclerItemClickListener {
                        override fun onClick(position: Int, viewType: Int) {
                            val item = photos[position].id
                            if (item=="Video") {
                                toast("Open full video viewer")
                            } else {
                                val list = arrayListOf<String>()
                                photos.forEach {
                                    list.add(it.url)
                                }
                                openPhotoViewer(context(), getViewHolder(position)!!.itemView as ImageView,list)
                            }
                        }
                    })
                }
                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        updatePhotosCount()
                    }
                })
            }

            nestedLayout.apply {
                data = this@DetailsFragment.product
                executePendingBindings()

                likeView.setOnClickListener {
                    if (hasUser()) {
                        DataController.setLikedProduct(product) { _, _, errorMessage ->
                            if (errorMessage!=null) {
                                toast(errorMessage)
                                return@setLikedProduct
                            }
                            updateProductLiked(false)
                        }
                    } else {
                        openLogin()
                    }
                }

                var user = DataController.getUser(product.sellerId)

                sellerInfoLayout.apply {
                    linearLayout4.setOnClickListener {
                        presentFragment(ProfileFragment(product.sellerId),false)
                    }
                    dostavkaLayout.setOnClickListener {
                        presentFragment(LocationFragment {
                        },false)
                    }
                    photoView.setOnClickListener {
                        presentFragment(ProfileFragment(product.sellerId),false)
                    }
                    subscribeButton.setOnClickListener {
                        DataController.subscribeToUser(product.sellerId) { s, sub ->
                            updateSubscribedView(!sub,true)
                        }
                    }
                    updateSubscribed(false)
                    if (user == null) {
                        user = User().apply {
                            id = product.sellerId
                            name = product.sellerName
                            photo = product.sellerPhoto
                            likes = product.sellerLikes
                            subscribers = product.sellerSubscribersCount
                            products = product.sellerProducts
                        }
                    }
                    updateSellerInfo(user!!)
                }
                descriptionButton.apply {
                    titleView.text = "Description"
                }
                descriptionTextView.text = product.subtitle
                moreProductsHeader.apply {
                    titleView.text = "Recomended"
                    actionTextView.setOnClickListener {
                        presentFragment(SearchResultFragment(), false)
                    }
                }
                moreProductsRecyclerView.apply {
                    adapter = HomeListAdapter().apply {
                        moreAdapter = this
                        setOnItemClickListener(object : RecyclerListView.RecyclerItemClickListener {
                            override fun onClick(position: Int, viewType: Int) {
                                presentFragment(DetailsFragment(currentList[position]), false)
                            }
                        })
                    }
                    layoutManager =
                        GridLayoutManager(context(), 2, GridLayoutManager.VERTICAL, false)
                    //    addItemDecoration(HomeFragment.itemDecoration)
                    moreProductsEmpty = false
                }
            }
        }
        updateProductLiked(true)
        loadPhotos()
        loadSpecifications()
        loadUser()
        loadMoreProducts()
    }

    fun updateSubscribed(animate: Boolean) {
        DataController.checkSubscribed(product.sellerId) {
            updateSubscribedView(!it,animate)
        }
    }

    fun updateSubscribedView(show: Boolean,animate: Boolean) {
        getNestedLayout().sellerInfoLayout.subscribeButton.apply {
            if(animate) {
                visibleOrGone(true)
                if (show) {
                    alpha = 0f
                    scaleX = 0f
                    scaleY = 0f
                }
                val scale = if (show) 1f else 0f
                animate().scaleY(scale).scaleX(scale).alpha(scale).withEndAction {
                    if (!show) {
                        visibleOrGone(false)
                    }
                }.setDuration(250).start()
            } else {
                visibleOrGone(show)
            }
        }
    }

    private fun updateProductLiked(ignoreCount: Boolean) {
        var network = false
        val fromNetwork = DataController.checkProductLiked(product.id) { data, error ->
            val isLiked = !data != true
            getNestedLayout().apply {
                liked = isLiked
                if (!ignoreCount) {
                    if (isLiked) {
                        product.likes += 1
                    } else {
                        product.likes -= 1
                    }
                }
                this.data = product
                executePendingBindings()

                if (network) {
                    likeView.apply {
                        visibleOrGone(true)
                        scaleY = 0f
                        animate().scaleY(1f).setDuration(200).start()
                    }
                }
            }
        }
        if (fromNetwork) {
            network = true
            getNestedLayout().likeView.visibleOrGone(false)
        }
    }

    fun getNestedLayout() = requireBinding().nestedLayout

    fun loadMoreProducts() {
        moreAdapter?.isLoadingMore = true
        moreProductsEmpty = false
        DataController.loadProducts(classGuid,
            catId = product.category.id,
            filter = DataController.ProductFilter.Newest,
            resultCallback = object :
                ResultCallback<ArrayList<Product>>() {
                override fun onSuccess(data: ArrayList<Product>?) {
                    moreAdapter?.isLoadingMore = false
                    moreAdapter?.submitList(data)
                    if (data.isNullOrEmpty()) {
                        moreProductsEmpty = true
                    }
                }
            })
    }

    fun loadPhotos() {
        photosIsLoading = true
        DataController.loadProductPhotos(product.id, object : ResultCallback<ArrayList<Photo>>() {
            override fun onSuccess(data: ArrayList<Photo>?) {
                super.onSuccess(data)
                photosIsLoading = false
                if (data == null || data.isEmpty()) return
                photos.addAll(1,data)
                photoAdapter?.setDataList(photos)
                updatePhotosCount()
            }
        })
    }

    fun loadSpecifications() {
        DataController.getSpecification(product.id,
            object : ResultCallback<ArrayList<Specification>>() {
                override fun onSuccess(data: ArrayList<Specification>?) {
                    super.onSuccess(data)
                }
            })
    }

    fun loadUser() {
        DataController.loadUser(product.id, object : ResultCallback<User>() {
            override fun onSuccess(data: User?) {
                if (data == null) return
                updateSellerInfo(data)
            }
        })
    }

    private fun updateSellerInfo(user: User) {
        getNestedLayout().sellerInfoLayout.apply {
            photoView.load(user.photo, circleCrop = true)
            nameView.text = user.name.ifEmpty { "New user" }
            subscribersTextView.text = "${user.subscribers} subscribers"
            productsTextView.text = "${user.products} products"
            if (ShippingController.shippingLocation.id > 0L) {
                dostavkaTextView.text = formatCurrency(ShippingController.shippingLocation.cost)
            } else {
                dostavkaTextView.text = "14.000 sum"
            }
            likesTextView.text = "${user.likes} likes"
        }
    }

    @SuppressLint("NewApi")
    fun showSpecification() {
        val bottomSheet = object :
            BottomSheet<SpecificationsLayoutBinding>(context(), R.layout.specifications_layout) {
            override fun onViewCreated(binding: SpecificationsLayoutBinding) {
                super.onViewCreated(binding)
                title().text = "Specifications"
                button().apply {
                    setImageResource(R.drawable.exit_icon)
                    setOnClickListener {
                        dismiss()
                    }
                }
                binding.container.apply {
                    orientation = LinearLayout.VERTICAL
                    val padding = dp(12f)
                    specifications.forEach {
                        val frameLayout = FrameLayout(context).apply {
                            setPadding(padding, padding, padding, padding)
                            val nameView = TextView(context, null, R.style.Subtitle1_14).apply {
                                setTextColor(getThemeColor(R.attr.colorOnSurfaceMedium))
                                text = it.name
                            }
                            addView(nameView,
                                FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                                    FrameLayout.LayoutParams.WRAP_CONTENT).apply {
                                    gravity = Gravity.START
                                })
                            val valueView = TextView(context, null, R.style.Title3_17).apply {
                                setTextColor(getThemeColor(R.attr.colorOnSurfaceHigh))
                                text = it.value
                            }
                            addView(valueView,
                                FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                                    FrameLayout.LayoutParams.WRAP_CONTENT).apply {
                                    gravity = Gravity.END
                                })
                        }
                        addView(frameLayout,
                            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT))
                    }
                }
            }
        }
        showDialog(bottomSheet)
    }
}