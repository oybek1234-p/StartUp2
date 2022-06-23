package com.org.ui

import android.graphics.Rect
import android.view.View
import android.widget.Toast
import androidx.core.view.doOnNextLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.market.R
import com.example.market.databinding.CategoryItemBinding
import com.example.market.databinding.FragmentCategoriesBinding
import com.org.market.DataController
import com.org.market.ResultCallback
import com.org.market.dp
import com.org.net.models.Category
import com.org.net.models.Empty
import com.org.ui.actionbar.BaseFragment
import com.org.ui.actionbar.PopupDialog
import com.org.ui.actionbar.PopupWindowLayout
import com.org.ui.components.AlertsCreator
import com.org.ui.components.RecyclerListView
import com.org.ui.components.load
import com.org.ui.components.visibleOrGone

class CategoriesFragment(val onCategorySelected: ((category: Category)->Unit) ?= null) : BaseFragment<FragmentCategoriesBinding>(R.layout.fragment_categories) {

    private var parentAdapter: CategoriesAdapter? = null
    private var subAdapter: CategoriesAdapter? = null
    private var parentList = arrayListOf<Category>()
    private var subList = arrayListOf<Category>()
    private var currentSelectedCat = ""

    fun setSelectedCategory(position: Int) {
        subCatEmpty = false
        val category = parentList[position]
        val cat = parentList[position].id
        currentSelectedCat = cat
        requireBinding().subCatTitle.text = category.name
        subCatLoading = true
        subList.clear()
        subAdapter?.submitList(null)
        val cache = DataController.getSubCategories(cat)
        if (cache?.isNotEmpty() == true) {
            subCatLoading = false
            subList.addAll(cache)
            updateSubList(true)
        } else {
            DataController.loadSubCategories(cat, subListCallback)
        }
    }

    private var ignoreParentAdd = true

    override fun onResume() {
        super.onResume()
        if (ignoreParentAdd) {
            ignoreParentAdd = false
            return
        }
        parentList = DataController.categories
        updateParentList(!ignoreParentAdd)
        if (currentSelectedCat.isNotEmpty()) {
            val list = DataController.getSubCategories(currentSelectedCat)
            if (list != null) {
                subList.clear()
                subList.addAll(list)
                val empty = subList.isEmpty()
                subCatEmpty = empty
                updateSubList(!empty)
            }
        }
    }

    private var subListCallback = object : ResultCallback<ArrayList<Category>>() {
        override fun onSuccess(data: ArrayList<Category>?) {
            subCatLoading = false
            subList.clear()
            if (data != null && data.isNotEmpty()) {
                subList.addAll(data)
                updateSubList(true)
            } else {
                subCatEmpty = true
            }
        }

        override fun onFailed(exception: Exception?) {
            subCatLoading = false
        }
    }

    private var parentListCallback = object : ResultCallback<ArrayList<Category>>() {
        override fun onSuccess(data: ArrayList<Category>?) {
            parentCatLoading = false
            parentList.clear()
            if (data != null) {
                parentList = data
            }
            updateParentList(true)
        }

        override fun onFailed(exception: Exception?) {
            parentCatLoading = false
            toast("Error")
            throw exception!!
        }
    }

    fun updateParentList(add: Boolean) {
        parentAdapter?.submitList(parentList.toMutableList().apply { if (add) add(addCatModel) })
    }

    fun updateSubList(add: Boolean) =
        subAdapter?.submitList(subList.toMutableList().apply { if (add) add(addCatModel) })

    val addCatModel = Category().apply {
        id = "Add"
        photo = "https://cdn.pixabay.com/photo/2017/04/20/07/07/add-2244771_960_720.png"
        name = "Add category"
        productsCount = 1
    }

    fun loadCategories() {
        if (parentCatLoading) return
        parentCatLoading = true
        val cached = DataController.categories
        if (cached.isNotEmpty()) {
            parentCatLoading = false
            parentList = cached
            updateParentList(true)
            toast("From cache")
        } else {
            DataController.loadParentCategories(parentListCallback)
            toast("From network")
        }
    }

