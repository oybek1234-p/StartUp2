package com.example.market
//
//import android.Manifest
//import android.graphics.Color
//import android.graphics.Rect
//import android.os.Build
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.recyclerview.widget.RecyclerView
//import android.provider.MediaStore
//import android.widget.ImageView
//import androidx.recyclerview.widget.GridLayoutManager
//import android.widget.CheckBox
//import androidx.annotation.RequiresApi
//import com.bumptech.glide.Glide
//import com.example.market.databinding.FragmentRasmYuklashBinding
//import com.example.market.permission.PermissionController
//import com.example.market.permission.PermissionResult
//import com.example.market.viewUtils.toast
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.launch
//
//class RasmYuklashFragment(var onGetPhoto: ((photo: String) -> Unit?)? = null) : BaseFragment<FragmentRasmYuklashBinding>(R.layout.fragment_rasm_yuklash) {
//    private lateinit var imagesListUri: ArrayList<String>
//    private var mAdapter: RasmYuklashAdapter? = null
//
//    override fun onViewFullyVisible() {
//        PermissionController.getInstance().requestPermissions(
//            requireContext(),
//            8484,
//            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
//            object : PermissionResult {
//                override fun onGranted() {
//                    toast("On granted")
//                    initFragment()
//                }
//
//                override fun onDenied() {
//                    closeLastFragment()
//                }
//            })
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//    }
//
//    private fun initFragment() {
//        getAllImages()
//        binding.rasmniYuklash.setOnClickListener {
//            mAdapter?.apply {
//                if (selectedItems.isNotEmpty()) {
//                    onGetPhoto?.let { it1 -> it1(selectedItems.first()) }
//                }
//                bundleAny = selectedItems
//            }
//            closeLastFragment()
//        }
//    }
//
//    override fun onViewAttachedToParent() {
//        super.onViewAttachedToParent()
//        showBottomNav(false)
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?,
//        binding: FragmentRasmYuklashBinding
//    ) {
//        binding.apply {
//            actionBar.apply {
//                backButton.setOnClickListener {
//                    closeLastFragment()
//                }
//                title.text = getString(R.string.rasm_yuklash)
//            }
//        }
//    }
//
//    private fun getAllImages() {
//        GlobalScope.launch(Dispatchers.IO) {
//            val cursor =
//                requireContext().contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                    arrayOf(MediaStore.MediaColumns.DATA), null, null, null)
//            imagesListUri = ArrayList()
//            while (cursor!!.moveToNext()) {
//                val imagePath: String =
//                    cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA))
//                imagesListUri.add(imagePath)
//            }
//            imagesListUri.reverse()
//            GlobalScope.launch(Dispatchers.Main) {
//                if (imagesListUri.isNotEmpty()) {
//                    binding.galeryImagesRecyclerView.apply {
//                        setHasFixedSize(true)
//                        val edgeSpace = 10
//                        val space = 5
//                        layoutManager = GridLayoutManager(context, 3).apply {
//
//                            addItemDecoration(object : RecyclerView.ItemDecoration() {
//                                override fun getItemOffsets(
//                                    outRect: Rect,
//                                    view: View,
//                                    parent: RecyclerView,
//                                    state: RecyclerView.State,
//                                ) {
//                                    val pos = parent.getChildAdapterPosition(view)
//                                    spanSizeLookup.getSpanIndex(pos, 3).let {
//                                        when (it) {
//                                            0 -> {
//                                                outRect.apply {
//                                                    left = edgeSpace
//                                                    right = space
//                                                    top = edgeSpace
//                                                    bottom = 0
//                                                }
//                                            }
//                                            1 -> {
//                                                outRect.apply {
//                                                    left = space
//                                                    right = space
//                                                    top = edgeSpace
//                                                    bottom = 0
//                                                }
//                                            }
//                                            2 -> {
//                                                outRect.apply {
//                                                    left = space
//                                                    right = edgeSpace
//                                                    top = edgeSpace
//                                                    bottom = 0
//                                                }
//                                            }
//                                            else -> 0
//                                        }
//                                    }
//                                }
//                            })
//
//                        }
//                        mAdapter = RasmYuklashAdapter(object : ChangeButtonColor {
//                            override fun change(enable: Boolean) {
//                                binding.rasmniYuklash.setBackgroundColor(if (enable) Color.GREEN else Color.LTGRAY)
//                            }
//                        }, (context as MainActivity).selectedUriPhotos ?: ArrayList()).apply {
//                            uriList = imagesListUri
//                        }
//                        adapter = mAdapter
//                    }
//                }
//                cursor.close()
//            }
//        }
//
//    }
//
//    class RasmYuklashAdapter(
//        private var changeButtonColor: ChangeButtonColor,
//        var selectedItems: ArrayList<String>,
//    ) : RecyclerView.Adapter<RasmYuklashAdapter.ViewHolder>() {
//        var uriList = ArrayList<String>()
//
//        class ViewHolder(
//            itemView: View,
//            list: ArrayList<String>,
//            selectedItems: ArrayList<String>,
//        ) : RecyclerView.ViewHolder(itemView) {
//            val imageView: ImageView? =
//                itemView.findViewById<ImageView>(R.id.rasm_yuklash_imageView)
//            val checkBox: CheckBox? = itemView.findViewById<CheckBox>(R.id.rasm_yuklash_checkbox)
//
//
//            fun checkSelected(
//                position: Int,
//                uriList: ArrayList<String>,
//                selectedItems: ArrayList<String>,
//            ): Boolean {
//                var isAviable = false
//                for (i in selectedItems) {
//                    if (uriList[position] == i) {
//                        isAviable = true
//                    }
//                }
//                return isAviable
//            }
//        }
//
//        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
//        override fun onCreateViewHolder(
//            parent: ViewGroup,
//            viewType: Int,
//        ): ViewHolder {
//            val itemView = LayoutInflater.from(parent.context)
//                .inflate(R.layout.rasm_yuklash_item, parent, false)
//            return ViewHolder(itemView, uriList, selectedItems).apply {
//                checkBox?.setOnCheckedChangeListener { buttonView, isChecked ->
//
//                    val checkSelected = checkSelected(adapterPosition, uriList, selectedItems)
//                    if (isChecked) {
//                        if (!checkSelected) {
//                            selectedItems.add(uriList[adapterPosition])
//                            if (!imageView!!.isSelected) {
//                                imageView.isSelected = true
//                            }
//                            if (!checkBox.isChecked) {
//                                checkBox.isChecked = true
//                            }
//                        }
//                    } else {
//                        if (checkSelected) {
//                            selectedItems.remove(uriList[adapterPosition])
//                            if (imageView!!.isSelected) {
//                                imageView.isSelected = false
//                            }
//                            if (checkBox.isChecked) {
//                                checkBox.isChecked = false
//                            }
//                        }
//                    }
//                    changeButtonColor.change(selectedItems.size > 0)
//                }
//            }
//        }
//
//        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
//        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//            holder.apply {
//                checkSelected(adapterPosition, uriList, selectedItems).let {
//                    checkBox?.apply {
//                        if (isChecked != it) {
//                            isChecked = it
//                        }
//                    }
//                    imageView?.apply {
//                        if (isSelected != it) {
//                            isSelected = it
//                        }
//                    }
//                }
//                Glide.with(itemView.context).load(uriList[position]).centerCrop().into(imageView!!)
//            }
//        }
//
//        override fun getItemCount(): Int {
//            return uriList.size
//        }
//
//    }
//
//}
//
//interface ChangeButtonColor {
//    fun change(enable: Boolean)
//}
