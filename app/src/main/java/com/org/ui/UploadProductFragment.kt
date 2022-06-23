package com.org.ui

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.view.doOnAttach
import androidx.core.view.setPadding
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.market.R
import com.example.market.databinding.FragmentProduktYuklashBinding
import com.example.market.databinding.ImageViewCheckableBinding
import com.google.android.material.textfield.TextInputLayout
import com.org.market.*
import com.org.net.NotificationCenterDelegate
import com.org.net.models.Photo
import com.org.net.models.Product
import com.org.net.models.Specification
import com.org.net.productPhotosDidLoad
import com.org.ui.actionbar.*
import com.org.ui.components.RecyclerListView
import com.org.ui.components.appInflater
import com.org.ui.components.load
import com.org.ui.components.visibleOrGone


class UploadProductFragment(val product: Product = Product()) :
    BaseFragment<FragmentProduktYuklashBinding>(R.layout.fragment_produkt_yuklash),
    NotificationCenterDelegate {

    private val isUpdate = product.id.isNotEmpty()
    private var photosAdapter: PhotosAdapter? = null
    var photos = arrayListOf<String>()
    var specifications = arrayListOf<Specification>()
    private var progress = 0

    private var progressMap = HashMap<Int, Boolean>().apply {
        put(0, false)
        put(1, false)
        put(2, false)
        put(3, false)
        put(4, false)
        put(5, false)
        put(6, false)
    }

    fun setProgress(type: Int, completed: Boolean) {
        val field = progressMap[type]
        if (field != null && field != completed) {
            progressMap[type] = completed
            updateProgress(completed)
        }
    }

    private var updateRunnable: Runnable? = null
    private var progressValue = 100 / progressMap.size

    fun updateProgress(increase: Boolean) {
        updateRunnable = Runnable {
            if (increase) {
                progress += progressValue
            } else {
                progress -= progressValue
            }
            if (progress < progressValue) {
                progress = progressValue
            }
            requireBinding().progressBar.setProgress(progress, true)
        }
        runOnUiThread(updateRunnable,300)
    }

    fun updateCategoryData() {
        requireBinding().categoryLayout.apply {
            val cat = product.category

            setProgress(4, cat.id.isNotEmpty())
            titleView.text = cat.name.ifEmpty { context().getString(R.string.choose_cat) }
            subtitleView.text =
                if (cat.name.isEmpty()) "Tap to open categories" else "Products: ${cat.productsCount}"
        }
    }

    fun uploadProduct() {
        checkAndGetFields {

        }
    }

    override fun isSwapBackEnabled(ev: MotionEvent): Boolean {
        return false
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(binding: FragmentProduktYuklashBinding) {
        binding.apply {
            actionBar.apply {
                title = "Create product"
                addMenuItem(0, R.drawable.ic_ab_done).setOnClickListener {
                    uploadProduct()
                }
            }

            titleEditText.clearErrorOnTextChange()
            subtitleEditText.clearErrorOnTextChange()
            costEditText.clearErrorOnTextChange()

            titleEditText.editText!!.doAfterTextChanged {
                val text = it.toString()
                setProgress(1, text.isNotEmpty())
            }
            subtitleEditText.editText!!.doAfterTextChanged {
                val text = it.toString()
                setProgress(2, text.isNotEmpty())
            }
            costEditText.editText!!.doAfterTextChanged {
                val text = it.toString()
                setProgress(3, text.isNotEmpty())
            }
            updateStatus(false)
            (currencyEditText.editText as AutoCompleteTextView).apply {
                val list = arrayListOf(
                    Currency.USZ.currency,
                    Currency.USD.currency
                )
                val adapter = ArrayAdapter(
                    context(),
                    R.layout.auto_complete_item_view,
                    R.id.text_view,
                    list
                )
                setAdapter(adapter)

            }
            costEditText.editText!!.apply {
                setOnFocusChangeListener { v, hasFocus ->
                    hint = if (hasFocus) {
                        "0.0000"
                    } else {
                        "Cost: 0.0000"
                    }
                }
            }
            categoryLayout.apply {
                iconView.visibleOrGone(false)
                root.setOnClickListener {
                    presentFragment(CategoriesFragment
                    {
                        product.category = it
                        updateCategoryData()
                    }, false)
                }
            }
            if (isUpdate) {
                product.apply {
                    titleEditText.editText!!.text.append(title)
                    subtitleEditText.editText!!.text.append(subtitle)
                    costEditText.editText!!.text.append(formatCurrency(cost))
                    currencyEditText.editText!!.setSelection(currency.ordinal)
                }
            }
            updateCategoryData()
            var ignoreChange = false
            hashTagView.doOnTextChanged { text, start, before, count ->
                if (ignoreChange) return@doOnTextChanged
                val builder = StringBuilder(text!!.trim())
                val first = builder.getOrNull(0)
                if (builder.length>1) {
                    val hashTag = builder.substring(1,builder.length)
                    setProgress(5,true)
                    product.hashtag = hashTag
                } else {
                    setProgress(5,false)
                }

                if (first != '#') {
                    ignoreChange = true
                    builder.insert(0, '#')
                    hashTagView.text!!.apply {
                        clear()
                        append(builder.toString())
                    }
                    ignoreChange = false
                }

            }
            hashTagView.text!!.append("#")

            photoDeleteView.setOnClickListener {
                if (!photosAdapter!!.checkedList.isEmpty()) {
                    photosAdapter?.deletePhotos()
                } else {
                    toast("Please choose photos to delete")
                }
            }
            addPhotoButton.setOnClickListener {
                photosAdapter?.openGallery()
            }

            photosRecyclerView.apply {
                photoDeleteView.alpha = 0.7f

                adapter = PhotosAdapter {
                    val showDelete = photosAdapter!!.checkedList.size > 0
                    photoDeleteView
                        .animate()
                        .alpha(if (showDelete) 1f else 0.7f)
                        .setDuration(300)
                        .start()
                }.apply {
                    photosAdapter = this

                    photos.add("Button")
                    setDataList(photos)
                }

                layoutManager =
                    GridLayoutManager(context, 3)
            }

            specificationRecyclerView.apply {
                specifications.add(Specification().apply { id = "+" })
                adapter = SpecificationAdapter()
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
            }

            if (isUpdate) {
                DataController.productPhotos[product.id]?.let {
                    photos.addAll(it.map { it.url })
                    photosAdapter?.notifyDataSetChanged()
                }
                loadingPhotos = true
                DataController.loadProductPhotos(product.id, object :
                    ResultCallback<ArrayList<Photo>>() {
                    override fun onSuccess(data: ArrayList<Photo>?) {
                        loadingPhotos = false
                        photos.clear()
                        val list = data!!.map { it.url }
                        photos.addAll(list)
                        photosAdapter?.apply {
                            notifyDataSetChanged()
                            isAddShown = photos.isNotEmpty()
                        }
                    }
                })
                DataController.getSpecification(product.id,
                    object : ResultCallback<ArrayList<Specification>>() {
                        override fun onSuccess(data: ArrayList<Specification>?) {
                            if (data == null) return
                            specifications.addAll(data)
                            data.forEach {
                                val index = specifications.indexOf(it)
                                specificationRecyclerView.adapter?.notifyItemInserted(index)
                            }
                        }
                    })
            }

        }
    }

    private var loadingPhotos = false

    override fun didReceiveNotification(id: Int, vararg objects: Any) {
        super.didReceiveNotification(id, *objects)
        if (isUpdate) {
            val data = objects[0] as Array<*>
            if (id == productPhotosDidLoad && data[0] == product.id) {
                if (data[1] is Error) {
                    return
                }
                val list = data[1] as ArrayList<String>
                photos.addAll(list)
                photosAdapter?.setDataList(photos)
            }
        }
    }

    fun getSpecificationsFromViewHolder(onError: (value: View) -> Unit): ArrayList<Specification> {
        val list = arrayListOf<Specification>()
        try {
            val listView = requireBinding().specificationRecyclerView
            val childCount = listView.childCount

            for (i in 0 until childCount) {
                with(listView.findViewHolderForLayoutPosition(i)) {
                    if (this is SpecificationAdapter.ViewHolder) {
                        val nameText = nameEditText.editText!!.text
                        val valueText = valueEditTExt.editText!!.text
                        if (nameText.isNullOrEmpty()) {
                            onError(nameEditText)
                            return@with
                        }
                        if (valueText.isNullOrEmpty()) {
                            onError(valueEditTExt)
                            return@with
                        }

                        val specification =
                            Specification(newId(), nameText.toString(), valueText.toString())
                        list.add(specification)

                    }
                }
            }
        } catch (e: Exception) {

        }
        return list
    }

    fun updateStatus(isUploading: Boolean) {
        requireBinding().completed.text = if (isUploading) "Uploading" else "Completed"
        progress = progressValue
        progressMap.forEach {
            progressMap[it.key] = false
        }
        requireBinding().progressBar.setProgress(progress,false)
    }

    private inline fun checkAndGetFields(data: (product: Product) -> Unit) = run {
        mBinding?.apply {
            val title = titleEditText.textOrEmptyError("Product must have a title") ?: return@apply
            val subtitle =
                subtitleEditText.textOrEmptyError("Subtitle must not be empty") ?: return@apply
            val cost = costEditText.textOrEmptyError("Product must have cost") ?: return@apply

            specifications = getSpecificationsFromViewHolder {
                val offsetY = it.top
                requireBinding().yuklashScrollview.smoothScrollTo(0,offsetY)
                shakeView(it)
                return@getSpecificationsFromViewHolder
            }

            if (specifications.isEmpty()) {
                shakeView(specificationRecyclerView)
                toast("Add at least one specification")
                return@apply
            }
            if (product.category.id.isEmpty()) {
                shakeView(categoryLayout.root)
                toast("Choose category")
                return@apply
            }
            if (photos.isEmpty()) {
                shakeView(photosRecyclerView)
                toast("Add at least one photo")
                return@apply
            }
            val quantityText = quantityEditText.editText!!.text.toString()
            if (quantityText.isEmpty() || quantityText.toInt() <= 0) {
                shakeView(quantityEditText)
                toast("Quantity must be 1 or more")
                return@apply
            }
            val quantity = quantityText.toInt()

            if (product.hashtag.isNullOrEmpty()) {
                shakeView(hashTagView)
                toast("Set a hashtag")
                return@apply
            }
            val discount = discountView.editText!!.text.toString().ifEmpty { "0" }.toInt()
            val videoUrl = videoUrlView.editText!!.text.toString()

            product.apply {
                this.id = newId()
                this.title = title
                this.subtitle = subtitle
                this.cost = cost.toLong()
                this.count = quantity
                this.sellerId = currentUserId()
                this.sellerPhoto = currentUser().photo
                this.sellerName = currentUser().name
                this.count = quantity
                this.sellerMobilePhone = currentUser().mobilePhone
                this.sellerSubscribersCount = currentUser().subscribers
                this.discountPercent = discount.toFloat()
                this.videoUrl = videoUrl
                val scalePhotoUrl = photos.first()

                updateStatus(true)
                DataController.uploadProductPhotos(product.id,photos,object : ResultCallback<ArrayList<String>>(){
                    override fun onSuccess(data: ArrayList<String>?) {
                        if (data==null) {
                            toast("Photos did not upload,Error!")
                            return
                        }
                        product.photo = data.first()
                        setProgress(0,true)
                        setProgress(1,true)
                        DataController.getPhotoScaleRatio(scalePhotoUrl) {
                            product.photoScaleRatio = it
                            setProgress(2,true)
                            setProgress(3,true)
                            DataController.uploadProductSpecifications(product.id,specifications) {
                                setProgress(4,true)
                                setProgress(5,true)
                                DataController.uploadProduct(product) {
                                    setProgress(6,true)
                                    DataController.getProducts(currentUserId()).add(0,product)
                                    currentUser().products +=1
                                    saveUserConfig()
                                    closeLastFragment()
                                }
                            }
                        }
                    }

                    override fun onFailed(exception: Exception?) {
                        super.onFailed(exception)
                    }
                })
            }
        }
        data(product)
    }

    inner class PhotosAdapter(
        val onChecked: (checkedId: Int) -> Unit,
    ) : RecyclerListView.SimpleAdapter
    <ImageViewCheckableBinding, String>
        (R.layout.image_view_checkable) {

        val checkedList = arrayListOf<String>()
        var isAddShown = false
            set(value) {
                if (field != value) {
                    field = value
                    requireBinding().addPhotoButton.visibleOrGone(value, 1)
                    if (value) {
                        photos.remove(photos.find { it == buttonType })
                        notifyItemRemoved(0)
                    } else {
                        photos.add(buttonType)
                        notifyItemInserted(0)
                    }
                }
            }

        val buttonType = "Button"

        fun deletePhotos() {
            try {
                if (checkedList.isNotEmpty()) {
                    checkedList.forEach {
                        val index = photos.indexOf(it)
                        checkedList.remove(it)
                        photos.removeAt(index)
                        notifyItemRemoved(index)
                    }
                    if (photos.isEmpty()) {
                        isAddShown = false
                    }
                    resizeRecyclerView()
                }
            } catch (e: Exception) {

            }
        }

        override fun onViewHolderCreated(
            holder: RecyclerListView.BaseViewHolder<ImageViewCheckableBinding>,
            type: Int,
        ) {
            holder.apply {
                binding.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                    val currentChecked = currentList[layoutPosition]

                    if (isChecked) {
                        checkedList.add(currentChecked)
                    } else {
                        checkedList.remove(currentChecked)
                    }
                }
                binding.root.setOnClickListener {
                    if (photos[layoutPosition] == buttonType) {
                        openGallery()
                        return@setOnClickListener
                    } else {
                        openPhotoViewer(context(), binding.imageView, photos)
                    }
                }
            }
        }

        val removeList = arrayListOf<String>()
        val listToAdd = arrayListOf<String>()

        fun openGallery() {
            presentFragment(GalleryFragment({
                runOnUiThread({
                    isAddShown = true

                    val oldList = photos
                    val newList = it

                    removeList.clear()
                    listToAdd.clear()

                    oldList.forEach {
                        if (!newList.contains(it)) {
                            removeList.add(it)
                        }
                    }
                    removeList.forEach {
                        val index = oldList.indexOf(it)
                        oldList.removeAt(index)
                        notifyItemRemoved(index)
                    }
                    newList.forEach {
                        if (!oldList.contains(it)) {
                            listToAdd.add(it)
                        }
                    }
                    listToAdd.forEach {
                        oldList.add(0, it)
                        notifyItemInserted(0)
                    }

                    resizeRecyclerView()
                }, 300)

            }, 6).apply {
                if (isAddShown) {
                    this.checkedImages = photos.toMutableList() as ArrayList<String>
                }
            }, false)
        }

        fun resizeRecyclerView() {
            runOnUiThread({
                mRecyclerView?.requestLayout()
            }, 300)
        }

        private val padding = dp(32f)
        override fun bind(
            holder: RecyclerListView.BaseViewHolder<ImageViewCheckableBinding>,
            position: Int,
            model: String,
        ) {
            holder.apply {
                val check = checkedList.contains(model)
                binding.apply {
                    val button = model == "Button"
                    checkBox.visibleOrGone(!button)
                    imageView.apply {
                        if (button) {
                            setImageResource(R.drawable.msg_addphoto)
                            imageTint(R.attr.colorOnSurfaceMedium)
                            setPadding(padding, padding, padding, padding)
                        } else {
                            imageView.load(model)
                            imageTintList = null
                            setPadding(0, 0, 0, 0)
                        }
                    }
                    checkBox.isChecked = check
                }
            }
        }
    }

    inner class SpecificationAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var nameEditText: TextInputLayout =
                view.findViewById<TextInputLayout>(R.id.option_title_editText).apply {
                    doOnAttach {
                        showKeyboard(this)
                    }
                }
            var valueEditTExt: TextInputLayout =
                view.findViewById<TextInputLayout>(R.id.options_value_editText)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view =
                appInflater.inflate(if (viewType == 0) R.layout.optisya_qoshish_button else R.layout.optsiya_item,
                    parent,
                    false).apply {
                    setOnClickListener {
                        if (viewType == 0) {
                            specifications.add(Specification())
                            notifyItemInserted(0)
                        }
                    }
                }
            return if (viewType == 0) object :
                RecyclerView.ViewHolder(view) {} else ViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        }

        override fun getItemViewType(position: Int): Int {
            return if (specifications[position].id == "+") 0 else 1
        }

        override fun getItemCount(): Int {
            return specifications.size
        }
    }
}
