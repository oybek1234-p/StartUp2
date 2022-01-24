package com.example.market.home

import android.graphics.BitmapFactory
import android.view.Gravity
import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.market.*
import com.example.market.comment.CommentsFragment
import com.example.market.databinding.*
import com.example.market.model.Header
import com.example.market.model.Product
import com.example.market.recycler.*
import com.example.market.utils.getString
import com.example.market.utils.log
import com.example.market.viewUtils.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.sql.Blob

const val VIEW_TYPE_CATEGORY = 0
const val VIEW_TYPE_PRODUCT = 1
const val VIEW_TYPE_BANNER = 2
const val VIEW_TYPE_MAIN_CATEGORY = 3
const val VIEW_TYPE_MORE = 4
const val VIEW_TYPE_PRODUCT_SIMPLE = 5
const val VIEW_TYPE_PROGRESS = 6
const val VIEW_TYPE_CATEGORY_ITEM = R.layout.home_category_item
const val VIEW_TYPE_SIMPLE_TEXT = 7
const val progressLayId = R.layout.comment_loading_layout
const val mainCategoryLayId = R.layout.home_main_category_layout
const val categoryLayId = R.layout.home_category_layout
const val productLayId = R.layout.home_product_item
const val bannerLayId = R.layout.home_banner_layout
const val headerLayId = R.layout.header_item
const val moreLayId = R.layout.home_product_more
const val noOtherProducts = R.layout.empty_screen
const val productSimple = R.layout.product_simple

