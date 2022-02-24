package com.example.market.home
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.market.*
import com.example.market.databinding.FragmentHomeBinding
import com.example.market.models.Product
import com.example.market.navigation.bottomNavVisiblity
import com.example.market.products.NotificationCenter
import com.example.market.recycler.EndlessRecyclerViewScrollListener
import com.example.market.recycler.RecyclerItemClickListener
import com.example.market.search.SearchFragment
import com.example.market.utils.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HomeFragment : BaseFragment<FragmentHomeBinding>(R.layout.fragment_home),NotificationCenter.NotificationDelegate, RecyclerItemClickListener {
    private var recyclerViewAdapter: HomeListAdapter?=null
    private var layoutManagerState: Parcelable?=null
    private var layManager: StaggeredGridLayoutManager?=null
    private var scrollListener: EndlessRecyclerViewScrollListener?=null

    private lateinit var productsPaging: FirestorePaging<Product>

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerViewAdapter = null
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

    override fun onClick(position: Int, type: Int) {
        when (type) {
            R.layout.home_product_item -> {
                recyclerViewAdapter?.currentList?.getOrNull(position)?.let {
                    presentFragmentRemoveLast(DetailsFragment(it), removeLast = false)
                }
            }
            R.layout.home_main_category_item -> {
                //fill
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
        binding: FragmentHomeBinding,
    ) {
        bottomNavVisiblity(context,true)

        binding.apply {
            homeSearchLayout.setOnClickListener { presentFragmentRemoveLast(
                SearchFragment(),
                removeLast = false
            ) }

            homeRecyclerView.apply {
                recyclerViewAdapter = HomeListAdapter().apply {
                    adapter = this
                    setOnItemClickListener(this@HomeFragment)
                }
                layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL).also {
                    layManager = it
                }
                addItemDecoration(HomeListDecoration)

                scrollListener = object : EndlessRecyclerViewScrollListener(layManager) {
                    init { addOnScrollListener(this) }

                    override fun onLoadMore(
                        page: Int,
                        totalItemsCount: Int,
                        view: RecyclerView?,
                    ) {
                        productsPaging.loadMore()
                    }
                }
            }
            productsPaging.observe(viewLifecycleOwner) {
                it?.let { recyclerViewAdapter?.submitList(it.toMutableList()) }
            }
        }
    }
}