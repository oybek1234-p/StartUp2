package com.example.market.fragments

//import android.os.Bundle
//import android.view.Gravity
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import android.widget.FrameLayout
//import android.widget.LinearLayout
//import androidx.core.view.isVisible
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.example.market.*
//import com.example.market.adapters.BannersAdapter
//import com.example.market.binding.inflateBinding
//import com.example.market.binding.playPopupAnimation
//import com.example.market.binding.visibleOrGone
//import com.example.market.databinding.AdvertisingFragmentBinding
//import com.example.market.databinding.EmptyScreenBinding
//import com.example.market.models.Banner
//import com.example.market.models.Empty
//import com.example.market.navigation.bottomNavVisiblity
//import com.example.market.products.NotificationCenter
//import com.example.market.utils.LottieUrls
//import com.example.market.viewUtils.toast
//
//class AdvertisingFragment: BaseFragment<AdvertisingFragmentBinding>(R.layout.advertising_fragment) {
//
//    private var emptyBinding: EmptyScreenBinding?=null
//    private var listAdapter: BannersAdapter?=null
//    private var showEmpty = false
//    private var requestId = -1
//    private var banners: ArrayList<Banner> = ArrayList()
//    private var isBannersGot = false
//
//    fun loadBanners() {
//        binding.apply {
//            if (progressBar.isVisible) {
//                return
//            }
//            if (!isBannersGot) {
//                progressBar.visibleOrGone(true)
//                requestId = getBanners(object : ResultCallback<ArrayList<Banner>?> {
//                    override fun onSuccess(result: ArrayList<Banner>?) {
//                        isBannersGot = true
//                        progressBar.visibleOrGone(false)
//
//                        banners.apply {
//                            clear()
//                            result?.let { addAll(it) }
//
//                            showEmpty(isEmpty())
//                        }
//                        listAdapter?.setDataList(banners)
//                    }
//
//                    override fun onFailed() {
//                        super.onFailed()
//                        toast("Failed")
//                        isBannersGot = true
//                        loadBanners()
//                    }
//                })
//            } else {
//                progressBar.visibleOrGone(false)
//                showEmpty(banners.isEmpty())
//            }
//            listAdapter?.setDataList(banners)
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        cancellRequest(requestId)
//        emptyBinding = null
//        listAdapter = null
//    }
//
//    override fun onViewAttachedToParent() {
//        super.onViewAttachedToParent()
//        bottomNavVisiblity(requireContext(),false)
//        loadBanners()
//    }
//
//    fun openAddAdFragment(banner: Banner?=null) {
//        presentFragmentRemoveLast(AddAdFragment(onNewBannerAdded = {
//            banners.apply {
//                if (banner == it) {
//                    val index = indexOf(banner)
//                    set(index,it)
//                    listAdapter?.notifyItemChanged(index)
//                } else {
//                    add(0,it)
//                    listAdapter?.notifyItemInserted(0)
//                }
//            }
//        }).apply {
//                 if (banner!=null) {
//                     this.banner = banner
//                 }
//        },false)
//    }
//
//    fun showEmpty(show: Boolean) {
//        if (showEmpty == show) {
//            return
//        }
//        showEmpty = show
//        if (emptyBinding==null) {
//            if (!show) {
//                return
//            }
//            emptyBinding = inflateBinding<EmptyScreenBinding>(null,R.layout.empty_screen).apply {
//                titleView.text = getString(R.string.banner_empty)
//                subtitleView.text = getString(R.string.add_new_banner)
//
//                addItemButton.apply {
//                    text = context.getString(R.string.new_banner)
//                    setOnClickListener {
//                        openAddAdFragment()
//                    }
//                }
//                lottieView.setAnimationFromUrl(LottieUrls.advertising)
//            }
//        }
//
//        emptyBinding!!.root.apply {
//            val container = (binding.root as FrameLayout)
//            if (show) {
//                container.addView(this, FrameLayout.LayoutParams(
//                        FrameLayout.LayoutParams.WRAP_CONTENT,
//                        FrameLayout.LayoutParams.WRAP_CONTENT
//                    ).apply {
//                    gravity = Gravity.CENTER
//                    })
//
//                playPopupAnimation()
//            } else {
//                container.removeView(this)
//            }
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?,
//        binding: AdvertisingFragmentBinding,
//    ) {
//        binding.apply {
//            actionBar.apply {
//                backButton.setOnClickListener {
//                    closeLastFragment()
//                }
//                title.text = getString(R.string.advertising)
//
//                options.apply {
//                    setImageResource(R.drawable.msg_addbot)
//                    setOnClickListener {
//                        openAddAdFragment()
//                    }
//                }
//            }
//
//            recyclerView.apply {
//                adapter = BannersAdapter(this@AdvertisingFragment) {
//                    if (it == 0) {
//                        showEmpty(true)
//                    }
//                }.also {
//                    listAdapter = it
//                }
//                layoutManager = LinearLayoutManager(context)
//            }
//        }
//    }
//
//}