package com.example.market.profile

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.market.*
import com.example.market.auth.LoginFragment
import com.example.market.binding.inflateBinding
import com.example.market.binding.load
import com.example.market.databinding.AlertDialogBinding
import com.example.market.databinding.FragmentProfileSellerBinding
import com.example.market.databinding.ProfileRegisterLayoutBinding
import com.example.market.home.HomeListAdapter
import com.example.market.home.VIEW_TYPE_PRODUCT_SIMPLE
import com.example.market.messages.MessagesFragment
import com.example.market.models.MESSAGE_TYPE_LIKE
import com.example.market.models.MESSAGE_TYPE_SUBSCRIBE
import com.example.market.models.MESSAGE_TYPE_SUBSCRIPTION
import com.example.market.models.Product
import com.example.market.navigation.bottomNavVisiblity
import com.example.market.recycler.EndlessRecyclerViewScrollListener
import com.example.market.recycler.RecyclerItemClickListener
import com.example.market.utils.AndroidUtilities
import com.example.market.utils.FirestorePaging
import com.example.market.viewUtils.DIALOG_ANIMATION_ALERT_DIALOG
import com.example.market.viewUtils.PopupDialog
import com.example.market.viewUtils.PopupWindowLayout
import com.example.market.viewUtils.toast
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.Query
import java.util.*

class ProfileFragmentSeller(val userId: String? = null) : BaseFragment<FragmentProfileSellerBinding>(R.layout.fragment_profile_seller) {

    override fun onViewAttachedToParent() {
        updateRegisterLayout()
        createPaging()
        paging?.observe(viewLifecycleOwner,observer)

        if (userId != null && currentUser!=null) {
            subscribedMutableLiveData.observe(viewLifecycleOwner,subscribeObserver)
            subscribedMutableLiveData.postValue(checkSubscribed(userId))

            subscribedListener = checkSubscribed(currentUser!!.id, userId, object : ResultCallback<Boolean> {
                    override fun onSuccess(result: Boolean?) {
                        subscribedMutableLiveData.postValue(result ?: false)
                    }
                })

        } else if (userId==null&& currentUser!=null) {
            subscirbeButton(2)
        } else {
            subscirbeButton(1)
        }
    }

    override fun onViewDetachedFromParent() {
        paging?.removeObserver(observer)
        if (userId!=null) {
             subscribedMutableLiveData.removeObserver(subscribeObserver)

        subscribedListener?.let { v -> currentUser?.let {
            getSubscriptionsReference(userId, it.id).removeEventListener(v)
            }
        }
        }
    }

    override fun canBeginSlide(): Boolean {
        return false
    }

    private var registerLayout: ProfileRegisterLayoutBinding? = null

    override fun onDestroyView() {
        super.onDestroyView()
        registerLayout = null
        paging?.clear()
        paging = null
        productsAdapter = null
        user = null
        buttonsContainer = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId?.let {
            getUser(it, object : ResultCallback<User> {
                override fun onSuccess(result: User?) {
                    user = result
                }
            })
        }
    }

