package com.example.market.home
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.market.*
import com.example.market.categories.AddNewCategoryFragment
import com.example.market.categories.CategoriesFragment
import com.example.market.model.Product
import com.example.market.navigation.bottomNavVisiblity
import com.example.market.products.NotificationCenter
import com.example.market.recycler.EndlessRecyclerViewScrollListener
import com.example.market.recycler.RecyclerItemClickListener
import com.example.market.search.SearchFragment
import com.example.market.utils.*
import com.example.market.viewUtils.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HomeFragment : BaseFragment(),NotificationCenter.NotificationDelegate, RecyclerItemClickListener {

    var recyclerView: RecyclerView? = null
    private var searchView: FrameLayout?=null
    private var recyclerViewAdapter: HomeListAdapter?=null
    private var layoutManagerState: Parcelable?=null
    private var layManager: StaggeredGridLayoutManager?=null
    private var scrollListener: EndlessRecyclerViewScrollListener?=null

    private lateinit var productsPaging: FirestorePaging<Product>

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerViewAdapter = null
        recyclerView = null
        searchView=null
        layoutManagerState=null
        layManager =null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

            val query = FirebaseFirestore
                .getInstance()
                .collection(PRODUCTS)
                .orderBy("id", Query.Direction.DESCENDING)

        productsPaging = FirestorePaging(Product::class.java,query,"id")
        productsPaging.list = products
    }

    override fun didRecieveNotification(id: String, any: Any?) {

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        val view =  inflater.inflate(R.layout.fragment_home, container, false).apply {
            searchView = findViewById(R.id.home_search_layout)
            recyclerView = findViewById(R.id.home_recycler_view)

        }

        bottomNavVisiblity(context,true)

        productsPaging.pagingCallback = object : FirestorePaging.PagingCallback {
            override fun onFinishedLoadMore() {
                recyclerViewAdapter?.loading = false
            }

            override fun onLoadMore() {
                recyclerViewAdapter?.loading = true
            }
        }

        searchView?.setOnClickListener {
            presentFragmentRemoveLast(
                SearchFragment(),
                removeLast = false
            )
        }

        layManager = StaggeredGridLayoutManager(
            2,
            StaggeredGridLayoutManager.VERTICAL
        )

        recyclerView?.apply {
            recyclerViewAdapter = HomeListAdapter().apply {
                setOnItemClickListener(this@HomeFragment)
                adapter = this
            }
            layoutManager = layManager
            addItemDecoration(HomeListDecoration)

            scrollListener = object : EndlessRecyclerViewScrollListener(layManager) {
                init {
                    addOnScrollListener(
                        this
                    )
                }

                override fun onLoadMore(
                    page: Int,
                    totalItemsCount: Int,
                    view: RecyclerView?,
                ) {
                    productsPaging.loadMore()
                }
            }
        }
        productsPaging.observe(viewLifecycleOwner,{
            it?.let {
                toast("Submit list")
                recyclerViewAdapter?.submitList(it.toMutableList())
            }

        })
        presentFragmentRemoveLast(CategoriesFragment(),false)
        return view
    }

    override fun onClick(position: Int, type: Int) {
        when (type) {
            R.layout.home_product_item -> {
                recyclerViewAdapter?.apply {
                    currentList[position]?.let {
                        presentFragmentRemoveLast(DetailsFragment(it), removeLast = false)
                    }
                }
            }
            R.layout.home_main_category_item -> {

            }
        }
    }

}