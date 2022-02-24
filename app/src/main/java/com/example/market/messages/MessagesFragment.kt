package com.example.market.messages

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.market.*
import com.example.market.binding.inflateBinding
import com.example.market.databinding.ButtonStyleMinBinding
import com.example.market.databinding.EmptyScreenBinding
import com.example.market.databinding.FragmentMessagesBinding
import com.example.market.databinding.SellerMessageLayoutBinding
import com.example.market.models.*
import com.example.market.navigation.bottomNavVisiblity
import com.example.market.profile.ProfileFragmentSeller
import com.example.market.recycler.BaseViewHolder
import com.example.market.recycler.EndlessRecyclerViewScrollListener
import com.example.market.recycler.SimpleAdapter
import com.example.market.utils.getDrawable
import com.example.market.viewUtils.presentFragmentRemoveLast
import com.example.market.viewUtils.toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat

const val MESSAGE_TYPE_ALL = -2

class MessagesFragment(private var selectedPosition: Int = MESSAGE_TYPE_ALL,
                       var hasBottomNav: Boolean = true,
                       val userId: String = currentUser?.id ?: "") : BaseFragment<FragmentMessagesBinding>(R.layout.fragment_messages) ,MessageCallback {
    private var messageAdapter: MessagesAdapter?=null
    private var hasActionBar = false
    private var chooserDialog: BottomSheetDialog?=null
    private var messagesController: MessagesController?=null

    private fun updateActionBarTitle() {
        binding.actionBar.title?.apply {
            text = when(selectedPosition) {
                MESSAGE_TYPE_SUBSCRIBE -> "Subscribers"
                MESSAGE_TYPE_LIKE -> "Likes"
                MESSAGE_TYPE_MESSAGE -> "Messages"
                MESSAGE_TYPE_PRODUCT_NEW_COMMENT -> "Comments"
                MESSAGE_TYPE_ALL -> "All"
                MESSAGE_TYPE_SUBSCRIPTION -> "Subscriptions"
                else -> null
            }
        }
    }

    private fun openChooser() {
        chooserDialog = BottomSheetDialog(requireContext()).apply {
            val chooserContainer = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
                val likeButton = inflateBinding<ButtonStyleMinBinding>(this,R.layout.button_style_min).apply {

                    root.setOnClickListener {
                        selectedPosition = MESSAGE_TYPE_LIKE
                        changeType()
                        chooserDialog?.dismiss()
                    }
                }

                addView(likeButton.root)
                val subscribers = inflateBinding<ButtonStyleMinBinding>(this,R.layout.button_style_min).apply {
                    image.setImageResource(R.drawable.search_users)
                    text.text = "Subscribers"
                    root.setOnClickListener {
                        selectedPosition = MESSAGE_TYPE_SUBSCRIBE
                        changeType()
                        chooserDialog?.dismiss()
                    }
                }
                addView(subscribers.root)

                val all = inflateBinding<ButtonStyleMinBinding>(this,R.layout.button_style_min).apply {
                    image.setImageResource(R.drawable.settings_message_ic)
                    text.text = "All"
                    root.setOnClickListener {
                        selectedPosition = MESSAGE_TYPE_ALL
                        changeType()
                        chooserDialog?.dismiss()
                    }
                }
                addView(all.root)
                val comments = inflateBinding<ButtonStyleMinBinding>(this,R.layout.button_style_min).apply {
                    image.setImageResource(R.drawable.comment_icon)
                    text.text = "Comments"
                    root.setOnClickListener {
                        selectedPosition = MESSAGE_TYPE_PRODUCT_NEW_COMMENT
                        changeType()
                        chooserDialog?.dismiss()
                    }
                }
                addView(comments.root)
            }
            setContentView(chooserContainer)
            show()
        }
    }

    val emptyModel = Message()
    var lastSelectedPosition = 56465415
    var isEmptyInternal = false

    private var isFirstLoad = true

    fun changeType() {
        updateActionBarTitle()
        messageAdapter?.apply {
            selectedPosition = this@MessagesFragment.selectedPosition
            submitList(null)
            notifyAdapterChanged(false)
            addProgress()
            messagesController?.loadMore(selectedPosition,true)
        }
    }

    private var observer: Observer<ArrayList<Message>> = Observer<ArrayList<Message>> {
        if (it!=null) {
            currentUser?.unreadMessages?.let {
                if (it > 0) {
                    getMainActivity().setMessagesCountBadge(false,-1)
                }
            }
            if (messagesController?.loadingMoreObserver?.value == true) {
                return@Observer
            }
            messageAdapter?.notifyAdapterChanged()
            isFirstLoad = false
        }
    }

    override fun onViewAttachedToParent() {
        super.onViewAttachedToParent()

        getMainActivity().setMessagesCountBadge(false,-1)
        messagesController?.messagesLiveData?.apply {
            value = null
            observe(viewLifecycleOwner,observer)
        }
        if (messagesController?.loadMore(selectedPosition,true) == true) {
            messageAdapter?.addProgress()
        }
    }

    override fun onViewDetachedFromParent() {
        super.onViewDetachedFromParent()

        messagesController?.messagesLiveData?.removeObserver(observer)
    }

    fun updateMessagesCount(){
        currentUser?.let {
            val size = if (selectedPosition== MESSAGE_TYPE_LIKE) it.likes else if (selectedPosition== MESSAGE_TYPE_SUBSCRIBE) it.subscribers else if (selectedPosition== MESSAGE_TYPE_SUBSCRIPTION) it.subscriptions else if (selectedPosition== MESSAGE_TYPE_ALL) it.messages else if (selectedPosition== MESSAGE_TYPE_PRODUCT_NEW_COMMENT) it.comments else 0
            setMessagesCount(size)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        messagesController = if (currentUser?.id == userId) MessagesController.getInstance() else MessagesController(userId)
    }

    override fun onDestroy() {
        super.onDestroy()
        messagesController = null
    }

    private fun setMessagesCount(size: Int) {
        binding.actionBar?.title?.apply {
            val a =  if (selectedPosition== MESSAGE_TYPE_ALL) "All" else if (selectedPosition== MESSAGE_TYPE_PRODUCT_NEW_COMMENT) "Comments" else if (selectedPosition == MESSAGE_TYPE_LIKE) "Likes" else if (selectedPosition== MESSAGE_TYPE_SUBSCRIBE) "Subscribers" else if (selectedPosition== MESSAGE_TYPE_MESSAGE) "Messages" else if (selectedPosition== MESSAGE_TYPE_SUBSCRIPTION) "Subscriptions" else ""
            text = "$a " + if (size!=0) size else ""
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
        binding: FragmentMessagesBinding,
    ) {
        hasActionBar = selectedPosition != MESSAGE_TYPE_ALL

        binding.apply {

            actionBar.apply {
                backButton.visibility = if (!hasBottomNav) View.VISIBLE else View.GONE

                if (!hasBottomNav) {
                    backButton.setOnClickListener { closeLastFragment() }
                    bottomNavVisiblity(context,false)
                }

                title.setOnClickListener { openChooser() }

                title.setCompoundDrawablesWithIntrinsicBounds(null,null, getDrawable(R.drawable.ic_arrow_down),null)
                updateActionBarTitle()
            }

            recyclerView.apply {
                messageAdapter = MessagesAdapter( { holder, type ,list->
                    holder?.apply {
                        if (holder.binding is SellerMessageLayoutBinding) {
                            binding.root.setOnClickListener {

                                val message = list[adapterPosition]
                                if (message.type== MESSAGE_TYPE_PRODUCT_NEW_COMMENT) {
                                    message.product?.let {
                                        val fragment = DetailsFragment(it)
                                        fragment.openAndSelectCommentId = ""
                                        presentFragmentRemoveLast(fragment,false)
                                    }
                                }
                            }
                            (binding as SellerMessageLayoutBinding).messageSenderPhoto.setOnClickListener {
                                val message = list[adapterPosition]
                                bundleAny = message.user!!.id
                                toast(message.user!!.id)
                                presentFragmentRemoveLast(ProfileFragmentSeller(),false)
                            }
                        }
                    }
                    return@MessagesAdapter true
                },selectedPosition,messagesController!!)

                adapter = messageAdapter
                layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)

                addOnScrollListener(object : EndlessRecyclerViewScrollListener(layoutManager as LinearLayoutManager) {
                    override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                        if (messagesController?.loadMore(selectedPosition) == true) {
                            messageAdapter?.addProgress()
                        }
                    }
                })

            }
        }

    }


    override fun onUnReadMessage(count: Int) {

    }

    override fun onFailed(message: String?) {

    }

    class MessagesAdapter(
        val onCreateHolder: (
            holder: BaseViewHolder<ViewDataBinding>?,
            type: Int,
            list: List<Message>
        ) -> Boolean,var selectedPosition: Int,val controller: MessagesController
    ) : SimpleAdapter<ViewDataBinding, Message>(MessageDiff,
        R.layout.seller_message_layout) {
        private  val likeText = "liked your product. "
        private  val followingText = "started following you. "
        private val commentText = "commented to you: \n"
        private val addedToCartSeller = "Added this product to cart"
        private val newOrderSeller = "You have new order"

        private var empty = Message().apply {
            id = System.currentTimeMillis().toString()
            type = VIEW_TYPE_EMPTY_LIST
        }

        private var progress = Message().apply {
            id = System.currentTimeMillis().toString()
            type = VIEW_TYPE_PROGRESS
        }

        private var isEmpty = false

        private fun setEmpty() {
            val list = currentList.toMutableList()
            list.clear()
            isProgres = false
            isEmpty = true
            val emptyText = when(selectedPosition){
                MESSAGE_TYPE_ALL -> "Your messages will apear here\uD83C\uDF89.You have not messages yet.Be active to recieve new interesting messages"
                MESSAGE_TYPE_LIKE -> "Likes is empty.If someone likes ❤ your product comment or etc. it will apear here"
                MESSAGE_TYPE_PRODUCT_NEW_COMMENT -> "Comments will apear here \uD83D\uDCEE.In order to get new comments you should be active"
                MESSAGE_TYPE_SUBSCRIBE -> "In order to get new subscribers post interesting products or comments\uD83D\uDE04"
                MESSAGE_TYPE_SUBSCRIPTION -> "Subscirbe someone that you like most\uD83E\uDD29"
                MESSAGE_TYPE_MESSAGE -> "Start messaging with other users\uD83D\uDE0A"
                MESSAGE_TYPE_NEW_ORDER_ADDED_TO_CART_TO_SELLER -> "Added your product to cart"
                MESSAGE_TYPE_NEW_ORDER_TO_SELLER -> "New order"
                else -> "Empty"
            }

            val lottieRaw = when(selectedPosition) {
                MESSAGE_TYPE_ALL -> R.raw.lottie_gift
                MESSAGE_TYPE_LIKE -> R.raw.lottie_products
                MESSAGE_TYPE_SUBSCRIPTION -> R.raw.lottie_search_product
                MESSAGE_TYPE_SUBSCRIBE -> R.raw.lottie_user
                MESSAGE_TYPE_MESSAGE -> 0
                MESSAGE_TYPE_PRODUCT_NEW_COMMENT -> 0
                else -> 0
            }
            empty.apply {
                id = "This is empty"
                message = emptyText
                type = lottieRaw
            }
            list.add(empty)
            submitList(list)
            toast("Add empty")
        }

        private var isProgres = false
        fun addProgress() {
            if (isProgres) {
                return
            }
            isProgres = true
            val list = currentList.toMutableList()
            if (isEmpty) {
                list.clear()
            }
            list.remove(progress)
            list.add(progress)
            submitList(list)
        }

        fun clear() {
            val list = currentList.toMutableList()
            list.clear()
            submitList(list)
        }

        fun notifyAdapterChanged(checkEmpty:Boolean = true){
            val list = controller.getListByType(selectedPosition).toMutableList()
            if (list.isEmpty()&&checkEmpty) {
                setEmpty()
            } else {
                list.remove(progress)
                isProgres = false
                isEmpty = false
                submitList(list)
            }
        }

        override fun onCreateViewHolderCreated(
            holder: BaseViewHolder<ViewDataBinding>?,
            type: Int,
        ) {
           onCreateHolder(holder, type, currentList as List<Message>)

            holder?.binding?.let {
                if (it is SellerMessageLayoutBinding) {
                    it.messageSubscribe.setOnClickListener {
                        getItem(holder.layoutPosition).let { m->
                            if (m.type == MESSAGE_TYPE_NEW_ORDER_TO_SELLER) {
                                toast("Apply")
                            } else {
                                presentFragmentRemoveLast(holder.itemView.context,ProfileFragmentSeller(getItem(holder.layoutPosition).user!!.id),false)
                            }
                        }
                    }
                }
            }
        }

        override fun getItemLayoutId(position: Int): Int {
            if (currentList[position].type > 6) {
                return R.layout.empty_screen
            }

            return when (currentList[position].type){
                VIEW_TYPE_PROGRESS ->  R.layout.comment_loading_layout
                else -> R.layout.seller_message_layout
            }
        }

        override fun bind(
            holder: BaseViewHolder<ViewDataBinding>,
            position: Int,
            model: Message?,
        ) {
            if (holder.binding is SellerMessageLayoutBinding) {
                holder.binding.apply {
                    model!!.apply {

                        holder.binding.messageSubtitle.text = when (type) {
                            MESSAGE_TYPE_LIKE -> {
                                likeText + sendDate!!.minutes + "m"
                            }
                            MESSAGE_TYPE_PRODUCT_NEW_COMMENT -> ({
                                message?.let {
                                    val dateTimeFormatter = SimpleDateFormat("hh:mm a");
                                    val formatted = dateTimeFormatter.format(sendDate)

                                    val spannableString = SpannableString("$commentText$it \n$formatted")
                                    spannableString.setSpan(ForegroundColorSpan(Color.BLACK),commentText.length,it.length+commentText.length,Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                                    spannableString.setSpan(
                                        ForegroundColorSpan(Color.rgb(151,151,151)),
                                        commentText.length+it.length,
                                        commentText.length+it.length+formatted.length+2,
                                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                                    )

                                }
                            }).toString()
                            MESSAGE_TYPE_NEW_ORDER_TO_SELLER -> {
                                messageSubscribe.text = "Apply"
                                toast("New order to seller")
                                newOrderSeller
                            }
                            MESSAGE_TYPE_NEW_ORDER_ADDED_TO_CART_TO_SELLER -> {
                                addedToCartSeller
                            }
                            MESSAGE_TYPE_SUBSCRIBE -> {
                                messageSubscribe.text = "See profile"
                                followingText + sendDate!!.minutes + "m"
                            }
                            else -> ""
                        }
                        holder.binding.data = model
                    }
                }
            } else if (holder.binding is EmptyScreenBinding) {
                holder.binding.apply {
                    model?.let {
                        lottieView.setAnimation(it.type)
                        lottieView.playAnimation()
                        titleView.text = it.id
                        subtitleView.text = it.message
                        addItemButton.visibility = View.GONE
                    }
                }
            }
        }
    }

    object MessageDiff: DiffUtil.ItemCallback<Message>(){
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
           return oldItem.id == newItem.id
        }

    }

}
const val VIEW_TYPE_PROGRESS = -45