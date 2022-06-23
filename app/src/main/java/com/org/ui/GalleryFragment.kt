package com.org.ui
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.net.Uri
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.market.R
import com.example.market.databinding.FragmentRasmYuklashBinding
import com.org.market.PermissionController
import com.org.market.dp
import com.org.market.runOnUiThread
import com.org.market.toast
import com.org.ui.actionbar.BaseFragment
import com.org.ui.components.ImageViewLoader
import com.org.ui.components.RecyclerListView
import com.org.ui.components.load
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.jar.Manifest

/**
 * Load photos
 */
class GalleryFragment(val onDone:(photos: ArrayList<String>) -> Unit,val photoLimit:Int = Int.MAX_VALUE): BaseFragment<FragmentRasmYuklashBinding>(R.layout.fragment_rasm_yuklash){
    var imagesUri = arrayListOf<String>()
    private var listAdapter: PhotosAdapter?=null
    var checkedImages = arrayListOf<String>()

    @SuppressLint("Range")
    fun getGalleryPhotos() {
        requireActivity().lifecycleScope.launch (Dispatchers.IO){
            val cursor = requireActivity().contentResolver!!.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.MediaColumns.DATA),null,null,null)!!
            while (cursor.moveToNext()) {
                val imageUri = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA))
                imagesUri.add(imageUri)
            }
            cursor.close()
            runOnUiThread({
                listAdapter?.notifyDataSetChanged()
            })
        }
    }

    override fun onResume() {
        super.onResume()
        listAdapter?.notifyDataSetChanged()
    }

    override fun onCreateView(binding: FragmentRasmYuklashBinding) {
        actionBar.title = "Photos"
        binding.apply {
            recyclerView.apply {
                adapter = PhotosAdapter {
                    doneButton.text = checkedImages.size.toString() + " Done"
                }.also {  listAdapter = it }
                layoutManager = GridLayoutManager(context,3)
                addItemDecoration(object : RecyclerView.ItemDecoration() {
                    override fun getItemOffsets(
                        outRect: Rect,
                        view: View,
                        parent: RecyclerView,
                        state: RecyclerView.State
                    ) {
                        super.getItemOffsets(outRect, view, parent, state)
                        val params = view.layoutParams as GridLayoutManager.LayoutParams
                        if (params.spanIndex == 0) {
                            outRect.right = dp(2f)
                        } else if (params.spanIndex == 1) {
                            outRect.right = dp(2f)
                        }
                        outRect.top = dp(2f)
                    }
                })
                setHasFixedSize(true)
            }
            PermissionController.getInstance().requestPermissions(context(),1, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            ,object : PermissionController.PermissionResult {
                    override fun onDenied() {
                        closeLastFragment()
                    }

                    override fun onGranted() {
                        getGalleryPhotos()
                    }
            })
            doneButton.setOnClickListener {
                val checkedImages = checkedImages
                if (checkedImages.isEmpty()) {
                    toast("Please choose at least one photo")
                    return@setOnClickListener
                }
                onDone(checkedImages)
                closeLastFragment()
            }
        }
    }

    class PhotoItem(context: Context): FrameLayout(context) {
        init {
            layoutParams = ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, dp(120f))
        }
        var photoView = ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            this@PhotoItem.addView(this,LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT))
        }
        var checkBox = CheckBox(context).apply {
            isClickable = false
            isFocusable = false
            isFocusableInTouchMode = false
            isFocusableInTouchMode = false
            this@PhotoItem.addView(this,LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT).apply {
                dp(8f).let {
                    setMargins(it,it,it,it)
                }
                setGravity(Gravity.TOP and  Gravity.END)
            })
        }
        fun setPhoto(photoPath: String) {
            Glide.with(context).load(photoPath).override(width,height).into(photoView)
        }
    }

    inner class PhotosAdapter(val onChanged: ()->Unit): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun getItemCount(): Int {
            return imagesUri.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            holder.apply {
                val image = imagesUri.get(layoutPosition)
                val checked = checkedImages.contains(image)
                (itemView as PhotoItem).apply {
                    setPhoto(image)
                    checkBox.isChecked = checked
                }
            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): RecyclerView.ViewHolder {
            PhotoItem(parent.context).apply {
                val viewHolder = object : RecyclerView.ViewHolder(this) {}
                setOnClickListener {

                    val check = !checkBox.isChecked.also {
                        val checkImage = imagesUri[viewHolder.layoutPosition]
                        if (!it) {
                            if (checkedImages.size == photoLimit) {
                                toast("You can choose only $photoLimit photos")
                                return@setOnClickListener
                            }
                            checkedImages.add(checkImage)
                        } else {
                            checkedImages.remove(checkImage)
                        }
                    }
                    checkBox.isChecked = check
                    onChanged()
                }
                return viewHolder
            }
        }
    }
}