    private fun createPaging(){
        if (paging==null||paging!=null&&paging!!.isCleared) {
            if (currentUser != null || userId != null) {
                val query = getProductsReference().whereEqualTo("sellerId", userId ?: currentUser!!.id).orderBy("id",Query.Direction.DESCENDING)
                paging = FirestorePaging(Product::class.java, query, "id")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
        binding: FragmentProfileSellerBinding
    ) {
        if (userId == null) {
            bottomNavVisiblity(requireContext(), true)
        }
        createView()
    }
    private var productsAdapter: HomeListAdapter? = null
    private var paging: FirestorePaging<Product>? = null
    private var buttonsContainer: LinearLayout? = null

    private var user: User? = null
        set(value) {
            if (value != null) {
                field = value
                setData(value)
            }
        }

    private fun updateRegisterLayout() {
        binding.apply {
            if (currentUser == null && userId == null) {
                appBarLayout.visibility = View.GONE
                recyclerView.visibility = View.GONE
                if (registerLayout == null) {
                    registerLayout = inflateBinding(container, R.layout.profile_register_layout)

                    registerLayout?.apply {
                        registerButton.setOnClickListener {
                            presentFragmentRemoveLast(LoginFragment(), false)
                        }
                        container.addView(this.root)
                    }
                }

            } else {
                container.removeView(registerLayout?.root)
                registerLayout = null
                appBarLayout.visibility = View.VISIBLE
                recyclerView.visibility = View.VISIBLE
            }
        }
    }
    companion object {
        private var space = AndroidUtilities.dp(2f)

        private val itemDecoration = object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                super.getItemOffsets(outRect, view, parent, state)

                val spanIndex = (view.layoutParams as GridLayoutManager.LayoutParams).spanIndex
                outRect.apply {
                    when(spanIndex) {
                        0-> {
                            left = 0
                            top = space
                            right = 0
                            bottom = 0

                        }
                        1-> {
                            left = space
                            top = space
                            right = 0
                            bottom = 0
                        }
                        2-> {
                            left = space
                            right = 0
                            bottom = 0
                            top = space
                        }
                    }
                }
            }
        }
    }

    private fun createView() {
        binding?.apply {
            if (userId == null) {
                getMainActivity().bottomNavigationView.selectedItemId = R.id.profile
            }

            actionBar.apply {
                title.text = "Profile"
                root.elevation = 0f
                if (userId != null) {
                    backButton.visibility = View.VISIBLE
                    backButton.setOnClickListener { closeLastFragment() }
                } else {
                    backButton.visibility = View.GONE
                }

                options.setOnClickListener {
                    PopupWindowLayout(requireContext()).apply {
                        addItem(0, "Sign out", R.drawable.preview_back) {
                            signOutIn()
                        }
                        PopupDialog(this).show(requireView(),
                            0,
                            MyApplication.displaySize.first - measuredWidth,
                            AndroidUtilities.dp(8f),
                            true)
                    }
                }
            }
            updateRegisterLayout()
            val id = userId ?: (currentUser?.id ?: "")

            subscribersContainer.setOnClickListener {
                presentFragmentRemoveLast(MessagesFragment(MESSAGE_TYPE_SUBSCRIBE, false,id), false)
            }

            subscriptionsContainer.setOnClickListener {
                presentFragmentRemoveLast(MessagesFragment(MESSAGE_TYPE_SUBSCRIPTION, false,id), false)
            }

            likesContainer.setOnClickListener {
                presentFragmentRemoveLast(MessagesFragment(MESSAGE_TYPE_LIKE, false,id), false)
            }

            userPhoto.setOnClickListener {
                if (userId == null) {
                    presentFragmentRemoveLast(RasmYuklashFragment {
                        Runnable {
                            setPhoto(it)
                            updateUserPhoto(it,object : Result{
                                override fun onSuccess(any: Any?) {
                                    toast("Photo uploaded successfully")
                                }
                            })
                        }.run()
                    }, false)
                }
            }

            editUserInfoButton.apply {
                if (userId != null) {
                    visibility = View.GONE
                }
            }.setOnClickListener {
                presentFragmentRemoveLast(EditUserInfoFragment(), false)
            }

            if (userId == null) {
                currentUserLiveData.observe(viewLifecycleOwner
                ) { d ->
                    d?.let { setData(it) }
                }
            } else {
                user?.let { setData(it) }
            }

            if (userId!=null) {
             subscribeButton.setOnClickListener {
                    if (isSubscribing) return@setOnClickListener
                    if (currentUser != null) {
                        user?.let { us ->
                            subscribedMutableLiveData.value?.let {
                                val subscribe = !it
                                us.subscribers =
                                    if (subscribe) us.subscribers + 1 else us.subscribers - 1
                                user = us
                                subscribedMutableLiveData.postValue(subscribe)
                                isSubscribing = true
                                subscibeToStore(us.id, subscribe, object : Result {
                                    override fun onSuccess(any: Any?) {
                                        isSubscribing = false
                                    }

                                    override fun onFailed() {
                                        super.onFailed()
                                        isSubscribing = false
                                    }
                                })
                            }
                        }
                    } else {
                        presentFragmentRemoveLast(LoginFragment(), false)
                    }
            }
            }

            recyclerView.apply {
                clipChildren = false
                clipToPadding = false
                productsAdapter = HomeListAdapter(1.2f).also {
                    adapter = it
                    paging?.pagingCallback = object : FirestorePaging.PagingCallback{
                        override fun onFinishedLoadMore() {
                            it.loading = false
                        }

                        override fun onLoadMore() {
                            it.loading = true
                        }
                    }
                    it.setOnItemClickListener(object : RecyclerItemClickListener {
                        override fun onClick(position: Int, type: Int) {
                            productsAdapter?.apply {
                                currentList[position]?.let {
                                    presentFragmentRemoveLast(DetailsFragment(it),
                                        removeLast = false)
                                }
                            }
                        }
                    })
                }

                addItemDecoration(itemDecoration)
                layoutManager =
                    GridLayoutManager(requireContext(),3).also {
                        addOnScrollListener(object : EndlessRecyclerViewScrollListener(it) {
                            override fun onLoadMore(
                                page: Int,
                                totalItemsCount: Int,
                                view: RecyclerView?,
                            ) {
                                Toast.makeText(context,"Load more",Toast.LENGTH_SHORT).show()
                                paging?.loadMore()
                            }
                        })
                    }
            }
        }
    }

