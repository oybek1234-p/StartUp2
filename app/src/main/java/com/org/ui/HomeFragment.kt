package com.org.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.Rect
import android.view.View
import androidx.core.view.doOnNextLayout
import androidx.databinding.OnRebindCallback
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.ActionBar.log
import com.example.market.R
import com.example.market.databinding.FragmentHomeBinding
import com.example.market.databinding.HomeSearchBarBinding
import com.org.market.*
import com.org.market.DataController.getPopularSearchList
import com.org.market.DataController.loadBanners
import com.org.market.DataController.loadParentCategories
import com.org.market.DataController.loadProducts
import com.org.net.NotificationCenterDelegate
import com.org.net.addObserver
import com.org.net.models.Banner
import com.org.net.models.Category
import com.org.net.models.Product
import com.org.net.models.SearchProduct
import com.org.net.newProductsDidLoad
import com.org.ui.actionbar.*
import com.org.ui.adapters.*
import com.org.ui.components.*

class HomeFragment : BaseFragment<FragmentHomeBinding>(R.layout.fragment_home),
    NotificationCenterDelegate {

    private var actionBarBinding: HomeSearchBarBinding? = null

    private var productsAdapter: HomeListAdapter? = null
    private var productsLayManager: StaggeredGridLayoutManager? = null

    private var isLoadingMore = false

    private val productsRequestId = currentTimeMillis().toInt()

    fun loadMore(reload: Boolean = false) {
        if (isLoadingMore) return
        showLoadingMore(true)
        loadProducts(
            productsRequestId,
            filter = DataController.ProductFilter.Newest,
            reload = reload,
            force = reload
        )
    }

    private var isOpened = false

    fun startOpenAnimation(show: Boolean,force: Boolean) {
        if (!isOpened || force){
            isOpened = true
            requireBinding().apply {
                root.visibleOrGone(true)
                recyclerView.visibleOrGone(true)
                root.apply {
                    doOnNextLayout {
                        val tY = dp(24f).toFloat()
                        val alp = 0f
                        if(show) {
                            translationY = tY
                            alpha = alp
                        }
                        animate().translationY(if (show) 0f else tY).alpha(if (show) 1f else alp).setDuration(300).setInterpolator(
                            decelerateInterpolator).withEndAction {
                            if (!show) {
                                root.visibleOrGone(false)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun didReceiveNotification(id: Int, vararg objects: Any) {
        super.didReceiveNotification(id, *objects)

        if (id == newProductsDidLoad) {
            (objects[0] as Array<*>)
            val objectss = objects[0] as Array<*>
            if (objectss[0] == productsRequestId) {
                startOpenAnimation(true,false)
                isLoading = false
                showLoadingMore(false)
                val list = DataController.getProducts(productsRequestId)
                if (list != null)
                    productsAdapter?.submitList(list.toMutableList())
                val isEmpty = productsAdapter!!.currentList.isEmpty()
                showEmpty(isEmpty)
            }
        }
    }

    fun showEmpty(show: Boolean) {
        mBinding?.emptyView?.visibleOrGone(show)
    }

    fun showLoadingMore(show: Boolean) {
        isLoadingMore = show
        productsAdapter?.isLoadingMore = show
    }

    private var isLoading = false
        set(value) {
            if (field != value) {
                field = value
                mBinding?.progressBar?.visibleOrGone(value)
            }
        }

    companion object {
        val offsetHorizontal = dp(0f)
        val offsetCenter = dp(2f)
        const val SEARCH_UPDATE_DURATION = 5000L

        var itemDecoration = object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State,
            ) {
                outRect.apply {
                    when ((view.layoutParams as StaggeredGridLayoutManager.LayoutParams).spanIndex) {
                        0 -> {
                            left = offsetHorizontal
                            top = offsetCenter
                            right = offsetCenter
                        }
                        1 -> {
                            left = offsetCenter
                            top = offsetCenter
                            right = offsetHorizontal
                        }
                    }
                }
            }
        }
    }

    private var searchTextList =
        arrayListOf("Search", "Popular products", "Best for you", "Recommended")


    override fun createActionBar(): View {
        return inflateBinding<HomeSearchBarBinding>(null,
            R.layout.home_search_bar).apply {
            actionBarBinding = this
            searchView.setOnClickListener {
                startOpenAnimation(false,true)
                presentFragment(CategoriesFragment(), false)
            }
            editText.setOnClickListener {
                searchView.performClick()
            }
            menuView.setOnClickListener {
                //presentMenu fragment
                applyTheme(R.style.Theme_StartUp2)
            }
            addOnRebindCallback(object : OnRebindCallback<HomeSearchBarBinding>() {
                override fun onBound(binding: HomeSearchBarBinding?) {
                    super.onBound(binding)
                    searchView.foreground =
                        createSelectorDrawable(changeBrightness(root.context.getThemeColor(R.attr.colorSurface),
                            0.8f), 4, 10)
                    menuView.background =
                        createsSelectorDrawable(changeBrightness(root.context.getThemeColor(R.attr.colorPrimary),
                            0.8f))
                }
            })
            executePendingBindings()
        }.root
    }

    override fun onFragmentCreate(): Boolean {

        getPopularSearchList(object : ResultCallback<ArrayList<SearchProduct>>() {
            override fun onFailed(exception: Exception?) {
                log(exception)
            }

            override fun onSuccess(data: ArrayList<SearchProduct>?) {
                if (data!!.isNotEmpty()) {
                    searchTextList.apply {
                        clear()
                        val list = ArrayList<String>()
                        data.forEach { list.add(it.text) }
                        addAll(list)
                    }
                }
            }
        })
        return super.onFragmentCreate()
    }

    override fun invalidateViewBindings() {
        super.invalidateViewBindings()
        actionBarBinding?.invalidateAll()
    }

    private var searchPosition = 0

    var searchUpdateRunnable = Runnable {
        searchPosition += 1
        if (searchPosition >= searchTextList.size) {
            searchPosition = 0
        }
        setSearchText(searchTextList[searchPosition])
        updateSearch()
    }

    val searchAnimator = ValueAnimator.ofFloat(1f, 0f).apply {
        duration = 150
        addUpdateListener {
            actionBarBinding?.editText?.apply {
                val value = it.animatedValue as Float
                alpha = value
                translationX = 40 - (40 * value)
            }
        }
        repeatCount = 1
        repeatMode = ValueAnimator.REVERSE
    }!!

    fun setSearchText(text: String) {
        searchAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationRepeat(animation: Animator?) {
                searchAnimator.removeListener(this)
                actionBarBinding?.editText?.apply {
                    hint = text
                }
            }
        })
        searchAnimator.start()
    }

    fun stopUpdateSearch() {
        cancelRunOnUIThread(searchUpdateRunnable)
    }

    fun updateSearch() {
        runOnUiThread(searchUpdateRunnable, SEARCH_UPDATE_DURATION)
    }

    private var isFirstResume = true

    override fun onResume() {
        super.onResume()

        updateSearch()
        if (!isFirstResume) {
            startOpenAnimation(true,true)
        }
        isFirstResume = false
    }

    override fun onPause() {
        super.onPause()
        stopUpdateSearch()
    }

    override fun onCreateView(binding: FragmentHomeBinding) {
        binding.apply {
            recyclerView.apply {
                addItemDecoration(HomeListDecoration)
                if (productsAdapter == null) {
                    productsAdapter = HomeListAdapter().apply {
                        setOnItemClickListener(
                            object : RecyclerListView.RecyclerItemClickListener {
                                override fun onClick(position: Int, viewType: Int) {
                                    if (viewType== productLayId) {
                                        presentFragment(DetailsFragment(currentList[position]),false)
                                    }
                                }
                            })
                    }
                }
                adapter = productsAdapter
                if (productsLayManager == null) {
                    productsLayManager =
                        StaggeredGridLayoutManager(
                            2,
                            StaggeredGridLayoutManager.VERTICAL
                        ).also {
                            addOnScrollListener(
                                object : EndlessRecyclerViewScrollListener(it) {
                                    override fun onLoadMore(
                                        page: Int,
                                        totalItemsCount: Int,
                                        view: RecyclerView?,
                                    ) {
                                        loadMore()
                                    }
                                })
                        }
                }
                layoutManager = productsLayManager
            }
            recyclerView.visibleOrGone(false)
        }

        DataController.products[productsRequestId] = ArrayList<Product>().apply {
            add(Product().apply {
                id = currentTimeMillis().toString()
                type = VIEW_TYPE_BANNER
            })
            add(Product().apply {
                id = currentTimeMillis().toString()
                type = VIEW_TYPE_MAIN_CATEGORY
            })
            add(Product().apply {
                id = currentTimeMillis().toString()
                type = VIEW_TYPE_POPULAR
            })
            productsAdapter?.submitList(this)
        }

        showEmpty(false)
        addObserver(this, newProductsDidLoad)
        isLoading = true
        loadBanners()
        loadCategories()
        loadPopularList()
        loadMore()
    }

    fun loadBanners() {
        loadBanners(object : ResultCallback<ArrayList<Banner>?>() {
            override fun onSuccess(data: ArrayList<Banner>?) {
                toast("Banners loaded")
                if (data != null)
                    productsAdapter?.banners = data
                productsAdapter?.notifyItemChanged(0)
            }
        })
    }

    fun loadCategories() {
        loadParentCategories(object : ResultCallback<ArrayList<Category>>() {
            override fun onSuccess(data: ArrayList<Category>?) {
                toast("Cate loaded")
                if (data != null)
                    productsAdapter?.apply {
                        categories = data
                        notifyItemChanged(1)
                    }
            }
        }, true)
    }

    fun loadPopularList() {
        loadProducts(currentTimeMillis().toInt(),
            10L,
            DataController.ProductFilter.Popular,
            resultCallback = object :
                ResultCallback<ArrayList<Product>>() {
                override fun onSuccess(data: ArrayList<Product>?) {
                    if (data != null)
                        productsAdapter?.apply {
                            popularList = data
                            notifyItemChanged(2)
                        }
                }
            })
    }

    object HomeListDecoration : RecyclerView.ItemDecoration() {
        var edgeSpacing: Int = dp(12f)
        var spacing: Int = dp(4f)

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State,
        ) {
            super.getItemOffsets(outRect, view, parent, state)

            val layoutParams = view.layoutParams as StaggeredGridLayoutManager.LayoutParams

            if (!layoutParams.isFullSpan) {
                val spanIndex =
                    (view.layoutParams as StaggeredGridLayoutManager.LayoutParams).spanIndex

                if (spanIndex == StaggeredGridLayoutManager.LayoutParams.INVALID_SPAN_ID) {
                    return
                }
                if (spanIndex == 0) {
                    outRect.set(edgeSpacing, spacing, spacing, spacing)
                } else {
                    outRect.set(spacing, spacing, edgeSpacing, spacing
                    )
                }
            }
        }
    }
}