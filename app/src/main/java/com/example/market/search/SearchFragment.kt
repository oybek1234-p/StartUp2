package com.example.market.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.market.*
import com.example.market.binding.inflateBinding
import com.example.market.databinding.EmptyScreenBinding
import com.example.market.databinding.FragmentSearchBinding
import com.example.market.databinding.SearchItemLayoutBinding
import com.example.market.navigation.bottomNavVisiblity
import com.example.market.recycler.RecyclerItemClickListener
import com.example.market.recycler.databoundrecycler.DataBoundAdapter
import com.example.market.recycler.databoundrecycler.DataBoundViewHolder
import com.example.market.utils.AndroidUtilities
import com.example.market.viewUtils.CubicBezierInterpolator
import com.example.market.viewUtils.toast
import java.util.*
import kotlin.collections.ArrayList

class SearchFragment(private var searchText: String?=null) : BaseFragment() {
    private val SEARCH_STATE_LOADING = 0
    private val SEARCH_STATE_LOADED = 1
    private val SEARCH_STATE_EMPTY = 2

    companion object {
        const val EMPTY_TYPE = "empty"
    }

    private var searchState = SEARCH_STATE_LOADING

    private var binding: FragmentSearchBinding?=null
    private var searchAdapter: SearchAdapter?=null
    private var searchList: ArrayList<SearchProduct>?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = inflateBinding(container,R.layout.fragment_search)

        binding?.apply {
            backButton.setOnClickListener {
                closeLastFragment()
            }
            searchButton.setOnClickListener {
                if (searchEditText.text!=null&&searchEditText.text!!.isNotEmpty()&&searchText!=null) {
                    presentFragmentRemoveLast(SearchResultsFragment(searchText!!),false)
                }
            }
            searchEditText.doOnTextChanged { text, start, before, count ->
                searchText = text?.toString()
                doSearch()
            }
            recyclerView.apply {
                itemAnimator = null
                adapter = SearchAdapter().also {
                    searchAdapter = it
                    it.setClickListener(object : RecyclerItemClickListener{
                        override fun onClick(position: Int, type: Int) {
                            val item = it.dataList?.getOrNull(position)
                            if (item!=null) {
                                presentFragmentRemoveLast(SearchResultsFragment(item.title),false)
                            }
                        }
                    })
                }
                layoutManager = LinearLayoutManager(requireContext())
                searchAdapter?.setDataList(searchList)
            }
        }
        return binding?.root
    }

    override fun onViewAttachedToParent() {
        super.onViewAttachedToParent()
        binding?.searchEditText?.let {
            getMainActivity().showKeyboard(it)
        }
    }
    private var emptyModel = SearchProduct().apply {
        id = System.currentTimeMillis().toString()
        title = EMPTY_TYPE
    }
    fun updateState(state: Int){
        searchState = state
        toast("State $state")
        when(searchState) {
            SEARCH_STATE_LOADING -> {
                searchList = null
                searchAdapter?.setDataList(null)
                showProgress(true)
            }
            SEARCH_STATE_LOADED -> {
                showProgress(false)
                if (searchText==null||searchText!=null&&searchText!!.isEmpty()){
                    updateState(SEARCH_STATE_EMPTY)
                }
                searchAdapter?.setDataList(searchList)
            }
            SEARCH_STATE_EMPTY -> {
                showProgress(false)
                if (searchText==null||searchText!=null&&searchText!!.isEmpty()) {
                    searchList = null
                    searchAdapter?.setDataList(null)
                    return
                }
                if (searchList==null){
                    searchList = ArrayList()
                }
                searchList?.apply {
                    clear()
                    add(emptyModel)
                    searchAdapter?.setDataList(this)
                }
            }
        }
    }
    private var searchTimer: Timer?=null
    private var searchRunnable: Runnable?=null
    private var searchReqId = 0

    fun doSearch() {
        if (searchText!=null&&searchText!!.isNotEmpty()) {
            searchText?.let {
                searchTimer?.cancel()
                searchTimer = null
                cancellRequest(searchReqId)

                updateState(SEARCH_STATE_LOADING)
                searchTimer = Timer().apply {
                    schedule(object : TimerTask() {
                        override fun run() {
                            searchReqId =  searchProducts(it,object : ResultCallback<ArrayList<SearchProduct>> {
                                override fun onSuccess(result: ArrayList<SearchProduct>?) {
                                    if (result!=null&&result.isNotEmpty()) {
                                        searchList = result
                                        updateState(SEARCH_STATE_LOADED)
                                    }else{
                                        setSearchProduct(System.currentTimeMillis().toString(),it,null)
                                        updateState(SEARCH_STATE_EMPTY)
                                    }
                                }
                                override fun onFailed() {
                                    super.onFailed()
                                    setSearchProduct(System.currentTimeMillis().toString(),it,null)
                                    updateState(SEARCH_STATE_EMPTY)
                                }
                            })

                        }
                    },300)
                }
            }
        } else {
            updateState(SEARCH_STATE_EMPTY)
        }
    }

    fun showProgress(show: Boolean) {
//        binding?.shimmerLayout?.apply {
//            if (show) {
//                showShimmer(true)
//            } else {
//                hideShimmer()
//            }
//        }
    }

 class SearchAdapter: DataBoundAdapter<ViewDataBinding,SearchProduct>(R.layout.search_item_layout) {
     override fun onCreateViewHolder(
         viewHolder: DataBoundViewHolder<ViewDataBinding>?,
         viewType: Int,
     ) {

     }

     override fun getItemLayoutId(position: Int): Int {
         return if (dataList!=null&&dataList[position].title == EMPTY_TYPE)
             R.layout.empty_screen
         else
             super.getItemLayoutId(position)
     }

     override fun bindItem(
         holder: DataBoundViewHolder<ViewDataBinding>?,
         position: Int,
         model: SearchProduct?,
     ) {
         holder?.binding?.apply {
             if (this is EmptyScreenBinding) {
                 lottieView.setAnimation(R.raw.no_result_lottie)
                 lottieView.playAnimation()

                 titleView.text = "No results found"
                 subtitleView.text = "Please check your text and make sure it is right"
                 addItemButton.visibility = View.GONE

                 root.apply {
                     scaleX = 0.8f
                     scaleY = 0.8f
                     alpha = 0f

                     animate().scaleY(1f).scaleX(1f).setDuration(150).alpha(1f).setInterpolator(CubicBezierInterpolator.DEFAULT).start()
                 }
             }
         }
     }


 }
}