class HomeListAdapter(var aspectRatio: Float?=null) : SimpleAdapter<ViewDataBinding,Product>(ProductDiff, R.layout.home_product_item) {
    /**
     * Recycler view pool
     */
    var holdersPool  = RecyclerView.RecycledViewPool().apply {
        setMaxRecycledViews(VIEW_TYPE_CATEGORY_ITEM,12)
    }
    /**
     * Create main category
     */
    private fun createMainCategory(binding: HomeMainCategoryLayoutBinding) {
        binding.homeMainCategoryRecyclerView.apply {

            setRecyclerPool(this)

            adapter = HomeMainCategoryListAdapter().apply {
                setClickListener(clickListener)
            }

            layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
            ).apply {
                recycleChildrenOnDetach = true
            }
        }
    }

    private var progressModel = Product().apply {
        id = System.currentTimeMillis().toString()
        type = VIEW_TYPE_PROGRESS
    }

    var loading = false
    set(value) {
        if (value!=field) {
            field = value

            val list = currentList.toMutableList()

            if (value) {
                list.add(progressModel)
            } else {
                list.remove(progressModel)
            }

            submitList(list)
        }
    }

    var viewsCount = -1

    //Use soldcount as lottie raw res.
    //Use narxi as description text.
    var emptyModel = Product().apply {
        id = System.currentTimeMillis().toString()
        type = VIEW_TYPE_EMPTY_LIST
        this.soldCount = R.raw.no_result_lottie
        this.title = "Empty list"
        this.narxi = "Try again"
    }

    fun setEmpty() {
        currentList.toMutableList().apply {
            clear()
            add(emptyModel)
            submitList(this)
        }
    }
    /**
     * Create banner
     */
    private fun createBanner(binding: HomeBannerLayoutBinding): HomeBannerAdapter {
        binding.apply {
            homeBannerViewPager.apply {

                adapter = HomeBannerAdapter().apply {

                    setClickListener(clickListener)

                }
                wormDotsIndicator.setViewPager2(this)

                nextPage(this)

                return adapter as HomeBannerAdapter
            }
        }
    }

    private fun setRecyclerPool(recyclerView: RecyclerView) {
        recyclerView.setRecycledViewPool(holdersPool)
    }

    /**
     * Create category
     */
    private fun createCategory(binding: HomeCategoryLayoutBinding) {
        binding.homeCategoryRecyclerView.apply {

            setRecyclerPool(this)

            layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
            ).apply {
                recycleChildrenOnDetach = true
            }
        }
    }

    override fun onCreateViewHolderCreated(holder: BaseViewHolder<ViewDataBinding>, type: Int) {

        when(type) {
            progressLayId -> {
                setFullSpan(holder,true)
            }
            noOtherProducts -> {
                setFullSpan(holder,true)
            }
            R.layout.comment_single_text_view -> {
                setFullSpan(holder,true)
                (holder.binding as CommentSingleTextViewBinding).apply {

                }
            }
            headerLayId -> {
                setFullSpan(holder,true)
            }
            R.layout.home_product_more -> {
                setFullSpan(holder,false)
            }

            R.layout.home_product_item -> {
                setFullSpan(holder,false)
                (holder.binding as HomeProductItemBinding).apply {
                    likeView.setOnClickListener {
                        toast("Like")
                    }
                    commentView.setOnClickListener {
                        val pos = holder.layoutPosition

                        val commentsFragment = CommentsFragment(getItem(pos)) {
                            getItem(pos).commentCount = it
                            notifyItemChanged(pos)
                            return@CommentsFragment true
                        }

                        (holder.itemView.context as MainActivity?)?.let {
                            it.supportFragmentManager.let { m->
                                commentsFragment.show(m,null)
                            }
                        }
                    }
                }
            }
            productSimple -> setFullSpan(holder,false)

            R.layout.home_banner_layout -> {
                setFullSpan(holder,true)
                createBanner(holder.binding as HomeBannerLayoutBinding)
            }

            R.layout.home_category_layout -> {
                setFullSpan(holder,true)
                createCategory(holder.binding as HomeCategoryLayoutBinding)
            }

            R.layout.home_main_category_layout -> {
                setFullSpan(holder,true)
                createMainCategory(holder.binding as HomeMainCategoryLayoutBinding)
            }
        }
    }

    private val customHeader = Header()
    init {
        customHeader.title = getString(R.string.header_title)
    }

    override fun bind(holder: BaseViewHolder<ViewDataBinding>, position: Int, model: Product) {
        holder.binding.apply {
            aspectRatio?.let {
                model.scaleRatio = it
            }
            when(this) {

                is HomeProductItemBinding -> {
                    model.thumbnailObject?.let {
                        imageViewHome.thumbnailBlobObejct = it
                    }
                    data = model
                }
                is CommentSingleTextViewBinding -> {
                    gravity = Gravity.CENTER
                    data = "Views: $viewsCount"
                }
                is ProductSimpleBinding -> {
                    data = model
                }
                is HeaderItemBinding -> {
                    data = customHeader
                }
                is EmptyScreenBinding -> {
                    this.apply {
                        lottieView.setAnimation(emptyModel.soldCount)
                        titleView.text = emptyModel.title
                        subtitleView.text = emptyModel.narxi
                        addItemButton.visibility = View.GONE
                    }
                }
                is HomeBannerLayoutBinding -> {

                    homeBannerViewPager.adapter?.apply {

                        if (this is HomeBannerAdapter) {
                            with(model.bannerList){
                                setDataList(this)
                            }
                        }

                    }

                }

                is HomeCategoryLayoutBinding -> {

                    homeCategoryRecyclerView.adapter?.apply {

                    }

                }

                is HomeMainCategoryLayoutBinding -> {

                    homeMainCategoryRecyclerView.adapter?.apply {
                        if (this is HomeMainCategoryListAdapter){
                            model.mainCategoryList?.let {
                                setDataList(it)
                            }
                        }
                    }
                }

                is HomeProductMoreBinding->{
                    (holder.binding as HomeProductMoreBinding).data = model
                }
            }
        }
    }

    override fun onViewRecycled(holder: BaseViewHolder<ViewDataBinding>) {
        super.onViewRecycled(holder)
        if (holder.itemViewType == VIEW_TYPE_PRODUCT) {
            Glide
                .with(MyApplication.appContext)
                .clear(
                    (holder.binding as HomeProductItemBinding).imageViewHome
                )
        }
    }

    override fun getItemLayoutId(position: Int): Int {

        return when(getItem(position).type) {
            VIEW_TYPE_PRODUCT -> productLayId
            VIEW_TYPE_BANNER -> bannerLayId
            VIEW_TYPE_CATEGORY -> categoryLayId
            VIEW_TYPE_MAIN_CATEGORY -> mainCategoryLayId
            VIEW_TYPE_MORE -> moreLayId
            VIEW_TYPE_EMPTY_LIST -> noOtherProducts
            VIEW_TYPE_PRODUCT_SIMPLE -> productSimple
            VIEW_TYPE_PROGRESS -> progressLayId
            VIEW_TYPE_SIMPLE_TEXT -> R.layout.comment_single_text_view
            else -> throw Throwable("Home list item type is invalid")
        }
    }
}