    private var subscribeObserver = Observer<Boolean> {
         t ->
            subscirbeButton(if (t == null) 2 else if (t) 0 else 1)
            t?.let {
                if (userId != null) {
                    addSubscriber(userId, it)
                }
            }

    }

    private var observer =
        Observer<ArrayList<Product>> { t ->
            t?.let {
                it.forEach {
                    it.type = VIEW_TYPE_PRODUCT_SIMPLE
                }
                productsAdapter?.submitList(it.toMutableList())
            }
        }

    private var isSubscribing = false
    private var subscribedListener: ValueEventListener? = null

    private fun subscirbeButton(subscribed: Int) {
        binding?.subscribeButton?.apply {
            if (subscribed == 2) {
                visibility = View.GONE
            } else {
                visibility = View.VISIBLE
                text =
                    if (subscribed == 0) "Subscribed" else if (subscribed == 1) "Subscribe" else null
                setTextColor(if (subscribed == 0) Color.DKGRAY else Color.WHITE)
                backgroundTintList = ColorStateList.valueOf(if (subscribed == 0) Color.rgb(242,
                    242,
                    242) else requireContext().getColor(R.color.colorRed))
            }
        }
    }

    private var subscribedMutableLiveData = MutableLiveData<Boolean>(null)

    private fun signOutIn() {
        val alertDialog = inflateBinding<AlertDialogBinding>(null, R.layout.alert_dialog)
        alertDialog.apply {
            val popupWindow = PopupWindowLayout(requireContext()).apply { addView(alertDialog.root) }
            val popupDialog = PopupDialog(popupWindow, DIALOG_ANIMATION_ALERT_DIALOG)
            data = Alert("Log out", "Do you realy want to sign out?", "CANCEL", "SIGN OUT", { popupDialog.dismiss() },
                {
                    signOut()
                    paging?.clear()
                    getMainActivity().updateBottomNav()
                    createView()
                    popupDialog.dismiss()
                }, iconResource = R.drawable.search_users)
            popupDialog.show(requireView(), Gravity.CENTER, 0, 0, true)
        }
    }

    private fun setPhoto(photo: String) {
        binding?.userPhoto?.load(photo, circleCrop = true)
    }

    private fun setData(user: User) {
        user.let {
            binding?.apply {
                setPhoto(it.photo)
                likes.text = it.likes.toString()
                subscribers.text = it.subscribers.toString()
                subscriptions.text = it.subscriptions.toString()
                name.text = it.name
                bio.text = it.about
            }
        }
    }


}
