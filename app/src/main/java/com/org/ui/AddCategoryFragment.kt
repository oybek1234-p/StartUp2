package com.org.ui

import com.ActionBar.log
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.market.R
import com.example.market.databinding.FragmentAddNewCategoryBinding
import com.org.market.*
import com.org.net.models.Category
import com.org.ui.actionbar.BaseFragment
import com.org.ui.actionbar.showError
import com.org.ui.components.load
import com.org.ui.components.visibleOrGone

class AddCategoryFragment(val category: Category = Category(),val onAdded: (cat: Category)->Unit = {}) :
    BaseFragment<FragmentAddNewCategoryBinding>(R.layout.fragment_add_new_category) {
    private var photoUri = ""
    private var isUpdate = category.id.isNotEmpty()

    override fun onCreateView(binding: FragmentAddNewCategoryBinding) {
        actionBar.apply {
            title = "Create category"
            addMenuItem(0, R.drawable.ic_done).setOnClickListener {
                addCategory()
            }
        }
        binding.apply {
            photoView.setOnClickListener {
                presentFragment(GalleryFragment({
                    photoUri = it[0]
                    updatePhotoView()
                }, 1).apply { if(photoUri.isNotEmpty()) checkedImages.add(photoUri) }, false)
            }
            updatePhotoView()
            editText.append(category.name)
        }
    }

    fun addCategory() {
        requireBinding().apply {
            val photo = photoUri.ifEmpty { category.photo }
            if (photo.isEmpty()) {
                photoView.showError()
                return
            }
            val name = editText.text.toString()
            if (name.isEmpty()) {
                editText.showError()
                return
            }
            progressBar.visibleOrGone(true)
            category.apply {
                if (!isUpdate) {
                    id = newId()
                }
                this.name = name
                this.photo = photo
                DataController.addCategory(this)
            }
            closeLastFragment()
            onAdded(category)
            uploadImageFromPath(photo, null) {
                if (it == null) {
                    toast("No internet connection!")
                    return@uploadImageFromPath
                }
                category.photo = it
                progressBar.visibleOrGone(false)

                DataController.uploadCategory(category, null)
            }
        }
    }

    fun updatePhotoView() {
        requireBinding().photoView.apply {
            val url = photoUri.ifEmpty { category.photo }
            if (url.isEmpty()) {
                dp(12f).let {
                    setPadding(it, it, it, it)
                }
                setImageResource(R.drawable.msg_gallery)
            } else {
                setPadding(0, 0, 0, 0)
                load(url, circleCrop = true, thumbnail = true)
            }
        }
    }
}