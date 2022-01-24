package com.example.market

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.market.binding.inflateBinding
import com.example.market.binding.load
import com.example.market.databinding.ActionBarBinding
import com.example.market.databinding.FragmentSellerShopBinding
import com.example.market.home.HomeListAdapter
import com.example.market.home.HomeListDecoration
import com.example.market.model.Product
import com.example.market.recycler.EndlessRecyclerViewScrollListener
import com.example.market.utils.FirestorePaging
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

const val SELLER_ID = "sellerId"

class SellerShopFragment : BaseFragment() {

    private var binding: FragmentSellerShopBinding?=null
    private lateinit var recyclerViewAdapter: HomeListAdapter
    private var itemDec: HomeListDecoration?=null
    private var layManager: StaggeredGridLayoutManager?=null
    private var scrollListener: EndlessRecyclerViewScrollListener?=null
    private var sellerInfo: SellerAccount?=null
    private lateinit var firestorePaging: FirestorePaging<Product>
    private fun getNewQuery() = FirebaseFirestore.getInstance().collection(PRODUCTS)
    private lateinit var filter: Filter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = inflateBinding(container,R.layout.fragment_seller_shop)

        bundleAny?.apply {
            if (this is SellerAccount){
                sellerInfo = this
            }
        }

     //   filter = Filter(this)
        binding?.apply {
            (productRatingOptionView.editText as AutoCompleteTextView?)?.apply {
                val items = listOf("Popular", "Most reviews", "New","Narxi oshib borsin","Narxi kamayib borsin")
                val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, items)
                setAdapter(adapter)
                hint = "Popular"
                setOnItemClickListener { parent, view, position, id ->
                    filter.sortBy = position

                }
            }

            imageView11.setOnClickListener {

            }
            acBar.apply {
                icon.apply {
                    visibility = View.VISIBLE
                    load(
                        sellerInfo?.companyPhoto,
                        circleCrop = true
                    )
                }
                title.text = sellerInfo?.companyName
            }

            val collectionReference =
                getNewQuery().whereEqualTo(SELLER_ID, sellerInfo?.id).orderBy(FieldPath.of("id"))

            firestorePaging = FirestorePaging(
                Product::class.java,
                collectionReference,
                "id"
            )

            recylerView.apply {
                recyclerViewAdapter = HomeListAdapter().apply {
                    submitList(null)
                    setRecycledViewPool(holdersPool)
                }
                adapter = recyclerViewAdapter
                layManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                layoutManager = layManager
                itemDec = HomeListDecoration.apply {
                    addItemDecoration(this)
                }
                scrollListener = object : EndlessRecyclerViewScrollListener(layManager) {
                    override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                        firestorePaging.loadMore()
                    }
                }.apply {
                    addOnScrollListener(this)
                }
            }
        }

        firestorePaging.observe(viewLifecycleOwner,
            {
                it?.let {
                    recyclerViewAdapter.submitList(it.toMutableList())
                }
            })

        return binding?.root
    }

    override fun onBeginSlide() {

    }

    override fun isSwapBackEnabled(): Boolean {
       return false
    }

    override fun onConnectionChanged(state: Boolean) {

    }

    override fun onBackPressed() {

    }

    override fun onViewFullyVisible() {

    }

    override fun onViewFullyHiden() {

    }

    override fun onViewAttachedToParent() {

    }

    override fun onViewDetachedFromParent() {

    }

    override fun canBeginSlide(): Boolean {
        return false
    }

}