    fun toast(m: String) {
        Toast.makeText(requireActivity(), m, Toast.LENGTH_LONG).show()
    }

    private var parentCatLoading = false
        set(value) {
            if (value != field) {
                field = value
                requireBinding().mainShimmerLayout.apply {
                    if (value) {
                        showShimmer(true)
                    } else {
                        hideShimmer()
                    }
                }
            }
        }

    private var subCatLoading = false
        set(value) {
            if (value != field) {
                field = value
                requireBinding().subProgressBar.visibleOrGone(value)
            }
        }

    private var subCatEmpty = false
        set(value) {
            if (value != field) {
                field = value
                if (mBinding != null)
                    mBinding!!.emptyScreen.root.visibleOrGone(value)
            }
        }

    fun showModifyAlert(category: Category, position: Int) {
        val isParentList = category.parentId == ""
        val popupWindow = PopupWindowLayout(requireActivity()).apply {
            addItem(0, "Edit", R.drawable.msg_edit) {
                presentFragment(AddCategoryFragment(category), false)
            }
            addItem(1, "Delete", R.drawable.msg_delete) {
                AlertsCreator.showAlert(
                    requireActivity(),
                    "Delete category",
                    "Do you really want to delete ${category.name} category",
                    "CANCEL",
                    "DELETE",
                    {},
                    {
                        if (isParentList) {
                            parentList.remove(category)
                            DataController.categories.remove(category)
                            updateParentList(true)
                        } else {
                            subList.remove(category)
                            DataController.subCategories[category.parentId]?.remove(category)
                            updateSubList(true)
                        }
                        DataController.deleteCategory(category.id)
                    }, category.photo
                )
            }
        }
        val view = if (isParentList) {
            requireBinding().mainRecyclerView.findViewHolderForAdapterPosition(position)?.itemView
        } else {
            requireBinding().subRecyclerView.findViewHolderForAdapterPosition(position)?.itemView
        }
        if (view != null) {
            PopupDialog(popupWindow).show(view, 0.2f)
        }
    }

    override fun onCreateView(binding: FragmentCategoriesBinding) {
        actionBar.title = "Categories"

        binding.apply {
            mainRecyclerView.apply {
                adapter = CategoriesAdapter(true, { it ->
                    if (it == parentList.size) {
                        presentFragment(AddCategoryFragment(onAdded = { cat ->
                            requireBinding().mainRecyclerView.doOnNextLayout {
                                parentAdapter?.setSelected(parentList.indexOf(cat))
                            }
                        }), false)
                    } else {
                        setSelectedCategory(it)
                    }
                }, { c, p ->
                    showModifyAlert(c, p)
                },false).also {
                    parentAdapter = it
                }
                layoutManager =
                    LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
            }
            subRecyclerView.apply {
                adapter = CategoriesAdapter(false, onLongClick = { c, p ->
                    showModifyAlert(c, p)
                }, onSelected = {
                    if (onCategorySelected!=null) {
                        val cat = subList.getOrNull(it)
                        if (cat!=null) {
                            onCategorySelected.invoke(cat)
                        }
                        closeLastFragment()
                    } else {
                        presentFragment(SearchResultFragment(),false)
                    }
                }, sub = true).apply {
                    subAdapter = this
                    setOnItemClickListener(object : RecyclerListView.RecyclerItemClickListener {
                        override fun onClick(position: Int, viewType: Int) {
                            if (position == subList.size) {
                                presentFragment(AddCategoryFragment().apply {
                                    category.parentId = subList.first().parentId
                                }, false)
                            } else {
                                //Present search results fragment
                            }
                        }
                    })
                    layoutManager = GridLayoutManager(context, 4)
                    val padding = dp(4f)
                    addItemDecoration(object : RecyclerView.ItemDecoration() {
                        override fun getItemOffsets(
                            outRect: Rect,
                            view: View,
                            parent: RecyclerView,
                            state: RecyclerView.State,
                        ) {
                            when ((view.layoutParams as GridLayoutManager.LayoutParams).spanIndex) {
                                0 -> {
                                    outRect.left = padding
                                }
                                1 -> {
                                    outRect.left = padding
                                }
                                2 -> {
                                    outRect.left = padding
                                    outRect.right = padding
                                }
                            }
                            outRect.top = padding
                        }
                    })
                }
            }
            emptyScreen.apply {
                data = Empty("Category is empty",
                    "Tap to create a new sub category",
                    "Add subcategory",
                    {
                        val catId = parentList[parentAdapter!!.selectedCategoryPos].id
                        presentFragment(AddCategoryFragment().apply { category.parentId = catId },
                            false)
                    },
                    lottieUrl = "https://assets4.lottiefiles.com/packages/lf20_epqmtf3b.json")
                executePendingBindings()
            }
            loadCategories()
        }
    }

