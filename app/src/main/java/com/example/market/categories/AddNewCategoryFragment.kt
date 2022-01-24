package com.example.market.categories
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.market.*
import com.example.market.binding.inflateBinding
import com.example.market.binding.load
import com.example.market.databinding.CategoryOptionItemBinding
import com.example.market.databinding.ChooseCategoryOptionsLayoutBinding
import com.example.market.databinding.FragmentAddNewCategoryBinding
import com.example.market.navigation.bottomNavVisiblity
import com.example.market.recycler.RecyclerItemClickListener
import com.example.market.recycler.databoundrecycler.DataBoundAdapter
import com.example.market.recycler.databoundrecycler.DataBoundViewHolder
import com.example.market.utils.AndroidUtilities
import com.example.market.utils.uploadImageFromPath
import com.example.market.viewUtils.CubicBezierInterpolator
import com.example.market.viewUtils.toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

enum class ProductOption {
    COLOR,
    BRAND,
    SIZE,
    DATE
}

data class ProductOptionData(var id: ProductOption,var photo: String,var title: String)
object ProductOptionsList {
    val list = hashMapOf(
        Pair(ProductOption.BRAND,ProductOptionData(ProductOption.BRAND,"https://ik.imagekit.io/startup/brand_MtI4IhU1y.png","Brand")),
        Pair(ProductOption.COLOR,ProductOptionData(ProductOption.COLOR,"https://ik.imagekit.io/startup/color_NVJTrqmbX.png","Color")),
        Pair(ProductOption.SIZE,ProductOptionData(ProductOption.SIZE,"https://ik.imagekit.io/startup/size_v5QekEkf0.png","Size")),
        Pair(ProductOption.DATE, ProductOptionData(ProductOption.DATE,"https://ik.imagekit.io/startup/da_nOTyefqAS.png","Date"))
        )
}

class Category {
    var id = ""
    var name = ""
    var photo = ""
    var parentId = ""
    var productsCount = 0
    var options = listOf(ProductOption.BRAND)
}
class aa{
    init {
        val mainCategoryId = "kkkkkoooni"

    }
}
class AddNewCategoryFragment(var category: Category = Category(),val newCategory: (newCategory: Category) -> Unit) : BaseFragment() {
    private var binding: FragmentAddNewCategoryBinding?=null
    private var optionsAdapter: CategoryOptionsAdapter?=null
    private var newPhotoPath = ""

    private fun createCategory() {
        binding?.let {
            val newName = it.categoryNameEditTextView.text.toString()
            val newPhotoPath = newPhotoPath
            val newOptions = optionsAdapter?.dataList

            var changed = false
            var photoChanged = false
            if (newName.isNotEmpty()) {
                if (newName!=category.name){
                    category.name = newName
                    changed = true
                }
            } else {
                toast("Please fill category name")
                return
            }
            if (newPhotoPath.isEmpty()) {
                if (category.photo.isEmpty()) {
                    toast("Upload category photo!")
                    return
                }
            } else {
                category.photo = newPhotoPath
                photoChanged = true
                changed = true
            }
            if (newOptions?.isEmpty() == true) {
                showOptionsFragment()
                toast("Add at least one option")
                return
            }
            newOptions?.apply {
                val newSize = size
                val oldSize = category.options.size

                if (newSize!=oldSize) {
                    category.options = newOptions
                    changed = true
                } else {
                    if (newSize == 1) {
                        if (getOrNull(0) != category.options.getOrNull(0)) {
                            category.options = newOptions
                            changed = true
                        }
                    }
                }
            }
            if (category.id.isEmpty()) {
                category.id = System.currentTimeMillis().toString()
            }
            val uploadRunnable = Runnable {
                category.name = category.name.lowercase(Locale.getDefault())
                uploadCategory(category,object : Result {
                    override fun onSuccess(any: Any?) {
                        toast("Category successfully uploaded")
                    }

                    override fun onFailed() {
                        super.onFailed()
                        toast("Category upload failed")
                    }
                })
            }
            if (photoChanged) {
                uploadImageFromPath(newPhotoPath,{ url->
                    url?.let {
                        category.photo = it
                        DetailsFragment.threadQueue.postRunnable(uploadRunnable)
                    }
                })
            } else {
                if (changed) {
                    DetailsFragment.threadQueue.postRunnable(uploadRunnable)
                } else {
                    closeLastFragment()
                    return
                }
            }
            newCategory(category)
            closeLastFragment()
        }
    }
    private var optionsFragment: AddNewOptionFragment?=null

