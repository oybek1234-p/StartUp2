package com.example.market.adapters
//
//import android.graphics.Color
//import android.view.Gravity
//import com.example.market.*
//import com.example.market.binding.load
//import com.example.market.databinding.PhotoBannerAdBinding
//import com.example.market.fragments.AdvertisingFragment
//import com.example.market.models.Banner
//import com.example.market.profile.ProfileFragmentSeller
//import com.example.market.recycler.databoundrecycler.DataBoundAdapter
//import com.example.market.recycler.databoundrecycler.DataBoundViewHolder
//import com.example.market.utils.DateUtils
//import com.example.market.utils.getDrawable
//import com.example.market.viewUtils.*
//
//class BannersAdapter(var fragment: AdvertisingFragment ,var onItemRemoved: (size: Int)-> Unit) : DataBoundAdapter<PhotoBannerAdBinding,Banner>(R.layout.photo_banner_ad){
//
//    var popupDialog: PopupDialog?=null
//    var alertDialog: AlertDialogLayout?=null
//    private var chipPlaceHolder = getDrawable(R.drawable.shortcut_user)
//
//    init {
//        setHasStableIds(false)
//    }
//
//    override fun onCreateViewHolder(
//        viewHolder: DataBoundViewHolder<PhotoBannerAdBinding>,
//        viewType: Int,
//    ) {
//        viewHolder.binding.apply {
//            val activity = fragment.context as MainActivity
//
//            sellerInfoChipView.root.setOnClickListener {
//
//                getItem(viewHolder.layoutPosition)?.let {
//                    presentFragmentRemoveLast(
//                        activity,
//                        ProfileFragmentSeller(it.user.id),
//                        false
//                    )
//                }
//            }
//
//            editButton.setOnClickListener {
//                fragment.openAddAdFragment(getItem(viewHolder.layoutPosition))
//            }
//
//            optionsButton.setOnClickListener {
//                popupDialog = null
//                alertDialog = null
//                val position = viewHolder.layoutPosition
//
//                val popupWindow = PopupWindowLayout(activity)
//                popupWindow.addItem(0,"Delete",R.drawable.chats_delete) {
//
//                    alertDialog = AlertDialogLayout(mRecyclerView!!).apply {
//                        val alert = Alert(
//                            "Delete banner",
//                            "Do you really want delete this banner?",
//                            "CANCEL",
//                            "DELETE",
//                            { dismiss() },
//                            {
//                                getItem(position)?.let {
//                                    dataList.remove(it)
//                                    notifyItemRemoved(position)
//                                    onItemRemoved(dataList.size)
//                                    deleteBanner(it, null)
//                                    dismiss()
//                                }
//                            },iconResource = R.drawable.msg_delete,iconTint = Color.DKGRAY,hasPadding = true
//                        )
//                        showDialog(alert)
//                    }
//                }
//
//                popupDialog = PopupDialog(popupWindow).apply {
//                    val location = IntArray(2)
//                    optionsButton.getLocationInWindow(location)
//                    val windowSize = getWindowSize()
//                    show(
//                        mRecyclerView!!,
//                        Gravity.NO_GRAVITY,
//                        location[0] - (windowSize.first) + optionsButton.width,
//                        location[1],
//                        true,
//                        0.2f
//                    )
//                }
//            }
//        }
//    }
//
//    override fun bindItem(
//        holder: DataBoundViewHolder<PhotoBannerAdBinding>,
//        binding: PhotoBannerAdBinding,
//        position: Int,
//        model: Banner,
//    ) {
//        binding.apply {
//            photoView.load(model.photo)
//            bannerTypeView.text = model.type
//            val views = "${model.viewsCount} views in ${DateUtils.calculateDifference(System.currentTimeMillis(),model.date).days} days"
//            viewsTextView.text = views
//            bannerNameView.text = model.name
//
//            sellerInfoChipView.apply {
//                model.user.apply {
//                    icon.load(photo,placeHolder = chipPlaceHolder)
//                    text.text = name
//                }
//            }
//        }
//    }
//
//}