package com.org.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.RotateDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.market.R
import com.example.market.databinding.FragmentMessagesBinding
import com.example.market.databinding.HeaderItemBinding
import com.example.market.databinding.InfoOptionBinding
import com.example.market.databinding.MessagesItemBinding
import com.org.market.*
import com.org.net.models.*
import com.org.ui.actionbar.*
import com.org.ui.adapters.VIEW_TYPE_HEADER
import com.org.ui.adapters.VIEW_TYPE_PROGRESS
import com.org.ui.adapters.headerLayId
import com.org.ui.adapters.progressLayId
import com.org.ui.components.*

class FrameLayoutExtended @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attr, defStyle) {
    var dispatchTouchListener: OnTouchListener? = null

    /**
     * If custom touch listener returns true super.dispatch will not be called.
     */
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (dispatchTouchListener?.onTouch(this, ev) == true) {
            return true
        }
        return super.dispatchTouchEvent(ev)
    }
}

class MessagesFragment(
    var userId: String,
    var currentType: Int,
    var canBack: Boolean = true,
) :
    BaseFragment<FragmentMessagesBinding>(R.layout.fragment_messages) {

    private var messagesLoading = false
        set(value) {
            field = value
            requireBinding().progressBar.visibleOrGone(value, 1)
        }

    private var messagesAdapter: MessagesAdapter? = null

    private var typeChooserView: LinearLayout? = null
    private var typeChooserItemsHolder = arrayListOf<InfoOptionBinding>()
    private var typeChooserOpen = false
    private lateinit var typeTextDrawable: RotateDrawable

    companion object {
        private var typeIconsAndTexts = hashMapOf<Int, Pair<Int, String>>().apply {
            put(MESSAGE_TYPE_ALL, Pair(R.drawable.msg_list, "All"))
            put(MESSAGE_TYPE_SUBSCRIBED, Pair(R.drawable.search_users, "Subscribed"))
            put(MESSAGE_TYPE_PRODUCT_LIKED, Pair(R.drawable.stickers_favorites, "Likes"))
            put(MESSAGE_TYPE_COMMENTED, Pair(R.drawable.msg_viewreplies, "Comments"))
            put(MESSAGE_TYPE_MESSAGE, Pair(R.drawable.msg_link, "Messages"))
        }

        private var emptyList = hashMapOf<Int, Empty>().apply {
            put(MESSAGE_TYPE_ALL,
                Empty(title = "Messages is empty",
                    subtitle = "Your all messages appear here",
                    lottieUrl = "https://assets6.lottiefiles.com/private_files/lf30_vjo1mjum.json"))
            put(MESSAGE_TYPE_SUBSCRIBED, Empty(title = "You have not subscribers yet",
                subtitle = "Be active to get more subscribers",
                lottieUrl = "https://assets1.lottiefiles.com/packages/lf20_bo8vqwyw.json"))
            put(MESSAGE_TYPE_PRODUCT_LIKED, Empty(title = "Comments is empty",
                subtitle = "Users like your product if they are interesting and quality",
                lottieUrl = "https://assets7.lottiefiles.com/datafiles/KZAksH53JBd6PNu/data.json"))
            put(MESSAGE_TYPE_COMMENTED, Empty(title = "Comments is empty",
                subtitle = "Comments which wrote to you appear here",
                lottieUrl = "https://assets8.lottiefiles.com/private_files/lf30_ac86ifrb.json"))
            put(MESSAGE_TYPE_MESSAGE, Empty(title = "You not received messages yet",
                subtitle = "Message other users to get more messages",
                lottieUrl = "https://assets5.lottiefiles.com/packages/lf20_maxj5quq.json"))
        }


    }

    private fun submitMessages(messages: ArrayList<Message>) {
        messagesAdapter?.submitList(messages.toMutableList())
    }

    fun showEmpty(show: Boolean) {
        requireBinding().emptyLayout.apply {
            if (show) {
                val emptyModel = emptyList[currentType]
                data = emptyModel
                executePendingBindings()
            }
            root.visibleOrGone(show, 1)
        }
    }

    fun loadMessagesForType() {
        messagesLoading = false
        showEmpty(false)

        if (updateMessages()) {
            loadMessagesFromNetwork(false)
        }
    }

    /**
     * Updates messages from cache returns if list empty
     */
    fun updateMessages(): Boolean {
        val messages = DataController.getMessages(userId, currentType)
        submitMessages(messages)
        return messages.isEmpty()
    }

    private var result = object : Result {
        override fun onFailed() {

        }

        override fun onSuccess() {

        }
    }

    fun loadMessagesFromNetwork(next: Boolean) {
        setLoading(next, true)
        showEmpty(false)
        DataController.loadNextMessages(userId, currentType, object : Result {
            override fun onFailed() {
                messagesLoading = false
                showEmpty(true)
            }

            override fun onSuccess() {
                setLoading(next, false)
                if (updateMessages()) {
                    showEmpty(true)
                }
            }
        })
    }

    fun setLoading(next: Boolean, loading: Boolean) {
        messagesAdapter?.loadingMore = next && loading
        messagesLoading = !next && loading
    }

    var overshootInterpolator = OvershootInterpolator(1f)
    var decelerateInterpolator = DecelerateInterpolator()

    fun animateListView(hide: Boolean, onEnd: () -> Unit = {}) {
        requireBinding().recyclerView.animate().translationY(if (hide) -dp(24f).toFloat() else 0f)
            .alpha(if (hide) 0f else 1f).setDuration(if (hide) 150 else 300)
            .setInterpolator(if (hide) decelerateInterpolator else overshootInterpolator)
            .withEndAction(onEnd).setStartDelay(if (hide) 100 else 0).start()
    }

    fun onTypeChanged(type: Int, binding: InfoOptionBinding) {
        updateTypeChooserText()
        animateListView(true) {
            messagesAdapter?.apply {
                val animator = requireBinding().recyclerView.itemAnimator
                requireBinding().recyclerView.itemAnimator = null
                requireBinding().recyclerView.clearAnimation()
                submitList(null)
                loadMessagesForType()
                if (isCurrentUser) {
                    loadNewMessages()
                }
                animateListView(false) {
                    requireBinding().recyclerView.itemAnimator = animator
                }
            }
        }
    }

    fun updateTypeChooserText() {
        requireBinding().typeChooserButton.text = typeIconsAndTexts[currentType]!!.second
    }

    fun onTypeChooserCreated(type: Int, infoOptionBinding: InfoOptionBinding) {
        infoOptionBinding.apply {
            executePendingBindings()
            val iconRes = typeIconsAndTexts[type]!!.first
            val titleText = typeIconsAndTexts[type]!!.second

            iconView.setImageResource(iconRes)
            titleView.text = titleText

            root.setOnClickListener {
                val lastType = currentType
                val newType = it!!.tag as Int
                if (lastType != newType) {
                    currentType = newType
                    val lastItem = typeChooserItemsHolder[lastType]
                    updateSelectedTypeItem(false, lastType, lastItem)
                    updateSelectedTypeItem(true, newType, this)
                    runOnUiThread({
                        closeTypeChooser()
                        onTypeChanged(lastType, this)
                    }, 150)
                }
            }
        }
    }

    fun updateSelectedTypeItem(selected: Boolean, type: Int, infoOptionBinding: InfoOptionBinding) {
        infoOptionBinding.apply {
            iconView.imageTint(if (selected) R.attr.colorSecondary else R.attr.colorOnSurfaceMedium)
            titleView.textColor(if (selected) R.attr.colorSecondary else R.attr.colorOnSurfaceHigh)
        }
    }

    fun closeTypeChooser() {
        openTypeChooser(false)
    }

    private fun openTypeChooser(open: Boolean = true) {
        if (typeChooserOpen == open) return
        createTypeChooser().apply {
            visibleOrGone(true)
            val height = -measuredHeight.toFloat() / 2

            if (open) {
                val currentItem = typeChooserItemsHolder.find { it.root.tag == currentType }
                if (currentItem != null) {
                    updateSelectedTypeItem(true, currentType, currentItem)
                }
                translationY = height
                alpha = 0f
            }
            requireBinding().typeChooserContainer.bringToFront()

            typeTextDrawable.apply {
                fromDegrees = 0f
                toDegrees = 180f
            }
            animate().alpha(if (open) 1f else 0f).translationY(if (open) 0f else height / 2)
                .setDuration(if (open) 300 else 150).setInterpolator(
                    if (open) overshootInterpolator else decelerateInterpolator).withEndAction {
                    if (!open) {
                        visibleOrGone(false)
                    }
                }.setUpdateListener {
                    var level = (it.animatedValue as Float * 10000).toInt()
                    if (!open) {
                        level = 10000 - level
                    }
                    typeTextDrawable.level = level
                }.start()
            typeChooserOpen = open
        }
    }

    fun createTypeChooser(): ViewGroup {
        if (typeChooserView == null) {
            typeChooserView = LinearLayout(requireActivity()).apply {
                orientation = LinearLayout.VERTICAL
                visibility = View.GONE
                typeChooserOpen = false
                backgroundColor(R.attr.colorSurface)
                elevation = dp(1f).toFloat()

                val types = arrayListOf(MESSAGE_TYPE_ALL, MESSAGE_TYPE_MESSAGE,
                    MESSAGE_TYPE_SUBSCRIBED, MESSAGE_TYPE_PRODUCT_LIKED, MESSAGE_TYPE_COMMENTED)

                types.forEach {
                    val childItemBinding =
                        inflateBinding<InfoOptionBinding>(this, R.layout.info_option)
                    typeChooserItemsHolder.add(childItemBinding)
                    childItemBinding.root.apply {
                        tag = it
                        addView(this,
                            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT))
                        onTypeChooserCreated(it, childItemBinding)
                    }
                }
                requireBinding().containerView.addView(this,
                    FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT).apply {
                        val tMargin = requireBinding().typeChooserContainer.bottom
                        topMargin = tMargin
                    })
            }
        }
        return typeChooserView!!
    }

    override fun createActionBar(): View? {
        return null
    }

    private val isCurrentUser = userId == currentUserId()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(binding: FragmentMessagesBinding) {

        binding.apply {
            typeChooserButton.setOnClickListener {
                openTypeChooser(!typeChooserOpen)
            }
            containerView.dispatchTouchListener = View.OnTouchListener { v, event ->
                if (typeChooserOpen) {
                    val touchedOutside = !isViewInsideMotionEvent(event, typeChooserView!!)
                    if (touchedOutside) {
                        closeTypeChooser()
                        return@OnTouchListener true
                    }
                }
                return@OnTouchListener false
            }
            val compoundDrawable = getDrawable(R.drawable.arrow_more)
            typeTextDrawable = RotateDrawable().apply {
                drawable = compoundDrawable
                setTint(context().getThemeColor(R.attr.colorOnSurfaceMedium))
                setBounds(0, 0, dp(24f), dp(24f))
            }
            typeChooserButton.apply {
                setCompoundDrawables(
                    null,
                    null,
                    typeTextDrawable,
                    null
                )
            }
            emptyLayout.addItemButton.visibleOrGone(false)
            progressBar.visibleOrGone(false)
            emptyLayout.root.visibleOrGone(false)

            recyclerView.apply {
                adapter = MessagesAdapter().apply {
                    messagesAdapter = this
                }
                layoutManager = LinearLayoutManager(context).apply {
                    addOnScrollListener(object : EndlessRecyclerViewScrollListener(this) {
                        override fun onLoadMore(
                            page: Int,
                            totalItemsCount: Int,
                            view: RecyclerView?,
                        ) {
                            loadMessagesFromNetwork(true)
                        }
                    })
                }
            }
            loadMessagesForType()
        }
    }

    override fun onResume() {
        super.onResume()
        if (isCurrentUser) {
            requireActivity().messagesChangedCallbacks.add(messageChangeCallback)
            if (!updateMessages()) {
                loadNewMessages()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (isCurrentUser) {
            requireActivity().messagesChangedCallbacks.remove(messageChangeCallback)
        }
    }

    fun loadNewMessages() {
        DataController.loadNextMessages(userId, currentType, object : Result {
            override fun onFailed() {
                toast("Failed to load new messages")
            }

            override fun onSuccess() {
                updateMessages()
                scrollToTop()
                DataController.clearUnreadMessages(userId)
                requireActivity().clearBadge()
            }
        }, true)
    }

    fun scrollToTop() {
        requireBinding().recyclerView.apply {
            runOnUiThread({
                smoothScrollToPosition(0)
            }, 300)
        }
    }

    private var messageChangeCallback = object : DataController.MessageChangeCallback {
        override fun onChanged(messageCount: MessagesCount) {
            loadNewMessages()
        }
    }

    inner class MessagesAdapter :
        RecyclerListView.Adapter<ViewDataBinding, Message>(R.layout.messages_item,
            object : DiffUtil.ItemCallback<Message>() {
                override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
                    return oldItem.message == newItem.message && oldItem.sendDate == newItem.sendDate
                }

                override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
                    return oldItem.id == newItem.id
                }
            }) {

        private val progressModel = Message().apply {
            id = newId()
            type = VIEW_TYPE_PROGRESS
        }

        var loadingMore = false
            set(value) {
                if (field != value) {
                    field = value
                    submitList(
                        //if loading add model and notify adapter else remove
                        currentList.toMutableList().apply {
                            remove(find { it.type == VIEW_TYPE_PROGRESS })
                            if (value) {
                                add(progressModel)
                            }
                        })
                }
            }

        override fun submitList(list: MutableList<Message>?) {
            if (list == null) {
                super.submitList(null)
                return
            }
            super.submitList(updateHeaders(list as ArrayList<Message>))
        }

        /**
         * Filter only by date
         * Header title is message field
         */
        fun updateHeaders(list: ArrayList<Message>): ArrayList<Message> {
            val filtered = arrayListOf<Message>()
            var lastType = -1
            list.forEach {
                val calculatedDate = calculateDateExtended(it.sendDate)
                val type = calculatedDate.first
                if (lastType != type) {
                    val headerTitle = getDateText(type)
                    val headerMessage = Message(type = VIEW_TYPE_HEADER, message = headerTitle)
                    filtered.add(headerMessage)
                    lastType = type
                }
                filtered.add(it)
            }
            return filtered
        }

        override fun getItemLayoutId(position: Int, model: Message): Int {
            return when (model.type) {
                VIEW_TYPE_PROGRESS -> progressLayId
                VIEW_TYPE_HEADER -> headerLayId
                else -> super.getItemLayoutId(position, model)
            }
        }

        override fun onViewHolderCreated(
            holder: RecyclerListView.BaseViewHolder<ViewDataBinding>,
            type: Int,
        ) {
            holder.apply {
                binding.apply {
                    if (this is MessagesItemBinding) {
                        photoView.setOnClickListener {
                            val item = currentList[layoutPosition]
                            presentFragment(ProfileFragment(item.receiverId), false)
                        }
                        root.setOnClickListener {
                            val item = currentList[layoutPosition]
                            if (item.type == MESSAGE_TYPE_COMMENTED || item.type == MESSAGE_TYPE_PRODUCT_LIKED) {
                                presentFragment(DetailsFragment(Product().apply {
                                    id = item.productId
                                }), false)
                            }
                            if (item.type == MESSAGE_TYPE_SUBSCRIBED) {
                                presentFragment(ProfileFragment(item.receiverId), false)
                            }

                        }
                        subscribeButton.setOnClickListener {
                            val item = currentList[layoutPosition]
                            when (item.type) {
                                MESSAGE_TYPE_COMMENTED -> {
                                    toast("Open comments")
                                }
                                MESSAGE_TYPE_SUBSCRIBED -> {
                                    DataController.subscribeToUser(item.senderId) { i, s ->
                                        notifyItemChanged(layoutPosition)
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }

        override fun bind(
            holder: RecyclerListView.BaseViewHolder<ViewDataBinding>,
            position: Int,
            model: Message,
        ) {
            val type = model.type

            holder.binding.apply {
                when (this) {
                    is MessagesItemBinding -> {
                        photoView.load(model.senderPhoto.ifEmpty { PhotoUrls.userPlaceholder },
                            circleCrop = true)
                        val sendName = model.senderName.ifEmpty { PlaceholderTexts.newUserText }
                        titleView.text = sendName
                        timeTextView.text = getDateDifferenceText(model.sendDate)
                        subtitleView.text =
                            model.message.ifEmpty { getMessageTextForType(model.type) }

                        subscribeButton.apply {
                            val visible = type == MESSAGE_TYPE_SUBSCRIBED
                            visibleOrGone(visible)

                            //Set visibility gone till we get a result
                            if (visible) {
                                (DataController.checkSubscribed(model.senderId) {
                                    text = if (it) "Subscribed" else "Subscribe"
                                })
                            }
                        }

                        attachedPhoto.apply {
                            val visible =
                                (type == MESSAGE_TYPE_COMMENTED || type == MESSAGE_TYPE_PRODUCT_LIKED)
                            cardView.visibleOrGone(visible)

                            if (visible) {
                                load(model.photo.ifEmpty { PhotoUrls.productPlaceholderBlur })
                            }
                        }
                    }
                    is HeaderItemBinding -> {
                        titleView.text = model.message
                        subtitleView.visibleOrGone(false)
                        iconView.visibleOrGone(false)
                    }

                }
            }
        }
    }
}