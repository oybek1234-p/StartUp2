package com.example.market.categories

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.market.*
import com.example.market.binding.inflateBinding
import com.example.market.binding.load
import com.example.market.databinding.CategoryItemBinding
import com.example.market.databinding.FragmentCategoriesBinding
import com.example.market.navigation.bottomNavVisiblity
import com.example.market.recycler.RecyclerItemClickListener
import com.example.market.recycler.databoundrecycler.DataBoundAdapter
import com.example.market.recycler.databoundrecycler.DataBoundViewHolder
import com.example.market.utils.AndroidUtilities
import com.example.market.viewUtils.PopupDialog
import com.example.market.viewUtils.PopupWindowLayout
import com.example.market.viewUtils.anticipateInterpolator
import com.example.market.viewUtils.setFlickerView

class CategoriesFragment : BaseFragment() {
    private var binding: FragmentCategoriesBinding?=null
    var mainCategoriesList = ArrayList<Category>()
    var subCategoryList = ArrayList<Category>()
    var mainCategoryReqId = -1
    var subCategoryReqId = -1
    private var mainCategoryAdapter: MainCategoryAdapter ?= null
    private var subCategoryAdapter: SubCategoryAdapter ?= null
    private var progress = false
    private var loadingMainCategoryList = false
    private var loadingSubCategoryList = false
    private var isEmptySubList = false

    companion object {
        const val TAG = "CategoriesFragment"
        const val LOADING = "loading"
    }

