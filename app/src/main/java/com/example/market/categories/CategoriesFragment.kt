//package com.example.market.categories
////
//import android.annotation.SuppressLint
//import android.graphics.Color
//import android.os.Bundle
//import android.view.Gravity
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.view.animation.AnticipateOvershootInterpolator
//import androidx.core.content.ContentProviderCompat.requireContext
//import androidx.core.view.doOnPreDraw
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.example.market.*
//import com.example.market.databinding.AlertDialogBinding
//import com.example.market.databinding.CategoryItemBinding
//import com.example.market.databinding.FragmentCategoriesBinding
//import com.example.market.navigation.bottomNavVisiblity
//import com.example.market.viewUtils.*
//import com.org.market.ResultCallback
//import com.org.net.models.Category
//import com.org.ui.actionbar.*
//import com.org.ui.components.RecyclerListView
//import com.org.ui.components.inflateBinding
//import com.org.ui.components.load
//
//
//class CategoriesFragment : BaseFragment<FragmentCategoriesBinding>(R.layout.fragment_categories) {
//    var mainCategoriesList = ArrayList<Category>()
//    var subCategoryList = ArrayList<Category>()
//    var mainCategoryReqId = -1
//    var subCategoryReqId = -1
//    private var mainCategoryAdapter: MainCategoryAdapter? = null
//    private var subCategoryAdapter: SubCategoryAdapter? = null
//    private var mProgress = false
//    private var loadingMainCategoryList = false
//    private var loadingSubCategoryList = false
//    private var isEmptySubList = false
//
//    private val binding = mBinding!!
//
//    companion object {
//        const val TAG = "CategoriesFragment"
//        const val LOADING = "loading"
//    }
//
//    fun showProgressView(show: Boolean) {
//        if (show == mProgress) {
//            return
//        }
//        mProgress = show
//        binding.subProgressBar.apply {
//            visibility = View.VISIBLE
//            var mScaleX = 1f
//            var mScaleY = 1f
//            var mAlpha = 1f
//            if (show) {
//                scaleX = 0.8f
//                scaleY = 0.8f
//                alpha = 0f
//            } else {
//                mScaleX = 0.8f
//                mScaleY = 0.8f
//                mAlpha = 0f
//            }
//            animate().scaleX(mScaleX).scaleY(mScaleY)
//                .setInterpolator(AnticipateOvershootInterpolator(2f)).alpha(mAlpha)
//                .setUpdateListener {
//                    if (!show && scaleX == 0.8f) {
//                        visibility = View.GONE
//                    }
//                }.setDuration(300).start()
//        }
//    }
//
//    fun deleteCategory(position: Int, main: Boolean = true): Boolean {
//        mainCategoryAdapter?.apply {
//            if (main && selectedItem != position) {
//                setSelected(position)
//                return true
//            }
//        }
//        (if (main) mainCategoriesList.getOrNull(position) else subCategoryList.getOrNull(position))?.let { category ->
//            val alertView = inflateBinding<AlertDialogBinding>(null, R.layout.alert_dialog, false)
//            alertView.apply {
//                val popupDialog = AlertDialog(requireActivity())
//                val alert = Alert("Delete category",
//                    "Do you really want to delete ${category.name} category ?",
//                    "CANCEL",
//                    "DELETE",
//                    {  },
//                    {
//                        //categories?.remove(category)
//                        if (main) {
//                            try {
//                                mainCategoriesList.removeAt(position)
//                                mainCategoryAdapter?.apply {
//                                    notifyItemRemoved(position)
//                                    setSelected(position, true)
//                                }
//                            } catch (e: Exception) {
//
//                            }
//                        } else {
//                            subCategoryList.removeAt(position)
//                            subCategoryAdapter?.notifyItemRemoved(position)
//                        }
//
//                    },
//                    category.photo)
//                popupDialog.setAlert(alert)
//                popupDialog.show()
//            }
//        }
//        return true
//    }
//
//    fun setEmptyView(empty: Boolean) {
//        if (empty == isEmptySubList) {
//            return
//        }
//        isEmptySubList = empty
//        binding.emptyScreen.root.apply {
//            visibility = View.VISIBLE
//
//            var mScaleX = 1f
//            var mScaleY = 1f
//            var mAlpha = 1f
//            if (empty) {
//                scaleX = 0.8f
//                scaleY = 0.8f
//                alpha = 0f
//            } else {
//                mScaleX = 0.8f
//                mScaleY = 0.8f
//                mAlpha = 0f
//            }
//            animate().scaleY(mScaleY).scaleX(mScaleX).alpha(mAlpha).setUpdateListener {
//                if (!empty && scaleX == 0.8f) {
//                    visibility = View.GONE
//                }
//            }.setInterpolator(AnticipateOvershootInterpolator(2f)).setDuration(300).start()
//        }
//    }
//
//    @SuppressLint("NotifyDataSetChanged")
//    fun showMainCategoryProgressView(show: Boolean) {
//        binding.mainShimmerLayout.apply {
//            if (show) {
//                mainCategoriesList.clear()
//                val progressModel = Category().apply { id = LOADING }
//                for (i in 0..6) {
//                    mainCategoriesList.add(progressModel)
//                }
//                showShimmer(true)
//            } else {
//                mainCategoriesList.clear()
//                mainCategoryAdapter?.notifyDataSetChanged()
//                hideShimmer()
//            }
//        }
//    }
//
//    private var mainListResultListener = object : ResultCallback<List<Category>?>() {
//        @SuppressLint("NotifyDataSetChanged")
//        override fun onSuccess(result: List<Category>?) {
//            showMainCategoryProgressView(false)
//            loadingMainCategoryList = false
//            if (result != null && result.isNotEmpty()) {
//                mainCategoriesList.apply {
//                    clear()
//                    addAll(result)
//                    mainCategoryAdapter?.apply {
//                        notifyDataSetChanged()
//                        setSelected(0)
//                    }
//                }
//            }
//        }
//    }
//
//    private var subListResultCallback = object : ResultCallback<List<Category>?>() {
//        @SuppressLint("NotifyDataSetChanged")
//        override fun onSuccess(result: List<Category>?) {
//            showProgressView(false)
//            loadingSubCategoryList = false
//            if (result != null && result.isNotEmpty()) {
//                subCategoryList.apply {
//                    clear()
//                    addAll(result)
//                }
//                subCategoryAdapter?.notifyDataSetChanged()
//            } else {
//                setEmptyView(true)
//            }
//        }
//    }
//
//    @SuppressLint("NotifyDataSetChanged")
//    fun loadSubList(categoryId: String) {
//        subCategoryList.clear()
//        subCategoryAdapter?.notifyDataSetChanged()
//        setEmptyView(false)
//        showProgressView(true)
//        loadingSubCategoryList = true
//        subListResultCallback.remove()
//
//    }
//
//    @SuppressLint("NotifyDataSetChanged")
//    fun loadMainList() {
//        mainCategoriesList.clear()
//        subCategoryList.clear()
//
//        mainCategoryAdapter?.notifyDataSetChanged()
//        subCategoryAdapter?.notifyDataSetChanged()
//
//        showMainCategoryProgressView(true)
//        setEmptyView(false)
//        loadingMainCategoryList = true
//    }
//
//    private fun openAddCategoryFragment(main: Boolean) {
//        var parentCategoryId = ""
//        if (!main) {
//            mainCategoryAdapter?.getSelectedCategory()?.id?.let { parentCategoryId = it }
//        }
//
//        val fragment = AddNewCategoryFragment(newCategory = {
//            categories?.add(it)
//            if (parentCategoryId.isEmpty()) {
//                mainCategoriesList.add(it)
//                val size = mainCategoriesList.size
//                mainCategoryAdapter?.apply {
//                    notifyItemInserted(size)
//                    setSelected(size - 1)
//                }
//                AndroidUtilities.runOnUIThread({
//                    binding.mainRecyclerView?.smoothScrollToPosition(size)
//                }, 200)
//            } else {
//                setEmptyView(false)
//                showProgressView(false)
//                subCategoryList.add(it)
//                subCategoryAdapter?.notifyItemInserted(subCategoryList.size)
//            }
//        }).apply {
//            category.parentId = parentCategoryId
//        }
//        presentFragmentRemoveLast(fragment, false)
//    }
//
//    override fun onViewAttachedToParent() {
//        super.onViewAttachedToParent()
//        bottomNavVisiblity(requireContext(), false)
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?,
//        binding: FragmentCategoriesBinding,
//    ) {
//        binding.apply {
//            actionBar.apply {
//                backButton.setOnClickListener { closeLastFragment() }
//                titleContainer.gravity = Gravity.START
//                title.text = getString(R.string.string_catgs)
//                emptyScreen.addItemButton.setOnClickListener { openAddCategoryFragment(false) }
//                options.setOnClickListener {
//                    PopupWindowLayout(requireContext()).apply {
//                        addItem(0, "Add category", R.drawable.msg_addbot) {
//                            openAddCategoryFragment(true)
//                        }
//                        addItem(1,
//                            "Add sub category",
//                            R.drawable.msg_addfolder) { openAddCategoryFragment(false) }
//                        addItem(2,
//                            "Delete category",
//                            R.drawable.msg_delete) {
//                            mainCategoryAdapter?.selectedItem?.let {
//                                deleteCategory(position = it,
//                                    true)
//                            }
//                        }
//                        PopupDialog(this).show(
//                            requireView(),
//                            0,
//                            MyApplication.displaySize.first - measuredWidth,
//                            AndroidUtilities.dp(8f),
//                            true)
//                    }
//                }
//            }
//            subRecyclerView.apply {
//                adapter = SubCategoryAdapter { deleteCategory(it, false) }.also {
//                    subCategoryAdapter = it
//                }.apply {
//                    setOnItemClickListener(object : RecyclerListView.RecyclerItemClickListener {
//                        override fun onClick(position: Int, type: Int) {
//                            subCategoryList.getOrNull(position)?.let {
//
//                            }
//                        }
//                    })
//                }
//            }
//            mainRecyclerView.apply {
//                adapter =
//                    MainCategoryAdapter(onItemSelected = { categoryId: String, _: CategoryItemBinding?, _: Int ->
//                        loadSubList(categoryId)
//                    },
//                        onLongClicked = { p ->
//                            deleteCategory(p,
//                                true)
//                        }).also { mainCategoryAdapter = it }
//                layoutManager = LinearLayoutManager(requireContext())
//            }
//            loadMainList()
//        }
//    }
//
//    inner class SubCategoryAdapter(val onLongClicked: (position: Int) -> Boolean) :
//        RecyclerListView.SimpleAdapter<CategoryItemBinding, Category>(R.layout.category_item) {
//        init {
//            currentList = subCategoryList
//        }
//
//        override fun onViewHolderCreated(
//            holder: RecyclerListView.BaseViewHolder<CategoryItemBinding>,
//            type: Int
//        ) {
//            super.onViewHolderCreated(holder, type)
//            holder.apply {
//                itemView.setOnLongClickListener {
//                    return@setOnLongClickListener onLongClicked(layoutPosition)
//                }
//            }
//        }
//
//        override fun bind(
//            holder: RecyclerListView.BaseViewHolder<CategoryItemBinding>,
//            position: Int,
//            model: Category
//        ) {
//            super.bind(holder, position, model)
//            holder.binding.apply {
//                model.apply {
//                    photoView.load(photo, circleCrop = true, fade = true)
//                    nameView.text = name
//                    productCountView.text = productsCount.toString()
//                }
//            }
//        }
//    }
//
//    inner class MainCategoryAdapter(
//        val onItemSelected: (categoryId: String, binding: CategoryItemBinding?, pos: Int) -> Unit,
//        val onLongClicked: (position: Int) -> Boolean,
//    ) : RecyclerListView.SimpleAdapter<CategoryItemBinding, Category>(R.layout.category_item) {
//        init {
//            currentList = mainCategoriesList
//            setHasStableIds(false)
//        }
//
//        var selectedItem = -1
//        var lastSelectedItem = -1
//
//        private val selectedColor = Color.LTGRAY
//        private val unSelectedColor = Color.WHITE
//
//        fun setSelected(categoryId: String) {
//            val positon = findCategoryPosition(categoryId)
//            if (positon != -1) {
//                setSelected(positon)
//            } else {
//                throw Exception("Selected position is -1")
//            }
//        }
//
//        fun getSelectedCategory() = currentList.getOrNull(selectedItem)
//
//        fun findCategoryPosition(categoryId: String): Int {
//            var position = -1
//            currentList.forEachIndexed { index, category ->
//                if (category.id == categoryId) {
//                    position = index
//                }
//            }
//            return position
//        }
//
//        fun setSelected(position: Int, force: Boolean = false) {
//            selectedItem = position
//            if (lastSelectedItem == selectedItem && !force) {
//                return
//            }
//            var b: CategoryItemBinding? = null
//            mRecyclerView?.apply {
//                findViewHolderForLayoutPosition(lastSelectedItem)?.itemView?.setBackgroundColor(
//                    unSelectedColor)
//
//                findViewHolderForAdapterPosition(selectedItem)?.apply {
//                    if (this is RecyclerListView.BaseViewHolder<*>) {
//                        b = binding as CategoryItemBinding?
//                    }
//                    itemView.setBackgroundColor(selectedColor)
//                    animateItem(itemView)
//                }
//            }
//            val itemId = currentList[position].id
//            onItemSelected(itemId, b, selectedItem)
//            lastSelectedItem = selectedItem
//        }
//
//
//        fun animateItem(view: View) {
//            view.apply {
//                alpha = 0.8f
//                doOnPreDraw {
//                    animate().alpha(1f).setDuration(300).start()
//                }
//            }
//        }
//
//        var lastPosition = -1
//
//        override fun bind(
//            holder: RecyclerListView.BaseViewHolder<CategoryItemBinding>,
//            position: Int,
//            model: Category,
//        ) {
//            super.bind(holder, position, model)
//            holder.binding.apply {
//                val loading = model.id == LOADING
//                photoView.apply {
//                    if (loading) {
//                        setImageDrawable(null)
//                    } else {
//                        foreground = null
//                        load(model.photo, circleCrop = true)
//                    }
//                }
//                nameView.apply {
//                    text = if (loading) {
//                        "****"
//                    } else {
//                        foreground = null
//                        model.name
//                    }
//                }
//                productCountView.apply {
//                    text = if (loading) {
//                        "***"
//                    } else {
//                        foreground = null
//                        model.productsCount.toString()
//                    }
//                }
//                root.apply {
//                    setBackgroundColor(if (selectedItem == position) selectedColor else unSelectedColor)
//                    if (lastPosition != position) {
//                        lastPosition = position
//                        animateItem(root)
//                    }
//                }
//            }
//        }
//
//        override fun onViewHolderCreated(
//            holder: RecyclerListView.BaseViewHolder<CategoryItemBinding>,
//            type: Int,
//        ) {
//            super.onViewHolderCreated(holder, type)
//            holder.apply {
//                itemView.apply {
//                    setOnClickListener {
//                        setSelected(layoutPosition)
//                    }
//                    setOnLongClickListener {
//                        return@setOnLongClickListener onLongClicked(layoutPosition)
//                    }
//                }
//            }
//        }
//    }
//
//    override fun onCreateView(binding: FragmentCategoriesBinding) {
//
//    }
//}

