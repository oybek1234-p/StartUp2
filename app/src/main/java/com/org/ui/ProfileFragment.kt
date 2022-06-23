package com.org.ui

import android.graphics.Rect
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.market.R
import com.example.market.databinding.EmptyScreenBinding
import com.example.market.databinding.FragmentProfileBinding
import com.example.market.databinding.ProfileProductItemBinding
import com.org.market.*
import com.org.net.addObserver
import com.org.net.models.Empty
import com.org.net.models.PHOTO
import com.org.net.models.Product
import com.org.net.models.UserFull
import com.org.net.newProductsDidLoad
import com.org.net.removeObserver
import com.org.net.userInfoDidLoad
import com.org.ui.actionbar.BaseFragment
import com.org.ui.adapters.*
import com.org.ui.components.EndlessRecyclerViewScrollListener
import com.org.ui.components.RecyclerListView
import com.org.ui.components.inflateBinding
import com.org.ui.components.visibleOrGone

class ProfileFragment(var userId: String) :
    BaseFragment<FragmentProfileBinding>(R.layout.fragment_profile) {

    var userInfo = UserFull()
    var isCurrentProfile = false

    private fun isCurrentUser() = userId == currentUserId()

    private var productsAdapter: ProductsAdapter? = null

    private var emptyView: EmptyScreenBinding? = null

    private var scrollListener: EndlessRecyclerViewScrollListener? = null


    companion object {
        private var emptyModel =
            Empty("Login",
                "Keep your data secure with us",
                "Login",
                { t->},
                "https://assets2.lottiefiles.com/private_files/lf30_m6j5igxb.json")
    }

    init {
        emptyModel.buttonClickAction =
            View.OnClickListener { presentFragment(LogInFragment(false), false) }
    }

    private var isUserLoading = false
        set(value) {
            if (value != field) {
                field = value
            }
        }

    override fun onFragmentDestroy() {
        super.onFragmentDestroy()
        isProductsLoading = false
        isUserLoading = false
        emptyView = null
        productsAdapter = null
    }

    private var isProductsLoading = false
        set(value) {
                field = value
                requireBinding().recyclerShimmer.apply {
                    if (value) {
                        productsAdapter?.setLoadingForShimmer()
                        showShimmer(true)
                    } else {
                        hideShimmer()
                    }
                }
        }

    fun updateUserLayout() {
        requireBinding().apply {
            user = userInfo
            executePendingBindings()
        }
    }

    private var isFirst = true

    fun notifyProductsAdapter() {
        productsAdapter?.apply {
            val products = DataController.getProducts(userId)
            submitList(products.toMutableList())
            if (products.isEmpty() && !isLoadingMore) {
                setEmpty()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (userId.isEmpty() && currentUserId().isNotEmpty()) {
            userId = currentUserId()
            onCreateView(requireBinding())
        }
        if (!isFirst) {
            notifyProductsAdapter()
        }
        isFirst = false

        loadMoreProducts()
    }

    override fun onCreateView(binding: FragmentProfileBinding) {
        actionBar.apply {
            backButtonIsVisible = !isCurrentProfile
            title = "Profile"
            addMenuItem(0, R.drawable.msg_search).setOnClickListener {
                presentFragment(SearchFragment(), false)
            }
        }
        val parent = fragmentView() as ViewGroup
        val noUser = userId == ""
        binding.appBarLayout.visibleOrGone(!noUser)
        if (noUser) {
            emptyView = inflateBinding<EmptyScreenBinding>(parent, R.layout.empty_screen).apply {
                data = emptyModel
                executePendingBindings()
                parent.addView(root,
                    CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.WRAP_CONTENT,
                        CoordinatorLayout.LayoutParams.WRAP_CONTENT).apply {
                        gravity = Gravity.CENTER
                    })
            }
            return
        } else {
            if (emptyView != null) {
                val root = emptyView!!.root
                parent.removeView(root)
                emptyView = null
            }
        }
        val cache = DataController.getUserFull(userId, false)
        if (cache != null) {
            userInfo = cache
        } else {
            val user = DataController.getUser(userId)
            if (user != null) {
                userInfo.user = user
            }
        }
        updateUserLayout()
        if (userInfo.user.id.isEmpty()) {
            isUserLoading = true
        }
        DataController.addSnapshotUser(userId) {
            isUserLoading = false
            val user = DataController.getUserFull(userId, false)
            if (user != null) {
                userInfo = user
                updateUserLayout()
            }
        }

        requireBinding().apply {
            if (isCurrentUser()) {
                photoView.setOnClickListener {
                    presentFragment(GalleryFragment(
                        {
                            val photo = it.getOrNull(0)
                            if (photo != null) {
                                userInfo.user.photo = photo
                                updateUserLayout()
                                uploadPhoto(photo)
                            }
                        }, 1), false)
                }
            }
            subscribersView.setOnClickListener {

            }
            subcriptionsView.setOnClickListener {

            }
            likesView.setOnClickListener {

            }

            createProductView.apply {
                visibleOrGone(isCurrentUser())
                setOnClickListener {
                    presentFragment(UploadProductFragment(), false)
                }
            }
            recyclerView.apply {
                adapter = ProductsAdapter().apply {
                    productsAdapter = this
                    setOnItemClickListener(object : RecyclerListView.RecyclerItemClickListener {
                        override fun onClick(position: Int, viewType: Int) {
                            toast("Clicked")
                        }
                    })
                }
                layoutManager = GridLayoutManager(requireActivity(), 3).apply {
                    spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(position: Int): Int {
                            val list = productsAdapter?.currentList
                            if (list != null) {
                                val item = list.getOrNull(position)
                                if (item != null) {
                                    return when (item.type) {
                                        VIEW_TYPE_PROGRESS -> 3
                                        VIEW_TYPE_EMPTY_LIST -> 3
                                        else -> 1
                                    }
                                }
                            }
                            return 1
                        }
                    }
                }
                val padding = dp(4f)
                addItemDecoration(object : RecyclerView.ItemDecoration() {
                    override fun getItemOffsets(
                        outRect: Rect,
                        view: View,
                        parent: RecyclerView,
                        state: RecyclerView.State,
                    ) {
                        super.getItemOffsets(outRect, view, parent, state)
                        val spanIndex =
                            (view.layoutParams as GridLayoutManager.LayoutParams).spanIndex
                        outRect.apply {
                            when (spanIndex) {
                                0 -> {
                                    right = padding
                                }
                                1 -> {
                                    right = padding
                                }
                            }
                            top = padding
                        }
                    }
                })
                addOnScrollListener(object :
                    EndlessRecyclerViewScrollListener(layoutManager as GridLayoutManager) {
                    override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                        loadMoreProducts()
                         toast("Load more")
                    }
                }.also {
                    scrollListener = it
                })
                isProductsLoading = false

                val cacheProducts = DataController.getProducts(userId)
                if (cacheProducts.isNotEmpty()) {
                    productsAdapter?.submitList(cacheProducts)
                } else {
                    toast("Products is empty")
                    isProductsLoading = true
                }
                loadMoreProducts()
            }
        }
    }

    private var isLoadingMore = false
        set(value) {
            if (value != field && !isProductsLoading) {
                field = value
                productsAdapter?.setLoadingMore(value)
            }
        }

    fun loadMoreProducts() {
        if (isLoadingMore) return
        isLoadingMore = true

        DataController.loadProducts(
            userId,
            filter = DataController.ProductFilter.Newest,
            sellerId = userId,
            limitSize = 8, resultCallback = productsLoadCallback
        )
    }

    private var productsLoadCallback = object : ResultCallback<ArrayList<Product>>() {
        override fun onSuccess(data: ArrayList<Product>?) {
            isProductsLoading = false
            isLoadingMore = false
            notifyProductsAdapter()
        }
    }

    private var isUploadingImage = false

    fun uploadPhoto(photo: String) {
        isUploadingImage = true
        uploadImageFromPath(photo, null) {
            isUploadingImage = false
            if (it != null) {
                val url = it
                currentUser().photo = url
                UserConfig.saveConfig()
                DataController.saveUserInfo(currentUserId(), PHOTO, url)
            }
        }
    }

    inner class ProductsAdapter :
        RecyclerListView.Adapter<ViewDataBinding, Product>(R.layout.profile_product_item,
            ProductDiff) {

        init {
            autoSetData = false
        }

        fun loadingList(): ArrayList<Product> {
            return arrayListOf<Product>().apply {
                for (i in 0..6) {
                    add(Product().apply {
                        id = newId()
                        type = -1
                    })
                }
            }
        }

        fun setLoadingForShimmer() {
            setLoadingMore(false)
            submitList(loadingList())
        }

        val emptyModel = Empty(
            "User products appear here",
            "Not uploaded products yet",
            "Add product",
            {
                presentFragment(UploadProductFragment(), false)
            }, "https://assets6.lottiefiles.com/packages/lf20_mvyhxnpt.json"
        )

        fun setEmpty() {
            val list = arrayListOf(Product().apply {
                id = newId()
                type = VIEW_TYPE_EMPTY_LIST
            })
            submitList(list)
        }

        fun setLoadingMore(show: Boolean) {
            val list = currentList.toMutableList()
            if (show) {
                list.add(Product().apply {
                    id = newId()
                    type = VIEW_TYPE_PROGRESS
                })
            } else {
                val progress = list.find { it.type == VIEW_TYPE_PROGRESS }
                if (progress != null) {
                    list.remove(progress)
                }
            }
            submitList(list)
        }

        override fun onViewHolderCreated(
            holder: RecyclerListView.BaseViewHolder<ViewDataBinding>,
            type: Int,
        ) {
            holder.apply {
                val view = holder.itemView
                val params = view.layoutParams as GridLayoutManager.LayoutParams
                //
            }
        }

        override fun bind(
            holder: RecyclerListView.BaseViewHolder<ViewDataBinding>,
            position: Int,
            model: Product,
        ) {
            holder.apply {
                binding.apply {
                    when (this) {
                        is EmptyScreenBinding -> {
                            data = emptyModel
                            executePendingBindings()
                        }
                        is ProfileProductItemBinding -> {
                            data = model
                            executePendingBindings()
                        }
                    }
                }
            }
        }

        override fun getItemLayoutId(position: Int, model: Product): Int {
            return when (model.type) {
                VIEW_TYPE_EMPTY_LIST -> emptyLayId
                VIEW_TYPE_PROGRESS -> progressLayId
                else -> R.layout.profile_product_item
            }
        }
    }
}