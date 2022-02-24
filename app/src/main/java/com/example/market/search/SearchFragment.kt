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

class SearchFragment(private var searchText: String = "") : BaseFragment<FragmentSearchBinding>(R.layout.fragment_search) {
    companion object {
        private const val SEARCH_STATE_LOADING = 0
        private const val SEARCH_STATE_LOADED = 1
        private const val SEARCH_STATE_EMPTY = 2
    }
    private var searchState = SEARCH_STATE_LOADING

    private var searchAdapter: SearchAdapter?=null
    private var searchList: ArrayList<SearchProduct>?=null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
        binding: FragmentSearchBinding,
    ) {
        binding.apply {

            backButton.setOnClickListener { closeLastFragment() }

            searchButton.setOnClickListener {
                val text = searchEditText.text
                if (text!=null && text.isNotEmpty() && searchText!=null) {
                    val sText = searchText
                    presentFragmentRemoveLast(SearchResultsFragment(sText),false)
                }
            }

            searchEditText.doOnTextChanged { text, _, _, _ ->
                searchText = text?.toString() ?: ""
                doSearch()
            }

            recyclerView.apply {
                itemAnimator = null
                adapter = SearchAdapter().apply {
                    searchAdapter = this
                    setClickListener(object : RecyclerItemClickListener{
                        override fun onClick(position: Int, type: Int) {
                            dataList.getOrNull(position)?.let {
                                presentFragmentRemoveLast(SearchResultsFragment(it.title),false)
                            }
                        }
                    })
                }
                layoutManager = LinearLayoutManager(requireContext())
                searchAdapter?.setDataList(searchList)
            }
        }
    }

    override fun onViewAttachedToParent() {
        super.onViewAttachedToParent()
        binding.searchEditText.let { getMainActivity().showKeyboard(it) }
    }

    private var emptyModel = SearchProduct().apply {
        id = System.currentTimeMillis().toString()
        title = "EMPTY"
    }

    fun showProgress(show: Boolean) {

    }

    fun updateState(state: Int){
        searchState = state
        when(searchState) {
            SEARCH_STATE_LOADING -> {
                searchList = null
                searchAdapter?.setDataList(null)
            }
            SEARCH_STATE_LOADED -> {
                showProgress(false)
                if (searchText.isEmpty()){
                    updateState(SEARCH_STATE_EMPTY)
                }
                searchAdapter?.setDataList(searchList)
            }
            SEARCH_STATE_EMPTY -> {
                showProgress(false)
                if (searchText.isEmpty()) {
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
    private var searchReqId = 0

    fun doSearch() {
        if (searchText.isNotEmpty()) {
            searchText.let {
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

 class SearchAdapter: DataBoundAdapter<ViewDataBinding,SearchProduct>(R.layout.search_item_layout) {
     override fun getItemLayoutId(position: Int): Int {
         val isEmpty = dataList.getOrNull(position)?.title == SEARCH_STATE_EMPTY.toString()
         return if (isEmpty) R.layout.empty_screen else super.getItemLayoutId(position)
     }

     override fun bindItem(
         holder: DataBoundViewHolder<ViewDataBinding>,
         binding: ViewDataBinding,
         position: Int,
         model: SearchProduct
     ) {
         binding.apply {
             if (this is EmptyScreenBinding) {
                 lottieView.setAnimation(R.raw.no_result_lottie)
                 lottieView.playAnimation()

                 titleView.text = MyApplication.appContext.getString(R.string.no_results)
                 subtitleView.text = MyApplication.appContext.getString(R.string.provide_more_info)
                 addItemButton.visibility = View.GONE

                 root.apply {
                     scaleX = 0.8f
                     scaleY = 0.8f
                     alpha = 0f
                     animate()
                         .scaleY(1f)
                         .scaleX(1f)
                         .setDuration(150)
                         .alpha(1f)
                         .setInterpolator(CubicBezierInterpolator.DEFAULT)
                         .start()
                 }
             }
         }
     }

     override fun onCreateViewHolder(
         viewHolder: DataBoundViewHolder<ViewDataBinding>?,
         viewType: Int,
     ) {}
 }

}