    fun showProgressView(show: Boolean) {
        if (show == progress) {
            return
        }
        progress = show
        binding?.subProgressBar?.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun setEmptyView(empty: Boolean) {
        if (empty == isEmptySubList) {
            return
        }
        isEmptySubList = empty
        binding?.emptyScreen?.root?.visibility = if (empty) View.VISIBLE else View.GONE
    }

    @SuppressLint("NotifyDataSetChanged")
    fun showMainCategoryProgressView(show: Boolean) {
        binding?.mainShimmerLayout?.apply {
            if (show) {
                mainCategoriesList.clear()
                val progressModel = Category().apply { id = LOADING }
                for (i in 0..6) {
                    mainCategoriesList.add(progressModel)
                }
                showShimmer(true)
            } else {
                mainCategoriesList.clear()
                hideShimmer()
            }
            mainCategoryAdapter?.notifyDataSetChanged()
        }
    }

    private var mainListResultListener = object : ResultCallback<List<Category>?> {
        @SuppressLint("NotifyDataSetChanged")
        override fun onSuccess(result: List<Category>?) {
            showMainCategoryProgressView(false)
            loadingMainCategoryList = false
            if (result != null && result.isNotEmpty()) {
                mainCategoriesList.apply {
                    clear()
                    addAll(result)
                    mainCategoryAdapter?.apply {
                        notifyDataSetChanged()
                        setSelected(0)
                    }
                }
            }
        }
    }

    private var subListResultCallback = object : ResultCallback<List<Category>?> {
        @SuppressLint("NotifyDataSetChanged")
        override fun onSuccess(result: List<Category>?) {
            showProgressView(false)
            loadingSubCategoryList = false
            if (result != null && result.isNotEmpty()) {
                subCategoryList.apply {
                    clear()
                    addAll(result)
                }
                subCategoryAdapter?.notifyDataSetChanged()
            } else {
                setEmptyView(true)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun loadSubList(categoryId: String) {
        subCategoryList.clear()
        subCategoryAdapter?.notifyDataSetChanged()
        setEmptyView(false)
        showProgressView(true)
        loadingSubCategoryList = true
        cancellRequest(subCategoryReqId)
        subCategoryReqId = getSubCategories(categoryId,subListResultCallback)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun loadMainList() {
        mainCategoriesList.clear()
        subCategoryList.clear()

        mainCategoryAdapter?.notifyDataSetChanged()
        subCategoryAdapter?.notifyDataSetChanged()

        showMainCategoryProgressView(true)
        setEmptyView(false)
        cancellRequest(mainCategoryReqId)
        loadingMainCategoryList = true
        mainCategoryReqId = getMainCategories(mainListResultListener)
    }

    private fun openAddCategoryFragment(main: Boolean) {
        var parentCategoryId = ""
        if (!main) { mainCategoryAdapter?.getSelectedCategory()?.id?.let { parentCategoryId = it } }

        AddNewCategoryFragment(newCategory = {
            categories?.add(it)
            if (parentCategoryId.isNotEmpty()) {
                mainCategoriesList.add(it)
                val size = mainCategoriesList.size
                mainCategoryAdapter?.notifyItemInserted(size)
                AndroidUtilities.runOnUIThread({
                    binding?.mainRecyclerView?.smoothScrollToPosition(size)
                },200)
            } else {
                subCategoryList.add(it)
                subCategoryAdapter?.notifyItemInserted(subCategoryList.size)
            }
        }).apply {
            category.parentId = parentCategoryId
            presentFragmentRemoveLast(this,false)
        }
    }

    override fun onViewAttachedToParent() {
        super.onViewAttachedToParent()
        bottomNavVisiblity(requireContext(),false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflateBinding(container,R.layout.fragment_categories)

        binding?.apply {
            actionBar.apply {
                backButton.setOnClickListener { closeLastFragment() }
                titleContainer.gravity = Gravity.START
                title.text = getString(R.string.string_catgs)
                options.setOnClickListener {
                    PopupWindowLayout(requireContext()).apply {
                        addItem(0,"Add category",R.drawable.msg_addbot) { openAddCategoryFragment(true) }
                        addItem(1,"Add sub category",R.drawable.msg_addfolder) { openAddCategoryFragment(false) }
                        PopupDialog(this).show(
                            requireView(),
                            0,
                            MyApplication.displaySize.first - measuredWidth,
                            AndroidUtilities.dp(8f),
                            true)
                    }
                }
            }
            subRecyclerView.apply {
                adapter = SubCategoryAdapter().also { subCategoryAdapter = it }.apply {
                    setClickListener(object : RecyclerItemClickListener {
                        override fun onClick(position: Int, type: Int) {
                            subCategoryList.getOrNull(position)?.let {
                                presentFragmentRemoveLast(SearchResultsFragment(it.name),false)
                            }
                        }
                    })
                }
            }
            mainRecyclerView.adapter = MainCategoryAdapter { categoryId, _, _ -> loadSubList(categoryId) }.also { mainCategoryAdapter = it }
            loadMainList()
        }
        return binding?.root
    }

    inner class SubCategoryAdapter : DataBoundAdapter<CategoryItemBinding,Category>(R.layout.category_item) {
        init {
            automaticallySetData = false
            dataList = subCategoryList
        }
        override fun bindItem(
            holder: DataBoundViewHolder<CategoryItemBinding>?,
            position: Int,
            model: Category
        ) {
            holder?.binding?.apply {
                model.apply {
                    photoView.load(photo, circleCrop = true, fade = true)
                    nameView.text = name
                    productCountView.text = productsCount.toString()
                }
            }
        }

        override fun onCreateViewHolder(
            viewHolder: DataBoundViewHolder<CategoryItemBinding>?,
            viewType: Int,
        ) {

        }
    }

    inner class MainCategoryAdapter(val onItemSelected: (categoryId: String,binding: CategoryItemBinding?,pos: Int) -> Unit) : DataBoundAdapter<CategoryItemBinding,Category>(R.layout.category_item) {
        init {
            automaticallySetData = false
            dataList = mainCategoriesList
        }
        var selectedItem = -1
        var lastSelectedItem = -1

        private val selectedColor = Color.LTGRAY
        private val unSelectedColor = Color.WHITE

        fun setSelected(categoryId: String) {
            val positon = findCategoryPosition(categoryId)
            if (positon != -1) {
                setSelected(positon)
            } else {
                throw Exception("Selected position is -1")
            }
        }

        fun getSelectedCategory() = dataList.getOrNull(selectedItem)

        fun findCategoryPosition(categoryId: String): Int {
            var position = -1
            dataList?.forEachIndexed { index, category ->
                if (category.id == categoryId) {
                    position = index
                }
            }
            return position
        }

        fun setSelected(position: Int) {
            selectedItem = position
            if (lastSelectedItem == selectedItem) {
                return
            }
            var binding: CategoryItemBinding ?= null
            mRecyclerView?.apply {
                findViewHolderForLayoutPosition(lastSelectedItem)?.itemView?.setBackgroundColor(unSelectedColor)

                findViewHolderForAdapterPosition(selectedItem)?.apply {
                    if (this is DataBoundViewHolder<*>) {
                        binding = binding as CategoryItemBinding
                    }
                    itemView.setBackgroundColor(selectedColor)
                }
            }
            val itemId = dataList[position].id
            onItemSelected(itemId,binding,selectedItem)
            lastSelectedItem = selectedItem
        }

        override fun bindItem(
            holder: DataBoundViewHolder<CategoryItemBinding>?,
            position: Int,
            model: Category
        ) {
            holder?.binding?.apply {
                val loading = model.id == LOADING
                photoView.apply {
                    if (loading) {
                        setImageDrawable(null)
                        setFlickerView()
                    } else {
                        load(model.photo, circleCrop = true)
                    }
                }
                nameView.apply {
                    text = if (loading) {
                        setFlickerView()
                        "****"
                    } else {
                        model.name
                    }
                }
                productCountView.apply {
                    text = if (loading) {
                        setFlickerView()
                        "***"
                    } else {
                        model.name
                    }
                }
                root.apply {
                    setBackgroundColor(if (selectedItem == position) selectedColor else unSelectedColor)
                    translationY = -50f
                    animate().translationY(0f).setDuration(300).setInterpolator(
                        anticipateInterpolator).start()
                }
            }
        }

        override fun onCreateViewHolder(
            viewHolder: DataBoundViewHolder<CategoryItemBinding>?,
            viewType: Int
        ) {
            viewHolder?.apply {
                itemView.setOnClickListener {
                    setSelected(layoutPosition)
                }
            }
        }
    }
}