    class CategoriesAdapter(
        var selectable: Boolean = true,
        val onSelected: (pos: Int) -> Unit = {},
        val onLongClick: ((category: Category, pos: Int) -> Unit)? = null,val sub: Boolean
    ) : RecyclerListView.Adapter<CategoryItemBinding, Category>(R.layout.category_item,
        object : DiffUtil.ItemCallback<Category>() {
            override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
                return oldItem.photo == newItem.photo && oldItem.name == newItem.name && oldItem.options == newItem.options && oldItem.productsCount == newItem.productsCount
            }

            override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
                return oldItem.id == newItem.id
            }
        }) {
        var selectedCategoryPos = -1
        var lastSelectedCategoryPos = -1

        override fun submitList(list: MutableList<Category>?) {
            if (!list.isNullOrEmpty() && selectedCategoryPos == -1 && !sub) {
                selectedCategoryPos = 0
                lastSelectedCategoryPos = 0
                onSelected(0)
            }
            super.submitList(list)
        }

        fun clear() {
            submitList(null)
            selectedCategoryPos = -1
            lastSelectedCategoryPos = -1
        }

        override fun bind(
            holder: RecyclerListView.BaseViewHolder<CategoryItemBinding>,
            position: Int,
            model: Category,
        ) {
            holder.apply {
                if (selectable) {
                    val selected = position == selectedCategoryPos
                    binding.isSelected = selected
                }
                binding.apply {
                    photoView.load(model.photo, circleCrop = true)
                    nameView.text = model.name
                    productCountView.text = model.productsCount.toString()
                    executePendingBindings()
                }
            }
        }

        fun setSelected(p: Int) {
            lastSelectedCategoryPos = selectedCategoryPos
            selectedCategoryPos = p
            if (lastSelectedCategoryPos == selectedCategoryPos) {
                return
            }
            onSelected(selectedCategoryPos)
            updateSelected()
        }

        fun updateSelected() {
            val lastItem =
                (mRecyclerView!!.findViewHolderForLayoutPosition(lastSelectedCategoryPos) as RecyclerListView.BaseViewHolder<CategoryItemBinding>?)?.binding
            val selectedItem =
                (mRecyclerView!!.findViewHolderForLayoutPosition(selectedCategoryPos) as RecyclerListView.BaseViewHolder<CategoryItemBinding>?)?.binding

            lastItem?.apply {
                isSelected = false
                executePendingBindings()
            }
            selectedItem?.apply {
                isSelected = true
                executePendingBindings()
            }
        }

        override fun onViewHolderCreated(
            holder: RecyclerListView.BaseViewHolder<CategoryItemBinding>,
            type: Int,
        ) {
            holder.apply {

                    itemView.setOnClickListener {
                        if (selectable) {
                            val cat = currentList[layoutPosition].id
                            if (cat == "Add") {
                                onSelected(layoutPosition)
                                return@setOnClickListener
                            }
                        }
                        setSelected(layoutPosition)
                    }

                itemView.setOnLongClickListener {
                    val cat = currentList[layoutPosition]
                    if (cat.id == "Add") {
                        return@setOnLongClickListener false
                    }
                    onLongClick?.invoke(cat, layoutPosition)
                    return@setOnLongClickListener true
                }
            }
        }
    }
}