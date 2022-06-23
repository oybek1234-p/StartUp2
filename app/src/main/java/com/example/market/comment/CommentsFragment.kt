package com.example.market.comment
//import android.animation.ArgbEvaluator
//import android.animation.ValueAnimator
//import android.app.Dialog
//import android.content.res.ColorStateList
//import android.graphics.Color
//import android.graphics.drawable.ColorDrawable
//import android.os.Bundle
//import android.view.*
//import androidx.annotation.Nullable
//import androidx.core.widget.TextViewCompat
//import androidx.databinding.ViewDataBinding
//import androidx.recyclerview.widget.*
//import com.example.market.*
//import com.example.market.binding.inflateBinding
//import com.example.market.databinding.CommentItemBinding
//import com.example.market.databinding.CommentLoadingLayoutBinding
//import com.example.market.databinding.CommentSingleTextViewBinding
//import com.example.market.databinding.FragmentCommentsBinding
//import com.example.market.models.Product
//import com.example.market.profile.ProfileFragmentSeller
//import com.example.market.utils.*
//import com.example.market.viewUtils.getRecyclerChildSafe
//import com.example.market.viewUtils.presentFragmentRemoveLast
//import com.example.market.viewUtils.snackBar
//import com.example.market.viewUtils.toast
//import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
//import com.google.android.material.bottomsheet.BottomSheetDialog
//import com.google.android.material.bottomsheet.BottomSheetDialogFragment
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.Query
//import de.mrapp.android.bottomsheet.BottomSheet
//
//const val COMMENTS = "comments"
//
//class CommentsFragment(val product: Product,val commentCountChanged: (count: Int) -> Boolean) : BottomSheetDialogFragment() {
//    @Nullable
//    var binding: FragmentCommentsBinding?=null
//    private var paging: CommentPaging?=null
//    private var layManager: LinearLayoutManager?=null
//    private var scrollListener: EndlessRecyclerViewScrollListener?=null
//    private var commentsAdapter: CommentsAdapter?=null
//    var selectedCommentId: String?=null
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        binding = null
//        paging = null
//        layManager = null
//        scrollListener = null
//        commentsAdapter = null
//        selectedCommentId = null
//    }
//
//    private fun addComment(c: String) {
//
//        val comment = Comment().apply {
//            id = System.currentTimeMillis().toString()
//            productId = product.id
//            comment = c
//            uploadedDate = getCurrentTime()
//
//            currentUser?.let {
//                userId = it.id
//                userName = it.name
//                userPhoto = it.photo
//            }
//        }
//
//        shouldScrollToPosition = true
//
//        paging?.add(comment)
//        product.commentCount +=1
//        updateCommentCount()
//
//        clearEditTextFocus()
//
//        addComment(product,comment,object : Result {
//            override fun onSuccess(any: Any?) {
//                toast("Commented successfully")
//            }
//        })
//    }
//
//    private fun clearEditTextFocus() {
//        binding?.commentsBottomNav?.commentEditText?.apply {
//            text = null
//            clearFocus()
//            (requireContext() as MainActivity).closeKeyboard(this)
//        }
//    }
//
//    private fun addReplyComment(commentId: String, comment: String) {
//        paging?.addReplyForComment(commentId,comment)
//        product.commentCount +=1
//        updateCommentCount()
//        clearEditTextFocus()
//    }
//
//    private var shouldScrollToPosition = false
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        try {
//            val commentsReference = FirebaseFirestore
//                .getInstance()
//                .collection(PRODUCTS)
//                .document(product.id)
//                .collection(COMMENTS)
//                .orderBy("id", Query.Direction.DESCENDING)
//
//            paging = CommentPaging(
//                commentsReference,
//                "id"
//            ) { pos ->
//                commentsAdapter?.notifyItemChanged(pos)
//                return@CommentPaging false
//            }
//
//            getCommentLikesForProduct(product.id)
//
//        }catch (e: Exception){
//
//        }
//    }
//
//    private fun setSetCommentCount() {
//        val title = product.commentCount.toString() + " comments"
//        binding?.actionBar?.titleView?.text = title
//    }
//
//    private fun updateCommentCount() {
//        commentCountChanged(product.commentCount)
//        setSetCommentCount()
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        binding = inflateBinding(container, R.layout.fragment_comments)
//
//        binding?.apply {
//            actionBar.apply {
//                setSetCommentCount()
//                exitButton.setOnClickListener {
//                    dismiss()
//                }
//
//                commentsBottomNav.apply {
//                    commentSendButton.setOnClickListener {
//                        commentEditText.apply {
//                            val editable = text
//                            val id = tag
//
//                            setOnFocusChangeListener { _, hasFocus ->
//                                onFocusChangeListener = null
//                                if (!hasFocus) {
//                                    tag = null
//                                }
//                            }
//
//                            if (!editable.isNullOrEmpty()) {
//                                val text = editable.toString()
//                                if (id != null) {
//                                    addReplyComment(id as String,text)
//                                } else {
//                                    addComment(text)
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//            binding?.apply {
//                commentsRecyclerView.apply {
//
//                    setEmptyData("Birinchilardan bulib komment yozing va sovga yutib oling",R.drawable.ic_gift)
//                    layoutManager = object : LinearLayoutManager(
//                        requireContext(),
//                        VERTICAL,
//                        false
//                    ) {
//                    }.also {
//                        layManager = it
//                    }
//
//                    adapter = CommentsAdapter.apply {
//                        commentsAdapter = this
//
//                        commentCallback = object : CommentCallback {
//                            override fun onShowRepliesClicked(
//                                holder: BaseViewHolder<ViewDataBinding>?,
//                                commentId: String,
//                                position: Int,
//                            ) {
//                                try {
//                                    paging?.showReplies(commentId)
//                                }catch (e: java.lang.Exception){
//
//                                }
//                            }
//
//                            override fun onLikeCommentClicked(
//                                holder: BaseViewHolder<ViewDataBinding>?,
//                                position: Int,
//                            ) {
//
//                            }
//
//                            override fun onCommentLiked(
//                                holder: BaseViewHolder<ViewDataBinding>?,
//                                liked: Boolean,
//                                commentId: Comment,
//                                position: Int
//                            ) {
//                                try {
//                                    addNewLikedComment(commentId,liked)
//
//                                    paging?.likeComment(
//                                        commentId.id,
//                                        if (liked)
//                                            commentId.likesCount + 1
//                                        else
//                                            commentId.likesCount - 1
//                                    )
//                                    notifyItemChanged(position)
//
//                                    likeComment(commentId,liked,object : Result {
//                                        override fun onSuccess(any: Any?) {
//                                            snackBar(root,"Comment liked successfully")
//                                        }
//                                    })
//                                } catch (e:java.lang.Exception){
//
//                                }
//                            }
//
//                            override fun onReplyClicked(
//                                holder: BaseViewHolder<ViewDataBinding>?,
//                                commentId: Comment,
//                                position: Int,
//                            ) {
//                                commentsBottomNav.commentEditText.apply {
//                                    hint = "Reply to ${commentId.userName}"
//                                    tag = commentId.id
//                                    (requireContext() as MainActivity).showKeyboard(this)
//                                }
//                            }
//
//                            override fun closeComment(
//                                holder: BaseViewHolder<ViewDataBinding>?,
//                                commentId: String,
//                                position: Int,
//                            ) {
//                                try {
//                                    paging?.closeComment(commentId)
//                                }catch (e: Exception){
//
//                                }
//                            }
//
//                            override fun onShowMoreReplies(
//                                holder: BaseViewHolder<ViewDataBinding>?,
//                                commentId: String,
//                                position: Int,
//                            ) {
//                                try {
//                                    paging?.addMoreReplies(commentId)
//                                }catch (e: java.lang.Exception) {
//
//                                }
//                            }
//
//                            override fun onLongClicked(
//                                holder: BaseViewHolder<ViewDataBinding>?,
//                                commentId: Comment,
//                                position: Int,
//                            ) {
//                                try {
//                                    commentId.apply {
//                                        if (currentUser != null && currentUser!!.id == userId) {
//                                            val builder = BottomSheet.Builder(requireContext())
//                                            builder.addItem(0, "Delete")
//                                            builder.setTitle("Options ${userName}")
//                                            builder.setOnItemClickListener { parent, view, position, id ->
//
//                                                if (id.toInt() == 0) {
//                                                    product.commentCount -= 1
//                                                    updateCommentCount()
//                                                    paging?.deleteComment(commentId,
//                                                        object : Result {
//                                                            override fun onSuccess(any: Any?) {
//                                                                toast("Succesfully deleted")
//                                                            }
//                                                        })
//                                                }
//                                            }
//                                            builder.show()
//                                        }
//                                    }
//                                }catch (e: java.lang.Exception) {
//                                }
//                            }
//                        }
//
//                        registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
//                            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
//                                super.onItemRangeInserted(positionStart, itemCount)
//                                if (shouldScrollToPosition) {
//                                    try {
//                                        layManager?.apply {
//                                            smoothScrollToPosition(CommentsAdapter.mRecyclerView, RecyclerView.State(), 0)
//
//                                            addOnScrollListener(object : RecyclerView.OnScrollListener() {
//                                                override fun onScrolled(
//                                                    recyclerView: RecyclerView,
//                                                    dx: Int,
//                                                    dy: Int
//                                                ) {
//                                                    removeOnScrollListener(this)
//                                                    super.onScrolled(recyclerView, dx, dy)
//                                                    if (dy == 0) {
//                                                        runOnCommentAddedAnimation(recyclerView)
//                                                    }
//                                                }
//                                            })
//                                        }
//                                        shouldScrollToPosition = false
//                                    }catch (e: java.lang.Exception){
//
//                                    }
//                                }
//                            }
//                        })
//                        try {
//                            submitList(null)
//                        }catch (e: Exception){
//
//                        }
//                    }
//                    scrollListener = object : EndlessRecyclerViewScrollListener(layManager) {
//                        override fun onLoadMore(
//                            page: Int,
//                            totalItemsCount: Int,
//                            view: RecyclerView?
//                        ) {
//                            try {
//                                paging?.loadMore()
//                            } catch (e:java.lang.Exception){
//
//                            }
//                        }
//                    }.also {
//                        addOnScrollListener(it)
//                    }
//                }
//            }
//
//            paging?.observe(viewLifecycleOwner, {
//                try {
//                    progressBar.visibility = View.GONE
//
//                    commentsAdapter?.apply {
//                        commentsRecyclerView.setEmpty(it.isEmpty())
//                        submitList(it.toMutableList())
//                    }
//                }catch (e: Exception){
//
//                }
//                })
//
//        }
//        return binding?.root
//    }
//
//    private fun runOnCommentAddedAnimation(recyclerView: RecyclerView) {
//        try {
//            val child = getRecyclerChildSafe(recyclerView,0)
//            if (child!=null) {
//                child.itemView.apply {
//                    val backgroundColor = if (background!=null&&background is ColorDrawable) (background as ColorDrawable).color else Color.WHITE
//                    val animator = ValueAnimator().apply {
//                        setIntValues(backgroundColor,context.getColor(R.color.gray_f2f2f2),backgroundColor)
//                        setEvaluator(ArgbEvaluator())
//                        addUpdateListener {
//                            setBackgroundColor(it.animatedValue as Int)
//                        }
//                        duration = 1000
//                    }
//                    animator.start()
//                }
//            } else {
//                AndroidUtilities.runOnUIThread({
//                    runOnCommentAddedAnimation(recyclerView)
//                },
//                    500)
//            }
//        }catch (e: Exception){
//
//        }
//    }
//
//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//
//        return BottomSheetDialog(requireContext(),R.style.BottomSheetDialogThemeNoFloating).apply {
//                behavior.apply {
//                    skipCollapsed = true
//                    state = STATE_EXPANDED
//                }
//        }
//    }
//
//    object CommentsAdapter : SimpleAdapter<ViewDataBinding,Comment>(CommentDiffCallback,R.layout.comment_item) {
//        lateinit var commentCallback: CommentCallback
//
//        override fun onCreateViewHolderCreated(
//            holder: BaseViewHolder<ViewDataBinding>?,
//            type: Int,
//        ) {
//            holder?.apply {
//                when(type){
//                    R.layout.comment_item -> {
//                            (binding as CommentItemBinding).apply {
//                                commentCallback.apply {
//                                    root.setOnLongClickListener {
//                                        onLongClicked(holder,getItem(adapterPosition),adapterPosition)
//                                        return@setOnLongClickListener true
//                                    }
//
//                                    userPhotoView.setOnClickListener {
//
//                                        presentFragmentRemoveLast(itemView.context,
//                                            ProfileFragmentSeller().apply { bundleAny = currentList[adapterPosition].userId },
//                                            removeLast = false)
//
//                                    }
//
//                                    likeCountView.setOnClickListener {
//                                        getItem(adapterPosition)?.let {
//                                            val liked = !checkIsCommentLiked(it)
//                                            onCommentLiked(holder,liked,it,adapterPosition)
//                                        }
//                                    }
//
//                                    replyButton.setOnClickListener {
//                                        onReplyClicked(holder,getItem(adapterPosition),adapterPosition)
//                                    }
//
//                                    viewRepliesButton.setOnClickListener {
//                                        onShowRepliesClicked(holder,getItem(adapterPosition).id,adapterPosition)
//                                    }
//                                }
//                            }
//                    }
//
//                    R.layout.comment_single_text_view -> {
//                            (binding as CommentSingleTextViewBinding).apply {
//                                textView.setOnClickListener {
//
//                                    getItem(adapterPosition)?.apply {
//                                        if (this.type == COMMENT_TYPE_CLOSE_COMMENT) {
//
//                                            commentCallback.closeComment(holder,getItem(adapterPosition).comment,adapterPosition)
//
//                                        } else if (this.type == COMMENT_TYPE_SHOW_MORE_REPLIES) {
//
//                                            commentCallback.onShowMoreReplies(holder,getItem(adapterPosition).comment,adapterPosition)
//
//                                        }
//                                    }
//
//                                }
//                            }
//                    }
//                    else -> {
//                        toast("Not comment item")
//                    }
//                }
//            }
//        }
//
//        override fun getItemLayoutId(position: Int): Int {
//            return when(getItem(position).type){
//                COMMENT_TYPE_COMMENT -> R.layout.comment_item
//                COMMENT_TYPE_PROGRESS -> R.layout.comment_loading_layout
//                COMMENT_TYPE_SHOW_MORE_REPLIES -> R.layout.comment_single_text_view
//                COMMENT_TYPE_CLOSE_COMMENT -> R.layout.comment_single_text_view
//                else -> {R.layout.comment_item}
//            }
//        }
//        private var likeColorList = arrayListOf(ColorStateList.valueOf(Color.RED),ColorStateList.valueOf(Color.rgb(133,135,139)))
//
//        override fun bind(
//            holder: BaseViewHolder<ViewDataBinding>,
//            position: Int,
//            model: Comment?,
//        ) {
//            holder.binding.apply {
//                when(this) {
//                    is CommentItemBinding -> {
//                        model?.apply {
//
//                            currentUser?.let {
//                                userNameBackground = it.id == model.userId
//                            }
//
//                                root.apply {
//                                    val lp = layoutParams as RecyclerView.LayoutParams
//                                    val margin = AndroidUtilities.dp(44f)
//                                    lp.width = if (type==COMMENT_TYPE_REPLY) MyApplication.displaySize.first - margin else MyApplication.displaySize.first
//                                    lp.marginStart = if (type == COMMENT_TYPE_REPLY) margin else 0
//                                    layoutParams = lp
//                                }
//
//                            TextViewCompat.setCompoundDrawableTintList(likeCountView,likeColorList[if (checkIsCommentLiked(this)) 0 else 1])
//
//                            if (showReplies==-1) {
//                                showReplies = if (repliesCount>0) 0 else 1
//                            }
//                            if (showReplies==0) {
//                                replies = "Show replies (${model.repliesCount})"
//                            }
//
//                            data = model
//                        }
//                    }
//                    is CommentLoadingLayoutBinding -> {
//
//                    }
//                    is CommentSingleTextViewBinding -> {
//                        model?.let {
//                            when(it.type) {
//                                COMMENT_TYPE_SHOW_MORE_REPLIES -> {
//                                    data = "Show more replies"
//                                    gravity = Gravity.START
//                                }
//                                COMMENT_TYPE_CLOSE_COMMENT -> {
//                                    data = "Close replies"
//                                    gravity = Gravity.END
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    object CommentDiffCallback: DiffUtil.ItemCallback<Comment>() {
//        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
//            return oldItem == newItem
//        }
//
//        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
//            return oldItem.id == newItem.id &&oldItem.showReplies == newItem.showReplies
//        }
//    }
//
//    interface CommentCallback {
//        fun onShowRepliesClicked(holder: BaseViewHolder<ViewDataBinding>?, commentId: String,position: Int)
//        fun onLikeCommentClicked(holder: BaseViewHolder<ViewDataBinding>?, position: Int)
//        fun onCommentLiked(holder: BaseViewHolder<ViewDataBinding>?,liked: Boolean, commentId: Comment,position: Int)
//        fun onReplyClicked(holder: BaseViewHolder<ViewDataBinding>?, commentId: Comment,position: Int)
//        fun closeComment(holder: BaseViewHolder<ViewDataBinding>?, commentId: String,position: Int)
//        fun onShowMoreReplies(holder: BaseViewHolder<ViewDataBinding>?, commentId: String,position: Int)
//        fun onLongClicked(holder: BaseViewHolder<ViewDataBinding>?, commentId: Comment,position: Int)
//    }
//}
//interface CommentChangedCallback {
//    fun onCommentSizeChanged(size: Int)
//}