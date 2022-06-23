package com.org.ui.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.market.R
import com.example.market.databinding.*
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.org.market.*
import com.org.net.models.Banner
import com.org.net.models.Category
import com.org.net.models.Product
import com.org.ui.components.RecyclerListView
import com.org.ui.components.inflateBinding
import com.org.ui.components.load
import com.org.ui.components.visibleOrGone


const val VIEW_TYPE_CATEGORY = 0
const val VIEW_TYPE_PRODUCT = 1
const val VIEW_TYPE_BANNER = 2
const val VIEW_TYPE_MAIN_CATEGORY = 3
const val VIEW_TYPE_MORE = 4
const val VIEW_TYPE_PRODUCT_SIMPLE = 5
const val VIEW_TYPE_PROGRESS = 999
const val VIEW_TYPE_CATEGORY_ITEM = R.layout.home_category_item
const val VIEW_TYPE_SIMPLE_TEXT = 7
const val VIEW_TYPE_EMPTY_LIST = 8
const val VIEW_TYPE_POPULAR = 9
const val VIEW_TYPE_HEADER = 10


const val progressLayId = R.layout.comment_loading_layout
const val mainCategoryLayId = R.layout.home_main_category_layout
const val categoryLayId = R.layout.home_category_layout
const val productLayId = R.layout.home_product_item
const val bannerLayId = R.layout.home_banner_layout
const val popularLayId = R.layout.popular_products_layout
const val headerLayId = R.layout.header_item
const val moreLayId = R.layout.home_product_more
const val emptyLayId = R.layout.empty_screen
const val productSimple = R.layout.product_simple

object ProductDiff : DiffUtil.ItemCallback<Product>() {
    override fun areItemsTheSame(oldItem: Product, newItem: Product) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Product, newItem: Product) = oldItem == newItem
}

