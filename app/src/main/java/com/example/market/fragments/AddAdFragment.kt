package com.example.market.fragments

//import android.net.Uri
//import android.os.Bundle
//import android.view.Gravity
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.LinearLayout
//import androidx.core.widget.doOnTextChanged
//import com.example.market.*
//import com.example.market.binding.load
//import com.example.market.binding.visibleOrGone
//import com.example.market.databinding.AddAdFragmentBinding
//import com.example.market.databinding.ButtonStyleMinBinding
//import com.example.market.models.Banner
//import com.example.market.models.PHOTO_BANNER
//import com.example.market.navigation.bottomNavVisiblity
//import com.example.market.utils.getDrawable
//import com.example.market.viewUtils.PopupDialog
//import com.example.market.viewUtils.PopupWindowLayout
//import com.example.market.viewUtils.toast
//
//class AddAdFragment(var banner: Banner = Banner(),var onNewBannerAdded: (banner: Banner) -> Unit) : BaseFragment<AddAdFragmentBinding>(R.layout.add_ad_fragment) {
//
//    private var addPhotoBinding: ButtonStyleMinBinding?=null
//    private var photoView: ImageView?=null
//
//    fun openInfoFragment() {}
//
//    override fun onViewAttachedToParent() {
//        super.onViewAttachedToParent()
//        bottomNavVisiblity(getMainActivity(),false)
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?,
//        binding: AddAdFragmentBinding
//    ) {
//        binding.apply {
//            actionBar.apply {
//                backButton.setOnClickListener {
//                    closeLastFragment()
//                }
//                title.text = getString(R.string.add_ad)
//
//                options.setOnClickListener {
//                    val popupWindowLayout = PopupWindowLayout(requireContext())
//                    popupWindowLayout.addItem(0,"Info",R.drawable.profile_info) {
//                        openInfoFragment()
//                    }
//                    val popupDialog = PopupDialog(popupWindowLayout)
//                    showPopupDialog(popupDialog,requireView(),Gravity.END,0,0,true)
//                }
//            }
//
//            addPhotoButton.apply {
//                addPhotoBinding = this
//
//                image.setImageResource(R.drawable.msg_addphoto)
//                text.text = getString(R.string.add_photo)
//
//                root.setOnClickListener {
//                    presentFragmentRemoveLast(RasmYuklashFragment {
//                            if (it.isNotEmpty()) {
//                                banner.photo = it
//                                updatePhotoView()
//                            } },
//                        false)
//                }
//
//                photoView = ImageView(requireContext()).apply {
//                    scaleType = ImageView.ScaleType.CENTER_CROP
//                }
//            }
//
//            bannerNameEditText.doOnTextChanged { text, start, before, count ->
//                banner.name = text.toString()
//            }
//
//            addButton.setOnClickListener {
//                saveBanner()
//            }
//
//            banner.apply {
//                val isEdit = id.isNotEmpty()
//
//                updatePhotoView()
//                bannerNameEditText.setText(name)
//
//                sellerPhotoView.load(
//                    if (isEdit) user.photo else currentUser().photo,
//                    placeHolder = getDrawable(R.drawable.shortcut_user)
//                )
//                sellerNameView.text = if (isEdit) user.name else currentUser().name
//
//                bannerTypeChip.apply {
//                    icon.visibleOrGone(false)
//                    text.text = if (isEdit) type else PHOTO_BANNER
//                }
//            }
//        }
//    }
//
//    fun checkNotEmpty() : Boolean {
//        banner.apply {
//            return (photo.isNotEmpty() && name.isNotEmpty()).also {
//                if (it) {
//                    if (id.isEmpty()) {
//                        id = System.currentTimeMillis().toString()
//                    }
//
//                    if (user.id.isEmpty()) {
//                        if (checkCurrentUser()) {
//                            val currentUser = currentUser!!
//                            user.id = currentUser.id
//                            user.photo = currentUser.photo
//                            user.name = currentUser.name
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    fun saveBanner() {
//        if (checkNotEmpty()){
//            addBanner(banner,null)
//            onNewBannerAdded(banner)
//            closeLastFragment()
//        } else {
//            toast("Banner name and photo must be provided! ")
//        }
//    }
//
//    fun updatePhotoView() {
//        binding.container.apply {
//            val childView = getChildAt(1)
//            val photoButton = addPhotoBinding!!.root
//            val photoUri = banner.photo
//
//            if (photoUri.isEmpty()) {
//                if (childView == photoView) {
//                    removeView(childView)
//                    addView(photoButton,1)
//                }
//            } else {
//                if (childView == photoButton) {
//                    removeView(photoButton)
//                    addView(photoView,1,
//                        LinearLayout.LayoutParams(
//                            LinearLayout.LayoutParams.MATCH_PARENT,
//                            AndroidUtilities.dp(180f)
//                        ).apply {
//                            val margin = AndroidUtilities.dp(8f)
//                            topMargin = margin
//                        }
//                    )
//                }
//                photoView?.load(Uri.parse(photoUri).toString())
//            }
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        addPhotoBinding = null
//        photoView = null
//    }
//}