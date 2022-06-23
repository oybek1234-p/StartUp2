package com.example.market
//
//import android.graphics.*
//import android.os.Build
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.*
//import androidx.annotation.RequiresApi
//import androidx.constraintlayout.widget.ConstraintLayout
//import androidx.core.widget.doOnTextChanged
//import androidx.lifecycle.lifecycleScope
//import androidx.recyclerview.widget.GridLayoutManager
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.bumptech.glide.request.target.SimpleTarget
//import com.bumptech.glide.request.transition.Transition
//import com.example.market.databinding.FragmentProduktYuklashBinding
//import com.example.market.models.Product
//import com.example.market.navigation.bottomNavVisiblity
//import com.example.market.viewUtils.presentFragmentRemoveLast
//import com.google.android.material.textfield.TextInputEditText
//import com.google.android.material.textfield.TextInputLayout
//import com.google.firebase.FirebaseApp
//import kotlinx.coroutines.launch
//
//class ProduktYuklashFragment : BaseFragment<FragmentProduktYuklashBinding>(R.layout.fragment_produkt_yuklash) {
//    private lateinit var optionsAdapter: OptionsAdapter
//    var arrayList = arrayListOf<Specification>()
//    private var list: ArrayList<String>? = null
//
//    private fun yuklash(): Boolean {
//        val product = Product()
//        var description = ""
//        binding.apply {
//            productTitleEditText.apply {
//                text?.apply {
//                    if (isEmpty() || length < 16) {
//                        if (parent.parent is TextInputLayout) {
//                            (parent.parent as TextInputLayout).apply {
//                                isErrorEnabled = true
//                                error =
//                                    if (isEmpty()) "Produkt nomini kiriting" else "Simvollar kamida 16 ta bulishi kerak"
//                            }
//                        }
//                        yuklashScrollview.smoothScrollTo(0, bottom)
//                        return false
//                    }
//                    product.title = text.toString()
//                }
//            }
//            productDescriptionEditText.apply {
//                text?.apply {
//                    if (isEmpty() && length < 60) {
//                        if (parent.parent is TextInputLayout) {
//                            (parent.parent as TextInputLayout).apply {
//                                isErrorEnabled = true
//                                error =
//                                    if (isEmpty()) "Produkt haqida malumot kiriting" else "Simvollar kamida 60 ta bulishi kerak"
//                            }
//                        }
//                        yuklashScrollview.smoothScrollTo(0, bottom)
//                        return false
//                    }
//                    description = text.toString()
//                }
//            }
//            if (list == null || list != null && list!!.isEmpty()) {
//                Toast(context).apply {
//                    this.view = LayoutInflater.from(context).inflate(R.layout.custom_snackbar,
//                        this@ProduktYuklashFragment.view as ViewGroup,
//                        false).apply {
//                        findViewById<TextView>(R.id.snackbar_textView).text = "Rasm yuklang"
//                        findViewById<ImageView>(R.id.snackbar_icon).setImageResource(R.drawable.error_image)
//                    }
//                    duration = Toast.LENGTH_SHORT
//                    show()
//                    yuklashScrollview.smoothScrollTo(0, rasmlarContainer.top)
//                }
//                return false
//            }
//            productNarxiEditText.apply {
//                text?.apply {
//                    if (isEmpty()) {
//                        if (parent.parent is TextInputLayout) {
//                            (parent.parent as TextInputLayout).apply {
//                                isErrorEnabled = true
//                                error = "Narxni kiriting"
//                            }
//                        }
//                        return false
//                    }
//                    product.narxi = text.toString()
//                }
//            }
//            productSoniEditText.apply {
//                text?.apply {
//                    if (isEmpty()) {
//                        if (parent.parent is TextInputLayout) {
//                            (parent.parent as TextInputLayout).apply {
//                                isErrorEnabled = true
//                                error = "Produkt sonini kiriti"
//                            }
//                        }
//                        return false
//                    }
//                    product.soni = text.toString()
//                }
//            }
//            chooseCurrencyYuklash.text?.let {
//                product.currency = it.toString()
//            }
//            productKatergotiyaEditText.apply {
//                text?.apply {
//                    if (isEmpty()) {
//                        if (parent.parent is TextInputLayout) {
//                            (parent.parent as TextInputLayout).apply {
//                                isErrorEnabled = true
//                                error = "Kategoriyani kiriting"
//                            }
//                        }
//                        return false
//                    }
//                    product.kategoriya = text.toString()
//                }
//            }
//            product.dostavka = (3000..21000).random().toString()
//            product.discount = (5..100).random().toString()
//
//            lifecycleScope.launch {
//                uploadProduct(product, description, list!!, arrayList, object : Result {
//                    override fun onFailed() {
//
//                    }
//
//                    override fun onSuccess(any: Any?) {
//                        (context as MainActivity).apply {
//                            fragmentController?.closeLastFragment()
//                            selectedUriPhotos = null
//                        }
//                        Toast(context).apply {
//                            this.view = LayoutInflater.from(context)
//                                .inflate(R.layout.custom_snackbar,
//                                    (context as MainActivity).window.decorView as ViewGroup,
//                                    false)
//                            duration = Toast.LENGTH_SHORT
//                        }.show()
//
//                    }
//                }, lifecycleScope)
//            }
//        }
//
//        return true
//    }
//
//    override fun onBeginSlide() {
//
//    }
//
//    override fun isSwapBackEnabled(): Boolean {
//        return true
//    }
//
//    override fun onConnectionChanged(state: Boolean) {
//
//    }
//
//    override fun onBackPressed() {
//
//    }
//
//    override fun onViewFullyVisible() {
//        initFragment()
//    }
//
//    override fun onViewFullyHiden() {
//    }
//
//    override fun onViewAttachedToParent() {
//
//    }
//
//    override fun onViewDetachedFromParent() {
//
//    }
//
//    override fun canBeginSlide(): Boolean {
//        return true
//    }
//
//    //We will call this method after animation finished for performance cases
//    private fun initFragment() {
//        (context as MainActivity).apply {
//            bottomNavVisiblity(context, false)
//            list = bundleAny as ArrayList<String>?
//        }
//        if (list != null && list!!.isNotEmpty()) {
//            rasmlarListView = null
//            rasmlarListView = RecyclerView(context as MainActivity).apply {
//                isNestedScrollingEnabled = false
//                setHasFixedSize(true)
//                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
//                    FrameLayout.LayoutParams.MATCH_PARENT)
//                layoutManager = GridLayoutManager(context, 3)
//                adapter = PhotosAdapter(list!!)
//            }
//            binding.rasmlarContainer.apply {
//                removeAllViews()
//                addView(rasmlarListView!!)
//            }
//        }
//    }
//
//    private var rasmlarListView: RecyclerView? = null
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?,
//        binding: FragmentProduktYuklashBinding
//    ) {
//        binding.apply {
//            yuklashScrollview.apply {
//                descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS
//                isFocusable = true
//                isFocusableInTouchMode = true
//                setOnTouchListener { v, event ->
//                    (context as MainActivity).apply {
//                        currentFocus?.let {
//                            if (it is TextInputEditText) {
//                                val rect = Rect()
//                                it.getGlobalVisibleRect(rect)
//                                if (!rect.contains(event.rawX.toInt(), event.rawY.toInt())) {
//                                    it.clearFocus()
//                                    closeKeyboard(it)
//                                }
//                            }
//                        }
//                    }
//                    false
//                }
//            }
//
//            productTitleEditText.apply {
//                onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
//                    if (!hasFocus) {
//
//                        text!!.length.let {
//                            if (it < 16) {
//                                if (parent.parent is TextInputLayout) {
//                                    (parent.parent as TextInputLayout).apply {
//                                        isErrorEnabled = true
//                                        error = "Simvollar kamida 16ta bulishi kerak"
//                                        this.isEndIconVisible = true
//                                        this.isEndIconCheckable = true
//                                    }
//                                }
//                            }
//
//                        }
//
//                    }
//                }
//            }
//            productDescriptionEditText.apply {
//                onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
//                    if (!hasFocus) {
//                        text!!.length.let {
//                            if (parent.parent is TextInputLayout) {
//                                (parent.parent as TextInputLayout).apply {
//                                    isErrorEnabled = true
//                                    error = "Simvollar kamida 60ta bulishi kerak"
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//            produktYuklashButton.setOnClickListener {
//                (yuklash())
//            }
//            val dropdownAdapter =
//                ArrayAdapter(requireContext(), R.layout.dropdown_item, R.id.dropdown_textView,
//                    arrayListOf("UZS", "USD"))
//            chooseCurrencyYuklash.setAdapter(dropdownAdapter)
//
//            optsiyaQoshishRecylerView.apply {
//                setHasFixedSize(true)
//                isNestedScrollingEnabled = false
//                layoutManager = LinearLayoutManager(context)
//                optionsAdapter = OptionsAdapter(arrayList)
//                adapter = optionsAdapter
//            }
//            rasmlarContainer.setOnClickListener {
//                presentFragmentRemoveLast(requireContext(), RasmYuklashFragment(), false)
//            }
//        }
//    }
//
//    class PhotosAdapter(private val photos: ArrayList<String>) :
//        RecyclerView.Adapter<PhotosAdapter.ViewHolder>() {
//        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//            lateinit var imageView: ImageView
//            lateinit var closeImage: ImageView
//
//            init {
//                if (itemView is FrameLayout) {
//                    imageView = itemView.findViewById(R.id.photo_yuklash)
//                    closeImage = itemView.findViewById(R.id.delete_photo_yuklash)
//                }
//            }
//        }
//
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//            val layoutInflater = LayoutInflater.from(parent.context)
//            val view: View
//            if (viewType == 0) {
//                view = layoutInflater.inflate(R.layout.product_yuklash_photos_item, parent, false)
//            } else {
//                view = ImageView(parent.context).apply {
//                    setOnClickListener {
//                        presentFragmentRemoveLast(context, RasmYuklashFragment(), false)
//                    }
//                    setImageResource(R.drawable.photo_add)
//                    colorFilter =
//                        PorterDuffColorFilter(Color.rgb(41, 204, 5), PorterDuff.Mode.SRC_IN)
//                    layoutParams = GridLayoutManager.LayoutParams(120, 120)
//                }
//            }
//            return ViewHolder(view).apply {
//                if (viewType == 0) {
//                    closeImage.setOnClickListener {
//                        photos.removeAt(adapterPosition)
//                        notifyDataSetChanged()
//                    }
//                }
//            }
//        }
//
//        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//            if (getItemViewType(position) == 0) {
//                Glide.with(FirebaseApp.getInstance().applicationContext).asBitmap()
//                    .load(photos[position]).into(object :
//                    SimpleTarget<Bitmap>() {
//                    override fun onResourceReady(
//                        resource: Bitmap,
//                        transition: Transition<in Bitmap>?
//                    ) {
//
//                        //      val bitmap = BitmapFactory.decodeFile(filePath)
//                        holder.imageView.setImageBitmap(resource)
//                    }
//                })
//            }
//        }
//
//        override fun getItemViewType(position: Int): Int {
//            return if (photos.size == position) 1 else 0
//        }
//
//        override fun getItemCount(): Int {
//            return photos.size + 1
//        }
//
//    }
//
//    class OptionsAdapter(val arrayList: ArrayList<Specification>) :
//        RecyclerView.Adapter<OptionsAdapter.ViewHolder>() {
//        private var hasViewForFocus = false
//
//        class ViewHolder(itemView: View, arrayList: ArrayList<Specification>) :
//            RecyclerView.ViewHolder(itemView) {
//            lateinit var titleEditText: EditText
//            lateinit var valueEditText: EditText
//
//            init {
//                if (itemView is ConstraintLayout) {
//                    titleEditText = itemView.findViewById(R.id.optionName_editText)
//                    titleEditText.apply {
//                        doOnTextChanged { text, start, before, count ->
//                            if (text!!.isNotEmpty()) {
//                                arrayList[adapterPosition].name = text.toString()
//                            }
//                        }
//                    }
//                    valueEditText = itemView.findViewById(R.id.option_value_editText)
//                    valueEditText.apply {
//                        doOnTextChanged { text, start, before, count ->
//                            if (text!!.isNotEmpty()) {
//                                arrayList[adapterPosition].value = text.toString()
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//            val layoutInflater = LayoutInflater.from(parent.context)
//            val view: View
//            if (viewType == 0) {
//                view =
//                    layoutInflater.inflate(R.layout.optisya_qoshish_button, parent, false).apply {
//                        setOnClickListener {
//                            arrayList.add(Specification())
//                            hasViewForFocus = true
//                            notifyDataSetChanged()
//                        }
//                    }
//            } else {
//                view = layoutInflater.inflate(R.layout.optsiya_item, parent, false)
//            }
//            return ViewHolder(view, arrayList)
//        }
//
//        @RequiresApi(Build.VERSION_CODES.KITKAT)
//        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//            if (getItemViewType(position) == 1) {
//                holder.apply {
//                    if (position == arrayList.size - 1 && hasViewForFocus) {
//                        (holder.itemView.context as MainActivity).showKeyboard(titleEditText)
//                        hasViewForFocus = false
//                    }
//                    titleEditText.text.clear()
//                    valueEditText.text.clear()
//
//                    titleEditText.text.insert(0, arrayList[position].name)
//                    valueEditText.text.insert(0, arrayList[position].value)
//                }
//            }
//        }
//
//        override fun getItemViewType(position: Int): Int {
//            return if ((arrayList.size) == position) 0 else 1
//        }
//
//        override fun getItemCount(): Int {
//            return arrayList.size + 1
//        }
//    }
//}
//class Specification {
//    var id = SPECIFICATION_ID
//    var name: String = ""
//    var value:String = ""
//
//    constructor()
//
//    constructor(id: Int,name: String,value:String){
//        this.id = id
//        this.name = name
//        this.value = value
//    }
// }
