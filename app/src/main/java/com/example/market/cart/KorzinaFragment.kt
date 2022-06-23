package com.example.market.cart

//import android.animation.ValueAnimator
//import android.annotation.SuppressLint
//import android.content.res.ColorStateList
//import android.graphics.Color
//import android.graphics.drawable.Drawable
//import android.os.Bundle
//import android.view.Gravity
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.constraintlayout.widget.ConstraintLayout
//import androidx.constraintlayout.widget.ConstraintSet
//import androidx.core.animation.doOnEnd
//import androidx.core.animation.doOnStart
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.example.market.*
//import com.example.market.binding.increaseOrderCount
//import com.example.market.binding.inflateBinding
//import com.example.market.binding.load
//import com.example.market.bottomsheets.PaymentBottomSheet
//import com.example.market.databinding.EmptyScreenBinding
//import com.example.market.databinding.FragmentKorzinaBinding
//import com.example.market.databinding.OrderItemBinding
//import com.example.market.location.LocationActivity
//import com.example.market.models.Order
//import com.example.market.navigation.FragmentController
//import com.example.market.fragments.PaymentFragment
//import com.example.market.recycler.databoundrecycler.DataBoundAdapter
//import com.example.market.recycler.databoundrecycler.DataBoundViewHolder
//import com.example.market.utils.FirestorePaging
//import com.example.market.viewUtils.getCostText
//import com.example.market.viewUtils.toast
//import com.example.market.viewUtils.vibrate
//import java.util.*
//
//class KorzinaFragment(var hasBackButton: Boolean = false) : BaseFragment<FragmentKorzinaBinding>(R.layout.fragment_korzina) {
//    private var listAdapter: OrderAdapter? = null
//    private var paging: FirestorePaging<Order>? = null
//    private var emptyBinding: EmptyScreenBinding? = null
//
//    private var isShowDeleting = false
//    private var isEmptyShowing = false
//    private var isLoading = false
//    private var showDostavkaLayout = false
//
//    private var redColor = 0
//    private var grayColor = 0
//    private var red2Color = 0
//    private var arrowTopDrawable: Drawable? = null
//    private var fullCost = 0
//
//    fun updateOrdersCount() {
//        binding.actionBar.title.text = "Orders " + orders.size.toString()
//    }
//
//    override fun onViewFullyVisible() {
//        super.onViewFullyVisible()
//
//        val size = orders.size
//        if (size > 0) {
//            updateActionBarTitle()
//            if (isEmptyShowing || isLoading) {
//                showEmptyView(false)
//                showProgressBar(false)
//            }
//            updateDostavkaLayout()
//            if (!isShowBottomActions) {
//                showBottomActionsView(true, true)
//                showDostavkaLayout(true)
//                updateOrdersCost()
//            }
//        }
//        listAdapter?.notifyDataSetChanged()
//    }
//
//    private var isShowBottomActions = false
//
//    fun showBottomActionsView(show: Boolean, force: Boolean = false) {
//        if (isShowBottomActions != show || force) {
//            isShowBottomActions = show
//            binding.apply {
//                root.let {
//                    val vis = deleteLayout.root.visibility
//                    if (show && vis != View.VISIBLE || !show && vis != View.GONE || force) {
//                        (it as ConstraintLayout).apply {
//                            changeVisibility(R.id.delete_layout, show)
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private fun showTrendProducts() {
//        getMainActivity().bottomNavigationView.selectedItemId = R.id.home
//    }
//
//    fun showDostavkaLayout(show: Boolean) {
//        if (showDostavkaLayout != show) {
//            showDostavkaLayout = show
//            binding.dostavkaLayout.root.apply {
//                changeVisibility(R.id.dostavka_layout, true)
//                if (show) {
//                    alpha = 0f
//                }
//                animate().alpha(if (show) 1f else 0f).translationY(if (show) 0f else 50f).setDuration(300).setUpdateListener {
//                    if (!show && alpha == 0f) {
//                        changeVisibility(R.id.dostavka_layout, false)
//                    }
//                    if (show && alpha == 1f) {
//                        changeVisibility(R.id.dostavka_layout, visibility = true)
//                    }
//                }.start()
//            }
//        }
//    }
//
//    fun changeVisibility(childId: Int, visibility: Boolean) {
//        binding.root.let {
//            (it as ConstraintLayout).apply {
//                ConstraintSet().apply {
//                    clone(it)
//                    setVisibility(childId, if (visibility) View.VISIBLE else View.GONE)
//                    applyTo(it)
//                }
//            }
//        }
//    }
//
//    fun updateDostavkaLayout() {
//        binding.dostavkaLayout.apply {
//            val shipping = currentUser?.shippingLocation
//            planTextView.text = shipping?.type ?: "Choose plan"
//            costTextView.text = if (shipping != null) getCostText(shipping.cost) else "No adress"
//            adressTextView.text = shipping?.adress ?: "Where to deliver?"
//        }
//    }
//
//    fun updateCheckBox() {
//        binding.deleteLayout.checkBox.apply {
//            listAdapter?.let {
//                isChecked = it.selectedPositions.size == it.dataList.size
//            }
//        }
//    }
//
//    fun getCostSumOrders(): Int {
//        var cost = 0
//        listAdapter?.getCheckedOrders()?.let {
//            it.forEach { or ->
//                or.product?.narxi?.let { cost += (it.toInt()) * or.count }
//                or.shippingLocation?.cost?.let { cost += it.toInt() }
//            }
//        }
//        return cost
//    }
//
//    fun showProgressBar(show: Boolean) {
//        if (isLoading != show) {
//            isLoading = show
//            if (show && isEmptyShowing) {
//                showEmptyView(false)
//            }
//            binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
//        }
//    }
//
//    fun deleteCheckedOrders(): Boolean {
//        listAdapter?.apply {
//            getCheckedOrders()?.forEach {
//                deleteOrder(it.id, null)
//            }
//            deleteCheckedOrders()
//            updateOrdersCount()
//            showDeleteOrders(false, false)
//            val empty = orders.isEmpty()
//            isShowBottomActions = false
//            showEmptyView(empty)
//        }
//        return false
//    }
//
//    fun showDeleteOrders(show: Boolean, animated: Boolean = true, force: Boolean = false) {
//        if (isShowDeleting != show || force) {
//            isShowDeleting = show
//
//            binding.apply {
//                listAdapter?.showDeleteItems(show, true)
//                deleteLayout.checkBox.isChecked = false
//
//                actionBar.options.setImageResource(if (show) R.drawable.edit_cancel else R.drawable.chats_delete)
//
//                updateBottomActionsView(show, {
//                    updateActionBarTitle()
//                    updateOrdersCost()
//                    updateSelectedOrdersSize()
//                    if (!show) {
//                        showDostavkaLayout(true)
//                    }
//                }, animated)
//                if (show) {
//                    showDostavkaLayout(false)
//                }
//            }
//            vibrate(30)
//        }
//    }
//
//    fun updateDeleteLayout(isDelete: Boolean) {
//        binding.deleteLayout.apply {
//            leftTextView.apply {
//                setTextColor(if (isDelete) redColor else Color.BLACK)
//                setCompoundDrawables(null, null, if (isDelete) null else arrowTopDrawable, null)
//            }
//        }
//    }
//
//    fun updateSelectedOrdersSize() {
//        listAdapter?.let {
//            fullCost = getCostSumOrders()
//            updateOrdersCost()
//            val size = it.selectedPositions.size
//            binding.deleteLayout.rightTextView.text =
//                (if (isShowDeleting) "DELETE" else "BUY") + " ($size)"
//        }
//    }
//
//    fun updateOrdersCost() {
//        binding.deleteLayout.leftTextView.apply {
//            text = if (isShowDeleting) "CANCEL" else getCostText(fullCost.toString())
//        }
//    }
//
//    fun updateActionBarTitle() {
//        binding.actionBar.title.text =
//            if (isShowDeleting) "Delete order" else "Orders ${orders.size}"
//    }
//
//    fun updateBottomActionsView(isDelete: Boolean, onAnimateFinish: () -> Unit, animated: Boolean) {
//        binding.deleteLayout.apply {
//            showBottomActionsView(true, true)
//
//            if (!animated) {
//                updateDeleteLayout(isDelete)
//                onAnimateFinish()
//                return
//            }
//            var a = 0
//            ValueAnimator.ofFloat(1f, 0.2f).apply {
//                duration = 300
//                interpolator = FragmentController.accelerateDecelerateInterpolator
//                addUpdateListener {
//                    val animatedVal = it.animatedValue as Float
//                    root.translationY = root.height - (root.height * animatedVal)
//                    binding.actionBar.apply {
//                        val c = animatedVal
//                        title.apply { alpha = c }
//                        options.apply {
//                            alpha = c
//                            rotation = 180 - 180 * animatedVal
//                        }
//                    }
//                }
//                doOnEnd {
//                    if (a != 1) {
//                        setFloatValues(0.2f, 1f)
//                        start()
//                        a = 1
//                        updateDeleteLayout(isDelete)
//                        onAnimateFinish()
//                    }
//                }
//                doOnStart { a = 0 }
//            }.start()
//
//        }
//    }
//
//    fun showEmptyView(show: Boolean) {
//        if (isEmptyShowing != show&&isViewCreated) {
//            isEmptyShowing = show
//            if (show) {
//                showProgressBar(false)
//                showDostavkaLayout(false)
//                showBottomActionsView(false, true)
//                emptyBinding = null
//                emptyBinding =
//                    inflateBinding((binding.root as ViewGroup), R.layout.empty_screen, false)
//
//                emptyBinding?.apply {
//                    lottieView.setAnimationFromUrl("https://assets2.lottiefiles.com/packages/lf20_peztuj79.json")
//                    titleView.text = "Your cart is empty"
//                    subtitleView.text = "Press below button to see new trend products"
//                    addItemButton.apply {
//                        setOnClickListener { showTrendProducts() }
//                        text = "See trend products"
//                    }
//                    binding.root.let {
//                        (it as ConstraintLayout).apply {
//                            root.id = R.id.emptyScreen
//                            addView(root)
//
//                            val constraintset = ConstraintSet()
//                            constraintset.clone(this)
//                            constraintset.constrainHeight(root.id,
//                                ConstraintLayout.LayoutParams.WRAP_CONTENT)
//                            constraintset.constrainHeight(root.id,
//                                ConstraintLayout.LayoutParams.WRAP_CONTENT)
//                            constraintset.connect(root.id,
//                                ConstraintSet.START,
//                                id,
//                                ConstraintSet.START)
//                            constraintset.connect(root.id, ConstraintSet.TOP, id, ConstraintSet.TOP)
//                            constraintset.connect(root.id, ConstraintSet.END, id, ConstraintSet.END)
//                            constraintset.connect(root.id,
//                                ConstraintSet.BOTTOM,
//                                id,
//                                ConstraintSet.BOTTOM)
//                            constraintset.applyTo(it)
//                        }
//                    }
//                }
//            } else {
//                binding.root.let {
//                    (it as ViewGroup).removeView(emptyBinding?.root)
//                    emptyBinding = null
//                }
//            }
//
//        }
//    }
//
//    inner class OrderAdapter(var onSelectedChanged: (size: Int) -> Unit) :
//        DataBoundAdapter<OrderItemBinding, Order>(R.layout.order_item) {
//        var selectedPositions = arrayListOf<Int>()
//        private var deleteItems = false
//
//        init {
//            setHasStableIds(false)
//        }
//
//        @SuppressLint("NotifyDataSetChanged")
//        fun showDeleteItems(show: Boolean, force: Boolean = false) {
//            if (deleteItems != show || force) {
//                deleteItems = show
//                selectedPositions.clear()
//                notifyDataSetChanged()
//            }
//        }
//
//        @SuppressLint("NotifyDataSetChanged")
//        fun checkAll() {
//            selectedPositions.clear()
//            for (i in 0 until dataList.size) {
//                selectedPositions.add(i)
//            }
//            notifyDataSetChanged()
//        }
//
//        fun deleteCheckedOrders() {
//                selectedPositions.forEach {
//                    try {
//                        dataList.removeAt(it)
//                        notifyItemRemoved(it)
//                    } catch (e: Exception) { }
//                }
//        }
//
//        fun getCheckedOrders(): ArrayList<Order>? {
//            if (selectedPositions.isNotEmpty() && dataList.isNotEmpty()) {
//                ArrayList<Order>().apply {
//                    selectedPositions.forEach {
//                        dataList.getOrNull(it)?.let { add(it) }
//                    }
//                    if (isNotEmpty()) {
//                        return this
//                    }
//                }
//            }
//            return null
//        }
//
//        val colorStateList = ColorStateList(arrayOf(intArrayOf(android.R.attr.state_checked),
//            intArrayOf(-android.R.attr.state_checked)), intArrayOf(redColor, Color.DKGRAY))
//        val deleteColorStateList = ColorStateList(arrayOf(intArrayOf(android.R.attr.state_checked),
//            intArrayOf(-android.R.attr.state_checked)), intArrayOf(redColor, redColor))
//
//        override fun onCreateViewHolder(
//            viewHolder: DataBoundViewHolder<OrderItemBinding>?,
//            viewType: Int,
//        ) {
//            viewHolder?.apply {
//                binding.apply {
//                    decreaseCountView.setOnClickListener {
//                        getItem(layoutPosition)?.let {
//                            increaseOrderCount((it), false, binding.countView)
//                            onSelectedChanged(-1)
//                        }
//                    }
//                    increaseCountView.setOnClickListener {
//                        getItem(layoutPosition)?.let {
//                            increaseOrderCount((it), true, binding.countView)
//                            onSelectedChanged(-1)
//                        }
//                    }
//                    checkBox.apply {
//                        isClickable = false
//                        isFocusable = false
//
//                        itemView.setOnClickListener {
//                            val check = !isChecked
//                            val pos = layoutPosition
//                            isChecked = check
//                            if (check) {
//                                selectedPositions.add(pos)
//                            } else {
//                                selectedPositions.remove(pos)
//                            }
//                            onSelectedChanged(selectedPositions.size)
//                        }
//                    }
//                }
//            }
//        }
//
//        override fun bindItem(
//            holder: DataBoundViewHolder<OrderItemBinding>,
//            binding: OrderItemBinding,
//            position: Int,
//            model: Order,
//        ) {
//                binding.apply {
//                    checkBox.supportButtonTintList = if (isShowDeleting) deleteColorStateList else colorStateList
//                    val check = selectedPositions.contains(holder.layoutPosition)
//                    checkBox.isChecked = check
//
//                    model.apply {
//                        product?.let {
//                            photoView.apply { load(it.photo) }
//                            nameView.text = it.title
//                            costTextView.text = getCostText(it.narxi)
//                            countView.text = count.toString()
//                            if (storeName.isNotEmpty()) {
//                                storeNameView.text = storeName
//                            }
//                            storePhotoView.load(storePhoto)
//                        }
//                    }
//                }
//
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?,
//        binding: FragmentKorzinaBinding,
//    ) {
//        redColor = resources.getColor(R.color.main_red)
//        grayColor = resources.getColor(R.color.gray_f2f2f2)
//        red2Color = resources.getColor(R.color.red_fff8f8)
//        arrowTopDrawable = resources.getDrawable(R.drawable.settings_arrow)
//
//        binding.apply {
//            actionBar.apply {
//                root.apply {
//                    setBackgroundColor(grayColor)
//                    elevation = 0f
//                }
//                backButton.apply {
//                    visibility = if (hasBackButton) View.VISIBLE else View.GONE
//                    setOnClickListener { closeLastFragment() }
//                }
//                title.apply {
//                    titleContainer.gravity = Gravity.START
//                    updateOrdersCount()
//                }
//                options.apply {
//                    setImageResource(R.drawable.chats_delete)
//                    setOnClickListener {
//                        if (isShowDeleting) {
//                            showDeleteOrders(false)
//                        } else {
//                            showDeleteOrders(true)
//                        }
//                    }
//                }
//                dostavkaLayout.root.setOnClickListener {
//                    presentFragmentRemoveLast(LocationActivity { updateDostavkaLayout() }, false)
//                }
//                deleteLayout.apply {
//                    checkBox.setOnClickListener {
//                        listAdapter?.apply {
//                            if (checkBox.isChecked) checkAll() else showDeleteItems(false,
//                                true)
//                        }
//                        updateSelectedOrdersSize()
//                    }
//
//                    leftTextView.setOnClickListener {
//                        if (isShowDeleting) {
//                            showDeleteOrders(false)
//                        }
//                    }
//                    buyNowButton.setOnClickListener {
//                        if (isShowDeleting) {
//                            deleteCheckedOrders()
//                        } else {
//                            listAdapter?.selectedPositions?.let {
//                                if (it.isNotEmpty()) {
//                                    PaymentBottomSheet(requireContext()) {}.show(this@KorzinaFragment)
//                                } else {
//                                    toast("Select order to buy")
//                                }
//                            }
//                        }
//                    }
//
//                }
//            }
//            recyclerView.apply {
//                showProgressBar(true)
//                adapter = OrderAdapter {
//                    updateSelectedOrdersSize()
//                    updateCheckBox()
//                }.also {
//                    listAdapter = it
//                    it.setDataList(orders)
//                }
//                layoutManager =
//                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
////
//                if (currentUser != null) {
//                    paging = FirestorePaging(Order::class.java,
//                        getOrdersCollection().whereEqualTo("customerId", currentUser!!.id),
//                        "id")
//                        .apply {
//                            list = orders
//                            orders.lastOrNull()?.let { newDocId = it.id }
//
//                            observe(viewLifecycleOwner) {
//                                if (it != null && it.isEmpty() || it == null) {
//                                    showEmptyView(true)
//                                } else {
//                                    updateActionBarTitle()
//                                    showProgressBar(false)
//                                    updateDostavkaLayout()
//                                    showDostavkaLayout(true)
//                                    showBottomActionsView(true)
//                                }
//                                it?.let { listAdapter?.setDataList(it) }
//                            }
//                        }
//                } else {
//                    showEmptyView(true)
//                }
//            }
//
//        }
//    }
//}