class HomeListAdapter(var aspectRatio: Float? = null) :
    RecyclerListView.Adapter<ViewDataBinding, Product>(R.layout.home_product_item, ProductDiff) {

    private val itemsCache = ArrayList<HomeProductItemBinding>()
    private var cacheCreated = false

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerListView.BaseViewHolder<ViewDataBinding> {
        if (cacheCreated) {
            for (i in 0..12) {
                val binding = inflateBinding<HomeProductItemBinding>(parent,R.layout.home_product_item)
                itemsCache.add(binding)
            }
            cacheCreated = true
        }

        var binding: HomeProductItemBinding? = null
        if (itemsCache.isNotEmpty() && viewType == productLayId) {
            binding = itemsCache[0]
            itemsCache.removeAt(0)
        }
        if (binding != null) {
            return RecyclerListView.BaseViewHolder(binding, clickListener)
        }
        return super.onCreateViewHolder(parent, viewType)
    }

    var holdersPool = RecyclerView.RecycledViewPool().apply {
        setMaxRecycledViews(VIEW_TYPE_CATEGORY_ITEM, 12)
    }

    var banners = ArrayList<Banner>()
        set(value) {
            field = value
            notifyItemChanged(0)
        }

    var categories = ArrayList<Category>()
        set(value) {
            field = value
            notifyItemChanged(1)
        }

    var popularList = ArrayList<Product>()
        set(value) {
            field = value
            notifyItemChanged(2)
        }

    private fun setRecyclerPool(recyclerView: RecyclerView) {
        recyclerView.setRecycledViewPool(holdersPool)
    }

    fun updateLikeView(binding: HomeProductItemBinding, like: Boolean, likeCount: Int = -1) {
        binding.apply {
            likeImageView.imageTintList =
                ColorStateList.valueOf(if (like) Color.rgb(254, 44, 85) else Color.WHITE)
            if (likeCount != -1) {
                likeTextView.text = likeCount.toString()
            }
        }
    }

    override fun onViewHolderCreated(
        holder: RecyclerListView.BaseViewHolder<ViewDataBinding>,
        type: Int,
    ) {
        super.onViewHolderCreated(holder, type)
        holder.apply {
            binding.apply {
                when (type) {
                    productLayId -> {
                        (this as HomeProductItemBinding)
                        likeImageView.setOnClickListener {
                            val item = getItem(layoutPosition)
                            DataController.setLikedProduct(item) { isSuccess, liked, errorMessage ->
                                item.likes = if (liked) item.likes + 1 else item.likes - 1
                                updateLikeView(this, liked, item.likes)
                            }
                        }
                        detailsCommentIconView.setOnClickListener {

                        }
                        playIconView.setOnClickListener {
                            vibrate(5)
                            onScrollStateChanged(RecyclerView.SCROLL_STATE_IDLE, playPosition = layoutPosition)
                        }
                    }
                    progressLayId -> setFullSpan(true)
                    bannerLayId -> {
                        (this as HomeBannerLayoutBinding)
                        setFullSpan(true)
                        viewPager.adapter =
                            RecyclerListView.SimpleAdapter<HomeBannerItemBinding, Banner>(R.layout.home_banner_item)
                                .apply {
                                    setOnItemClickListener(object :
                                        RecyclerListView.RecyclerItemClickListener {
                                        override fun onClick(position: Int, viewType: Int) {

                                        }
                                    })
                                }
                        dotsIndicator.setViewPager2(viewPager)
                    }
                    mainCategoryLayId -> {
                        (this as HomeMainCategoryLayoutBinding)
                        setFullSpan(true)
                        recyclerView.apply {
                            adapter =
                                RecyclerListView.SimpleAdapter<HomeMainCategoryItemBinding, Category>(
                                    R.layout.home_main_category_item)
                                    .apply {
                                        setOnItemClickListener(object :
                                            RecyclerListView.RecyclerItemClickListener {
                                            override fun onClick(position: Int, viewType: Int) {

                                            }
                                        })
                                    }
                            layoutManager = GridLayoutManager(context, 5)
                        }
                    }
                    popularLayId -> {
                        setFullSpan(true)
                        (this as PopularProductsLayoutBinding)
                        seeAllView.setOnClickListener {

                        }
                    }
                }
            }
        }
    }

    private var currentPlayingPosition = -1
    private var ignorePlayPos = -1
    private var currentlyPlayingHolder: RecyclerListView.BaseViewHolder<HomeProductItemBinding>? =
        null

    val firstVisibleArray = IntArray(2)
    val lastVisibleArray = IntArray(2)
    var firstVisiblePosition = 0
    var lastVisiblePosition = 0

    fun onScrollStateChanged(newState: Int, restart: Boolean = false, playPosition: Int = -1) {
        val recyclerView = mRecyclerView!!
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            var playerBindingToPlay: RecyclerListView.BaseViewHolder<HomeProductItemBinding>? =
                null
            if (playPosition == -1) {
                //Get visible range positions
                val layoutManager = recyclerView.layoutManager as StaggeredGridLayoutManager
                layoutManager.findFirstVisibleItemPositions(firstVisibleArray)
                layoutManager.findLastVisibleItemPositions(lastVisibleArray)
                firstVisiblePosition = firstVisibleArray[0]
                lastVisiblePosition = lastVisibleArray[0]

                var startOffset = firstVisiblePosition - 5
                var endOffset = lastVisiblePosition + 5

                if (startOffset < 0) {
                    startOffset = 0
                }
                if (endOffset > currentList.size - 1) {
                    endOffset = currentList.size - 1
                }

                var playingFound = false
                var maxHeight = -1

                //Find more visible video
                for (i in startOffset..endOffset) {
                    val item = getItem(i)
                    if (item.videoUrl.isNotEmpty()) {
                        val holder =
                            recyclerView.findViewHolderForAdapterPosition(i) as RecyclerListView.BaseViewHolder<*>?

                        if (holder != null && item.type == VIEW_TYPE_PRODUCT) {
                            (holder as RecyclerListView.BaseViewHolder<HomeProductItemBinding>)

                            val itemView = holder.itemView
                            val visibleRect = Rect()
                            itemView.getGlobalVisibleRect(visibleRect)

                            val itemVisibleHeight = visibleRect.bottom - visibleRect.top

                            if (itemVisibleHeight > 0 && !playingFound && i > ignorePlayPos) {
                                if (itemVisibleHeight == itemView.measuredHeight) {
                                    playingFound = true
                                    maxHeight = itemVisibleHeight
                                    playerBindingToPlay = holder
                                } else {
                                    if (itemVisibleHeight > maxHeight) {
                                        maxHeight = itemVisibleHeight
                                        playerBindingToPlay = holder
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                playerBindingToPlay =
                    recyclerView.findViewHolderForLayoutPosition(playPosition) as RecyclerListView.BaseViewHolder<HomeProductItemBinding>?
            }

            if (playerBindingToPlay != null) {
                //Check is same video playing
                if (playerBindingToPlay == currentlyPlayingHolder) {
                    checkIsPlayingVideo(playerBindingToPlay, restart)
                    return
                }

                try {
                    //Stop previous video
                    val oldHolder =
                        recyclerView.findViewHolderForLayoutPosition(currentPlayingPosition) as RecyclerListView.BaseViewHolder<HomeProductItemBinding>?
                    if (oldHolder != null)
                        checkIsNotPlaying(oldHolder)
                } catch (e: Exception) {

                }
                //Start next video
                checkIsPlayingVideo(playerBindingToPlay, restart)
            } else {
                //Next playing video not found play a loop again
                if (ignorePlayPos != -1) {
                    ignorePlayPos = -1
                    onScrollStateChanged(RecyclerView.SCROLL_STATE_IDLE, true)
                }
            }
        }
    }


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                onScrollStateChanged(newState)
            }
        })
    }

    fun checkIsNotPlaying(holder: RecyclerListView.BaseViewHolder<HomeProductItemBinding>) {
        holder.binding.apply {

        }
    }

    fun checkIsPlayingVideo(
        holder: RecyclerListView.BaseViewHolder<HomeProductItemBinding>,
        restart: Boolean = false,
    ) {
        holder.binding.apply {

        }
    }

    override fun onViewDetachedFromWindow(holder: RecyclerListView.BaseViewHolder<ViewDataBinding>) {
        super.onViewDetachedFromWindow(holder)

    }

    fun bindProduct(binding: HomeProductItemBinding, product: Product) {
        binding.apply {
            val hasVideo = product.videoUrl.isNotEmpty()

            photoView.load(
                url = product.photo,
                fade = true,
                thumbnail = true,
                aspectRatio = product.photoScaleRatio,)

            userPhotoView.load(product.sellerPhoto, circleCrop = true)

            playIconView.visibleOrGone(hasVideo)
            progressView.visibleOrGone(hasVideo)

            titleView.text = product.title
            costView.text = formatCurrency(product.cost)
            val sellerName = product.sellerName.ifEmpty { "New seller" }
            sellerInfoTextView.text = "$sellerName * ${product.category.name}"
            likeTextView.text = product.likes.toString()
            detailsCommentCountView.text = product.commentsCount.toString()
            viewsTextView.text = "${product.views} views"
            soldTextView.text = "${product.sold} sold"
            val discountVisible = product.discountPercent > 0
            discountView.apply {
                visibleOrGone(discountVisible)
                text = if (discountVisible) getDiscount(product.cost,
                    product.discountPercent) else null
            }
            discountPercentView.apply {
                visibleOrGone(discountVisible)
                text = if (discountVisible) "-${product.discountPercent}%" else null
            }
            timeView.text = getDateDifferenceText(product.uploadedDate)
        }
    }

    override fun bind(
        holder: RecyclerListView.BaseViewHolder<ViewDataBinding>,
        position: Int,
        model: Product,
    ) {
        holder.binding.apply {
            when (this) {
                is HomeProductItemBinding -> {
                    containerView.radius = (dp(4f)..dp(12f)).random().toFloat()
                    bindProduct(this, model)
                    this.apply {
                        DataController.checkProductLiked(model.id) { isLiked, error ->
                            updateLikeView(this, isLiked)
                        }
                    }
                }
                is HomeBannerLayoutBinding -> {
                    (viewPager.adapter
                            as RecyclerListView.SimpleAdapter<HomeBannerItemBinding, Banner>)
                        .setDataList(banners)
                }
                is HomeMainCategoryLayoutBinding -> {
                    (recyclerView.adapter
                            as RecyclerListView.SimpleAdapter<HomeMainCategoryItemBinding, Category>)
                        .setDataList(categories)
                }
                is PopularProductsLayoutBinding -> {
                    toast(popularList.size.toString() + " popular list size")
                    popularList.forEachIndexed { index, product ->
                        bindPopularItem(
                            when (index) {
                                0 -> include
                                1 -> include2
                                else -> include3
                            },
                            product
                        )
                    }
                }
            }

        }
    }

    fun bindPopularItem(binding: PopularProductsItemBinding, product: Product) {
        binding.product = product
        binding.executePendingBindings()
    }

    private val loadingProduct = Product().apply {
        id = currentTimeMillis().toString()
        type = VIEW_TYPE_PROGRESS
    }

    var isLoadingMore = false
        set(value) {
            if (field != value) {
                field = value

                val list = currentList.toMutableList()

                list.remove(list.find { it.type == VIEW_TYPE_PROGRESS })

                if (value) {
                    list.add(loadingProduct)
                }

                submitList(list)
            }
        }

    override fun getItemLayoutId(position: Int, model: Product): Int {
        return when (getItem(position).type) {
            VIEW_TYPE_PRODUCT -> productLayId
            VIEW_TYPE_MORE -> moreLayId
            VIEW_TYPE_EMPTY_LIST -> emptyLayId
            VIEW_TYPE_PRODUCT_SIMPLE -> productSimple
            VIEW_TYPE_PROGRESS -> progressLayId
            VIEW_TYPE_SIMPLE_TEXT -> R.layout.comment_single_text_view
            VIEW_TYPE_BANNER -> bannerLayId
            VIEW_TYPE_MAIN_CATEGORY -> mainCategoryLayId
            VIEW_TYPE_POPULAR -> popularLayId
            else -> productLayId
        }
    }
}



