package com.example.market
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.RecyclerView
//import androidx.recyclerview.widget.StaggeredGridLayoutManager
//import com.example.market.databinding.FragmentSearchResultsBinding
//import com.example.market.home.HomeListAdapter
//import com.example.market.home.HomeListDecoration
//import com.example.market.models.Product
//import com.example.market.navigation.bottomNavVisiblity
//import com.google.firebase.firestore.Query
//
//class Filter {
//    var sortBy = FILTER_VIEWS_COUNT
//    var priceRange: PriceRange?=null
//    var brand = ""
//    var freeShipping = false
//    var category = ""
//    var direction = Query.Direction.DESCENDING
//}
//
//class PriceRange {
//    var maxPrice = 0L
//    var minPrice = 0L
//}
//
//const val FILTER_SOLD_COUNT = 0
//const val FILTER_VIEWS_COUNT = 1
//const val FILTER_DATE = 2
//const val FILTER_NARXI = 3
//
//class SearchResultsFragment(private var searchText: String) : BaseFragment<FragmentSearchResultsBinding>(R.layout.fragment_search_results) {
//    private var list: ArrayList<Product> = ArrayList()
//    private var loading = false
//    private var filter: Filter = Filter()
//    private var query: Query?=null
//    private var listAdapter: HomeListAdapter?=null
//
//    fun getOrderByType(filter: Filter) = when(filter.sortBy){
//        FILTER_DATE -> "date"
//        FILTER_NARXI -> "narxi"
//        FILTER_SOLD_COUNT -> "soldCount"
//        FILTER_VIEWS_COUNT -> "viewsCount"
//        else -> ""
//    }
//
//    fun notifyAdapter() {
//        listAdapter?.submitList(list.toMutableList())
//    }
//
//
//    override fun onViewAttachedToParent() {
//        super.onViewAttachedToParent()
//        bottomNavVisiblity(context,false)
//        getMainActivity().closeKeyboard()
//        load(false)
//    }
//
//    fun load(reload: Boolean = false) {
//        if (reload) {
//            list.clear()
//            listAdapter?.submitList(null)
//        }
//        listAdapter?.loading = true
//        loading = true
//        val startAt = list.lastOrNull()
//        val hasPriceRange = filter.priceRange!=null
//        query = getProductsReference()
//        var query = query!!
//        val filterBy = getOrderByType(filter)
//        query = query.orderBy("title")
//        query = query.orderBy(filterBy,filter.direction)
//        query = query.startAt(searchText)
//        query = query.endAt(searchText + "\uF8FF")
//
//        if (hasPriceRange) {
//            filter.priceRange!!.apply {
//                if (minPrice!=-1L) {
//                    query = query.whereGreaterThanOrEqualTo("narxi",minPrice)
//                }
//                if (maxPrice!=-1L) {
//                    query = query.whereLessThanOrEqualTo("narxi",maxPrice)
//                }
//            }
//        }
//        if (filter.freeShipping) {
//            query = query.whereEqualTo("freeShipping",true)
//        }
//        if (filter.brand.isNotEmpty()) {
//            query = query.whereEqualTo("brand",filter.brand)
//        }
//        if (startAt!=null&&!reload) {
//            query = query.startAfter(startAt.date)
//        }
//        query = query.limit(6)
//        query.get().addOnCompleteListener {
//            loading = false
//            listAdapter?.loading = false
//            if (it.isSuccessful&&it.result!=null&&it.result.documents.isNotEmpty()){
//                val newList = parseDocumentSnapshot(it.result.documents,Product::class.java)
//                list.addAll(newList)
//                notifyAdapter()
//            } else {
//                listAdapter?.setEmpty()
//            }
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?,
//        binding: FragmentSearchResultsBinding,
//    ) {
//        binding.apply {
//            backButton.setOnClickListener {
//                closeLastFragment()
//            }
//            searchEditText.text.append(searchText)
//            searchEditText.isFocusable = false
//            searchEditText.setOnClickListener { closeLastFragment() }
//            searchButton.setOnClickListener { load(false) }
//
//            filterButton.setOnClickListener {
//                val filterFragment = FilterProductsFragment(filter) { newFilter->
//                    filter = newFilter
//                    load(true)
//                }
//                filterFragment.show(childFragmentManager,System.currentTimeMillis().toString())
//            }
//
//            recyclerView.apply {
//                adapter = HomeListAdapter().also { listAdapter = it }
//                layoutManager = StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL).also {
//                    addOnScrollListener(object : EndlessRecyclerViewScrollListener(it){
//                        override fun onLoadMore(
//                            page: Int,
//                            totalItemsCount: Int,
//                            view: RecyclerView?
//                        ) { load() }
//                    })
//                }
//                addItemDecoration(HomeListDecoration)
//                notifyAdapter()
//            }
//        }
//    }
//
//}