    fun showOptionsFragment() {
        fragmentController?.let { controller ->
            fragmentManager?.let { manager->
                if (optionsFragment!=null&&optionsFragment!!.isVisible) {
                    return
                }
                optionsFragment = AddNewOptionFragment(ArrayList<ProductOption>().apply {
                    optionsAdapter?.dataList?.let {
                        addAll(it)
                    }
                }) {
                    optionsAdapter?.setDataList(it)
                }.apply {
                    dialog?.setOnDismissListener {
                        optionsFragment = null
                    }
                    controller.removeIfContainsAlready(this)
                    show(manager,null)
                }
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = inflateBinding(container,R.layout.fragment_add_new_category)
        binding?.apply {
            actionBar.apply {
                backButton.setOnClickListener {
                    closeLastFragment()
                }
                titleContainer.gravity = Gravity.START
                title.text = getString(R.string.addnewcategory)
                options.setImageResource(R.drawable.msg_check)
                options.setOnClickListener {
                    createCategory()
                }
            }
            categoryPhotoView.setOnClickListener {
                presentFragmentRemoveLast(RasmYuklashFragment {
                    if (it.isNotEmpty()) {
                        categoryPhotoView.setPadding(0,0,0,0)
                        categoryPhotoView.load(Uri.parse(it).toString(), circleCrop = true)
                        newPhotoPath = it
                    }
                },false)
            }
            getMainActivity().showKeyboard(categoryNameEditTextView)
            addNewOptionView.apply {
                image.setImageResource(R.drawable.menu_add)
                text.text = getString(R.string.addnewoptions)
                root.setOnClickListener {
                    showOptionsFragment()
                }
            }
            optionsRecyclerView.apply {
                adapter = CategoryOptionsAdapter().also {
                    optionsAdapter = it
                    it.setClickListener(object : RecyclerItemClickListener {
                        override fun onClick(position: Int, type: Int) {
                            toast("OptionsClicked")
                        }
                    }) } }
        }
        return binding?.root
    }

    override fun onViewAttachedToParent() {
        super.onViewAttachedToParent()
        bottomNavVisiblity(requireContext(),false)
        setData()
    }

    fun setData() {
        binding?.apply {
            category.apply {
                if (photo.isNotEmpty()) { categoryPhotoView.load(category.photo) }
                categoryNameEditTextView.text?.apply {
                    if (name!=this.toString()) {
                        append(name)
                    }
                }
                optionsAdapter?.setDataList(options)
            }
        }
    }

    class AddNewOptionFragment(var list: ArrayList<ProductOption>,val notify: (list: ArrayList<ProductOption>?) -> Unit) : BottomSheetDialogFragment() {
        private var binding : ChooseCategoryOptionsLayoutBinding?=null
        private var adapter: CategoryOptionsAdapter?=null

        override fun onDestroyView() {
            super.onDestroyView()
            binding = null
            adapter = null
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return BottomSheetDialog(requireContext(),R.style.MyBottomSheetDialogTheme)
        }
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            binding = inflateBinding(container,R.layout.choose_category_options_layout)
            binding?.apply {
                optionsRecyclerView.adapter = CategoryOptionsAdapter(list,notify).also {
                    adapter = it
                    val arrayList = ArrayList<ProductOption>()
                    arrayList.addAll(ProductOptionsList.list.keys)
                    it.setDataList(arrayList)
                }
                applyButtonView.setOnClickListener {
                    dismiss()
                }
            }
            return binding?.root
        }
    }
}

class CategoryOptionsAdapter(var checkedList: ArrayList<ProductOption>?=null,val notify: (list: ArrayList<ProductOption>?) -> Unit = {}): DataBoundAdapter<CategoryOptionItemBinding,ProductOption>(R.layout.category_option_item) {
    override fun onCreateViewHolder(
        viewHolder: DataBoundViewHolder<CategoryOptionItemBinding>?,
        viewType: Int,
    ) {
        if (checkedList!=null) {
            viewHolder?.apply { binding?.apply {
                cardView.apply {
                    foreground = GradientDrawable().apply {
                        cornerRadius = AndroidUtilities.dp(8f).toFloat()
                        if (tag == null) {
                            tag = ValueAnimator.ofFloat(0f,1f).apply {
                                duration = 300
                                addUpdateListener {
                                    val value = it.animatedValue as Float
                                    setStroke((AndroidUtilities.dp(2f) * value).toInt(),checkedColor)
                                    invalidate()
                                }
                            }
                        }
                    }
                }
                root.setOnClickListener {
                    val data = dataList.getOrNull(layoutPosition)
                    data?.let {
                        val checked = !checkedList!!.contains(data)
                        checkedList!!.apply {
                            if (checked) {
                                add(data)
                            } else {
                                remove(data)
                            }
                        }
                        cardView.tag?.let {
                            (it as ValueAnimator).apply {
                                if (checked) {
                                    start()
                                }else {
                                    reverse()
                                }
                            }
                        }
                        notify(checkedList)
                    }
                }
            } } } }
    init {
        automaticallySetData = false
    }
    private val checkedColor =  MyApplication.appContext.getColor(R.color.red_color)

    @SuppressLint("Recycle")
    override fun bindItem(
        holder: DataBoundViewHolder<CategoryOptionItemBinding>?,
        position: Int,
        model: ProductOption?,
    ) {
        holder?.binding?.apply {
            if (checkedList!=null) {
                val tag = cardView.tag
                if (tag!=null&&tag is ValueAnimator) {
                    val checked = checkedList?.contains(model)
                    if (checked == true) {
                        tag.start()
                    }
                }
            }
            data = ProductOptionsList.list[model]
            executePendingBindings()
        }
    }


}