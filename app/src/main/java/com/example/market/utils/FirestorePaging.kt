package com.example.market.utils
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.market.*
import com.example.market.comment.*
import com.example.market.home.lastDoc
import com.example.market.recycler.EmptyRecyclerView
import com.example.market.viewUtils.RecyclerView
import com.example.market.viewUtils.toast
import com.google.firebase.firestore.Query
import java.lang.Exception

open class FirestorePaging<M>(
    private val modelClass: Class<M>,
    var query: Query,
    var ordrerBy: String
) {
    var liveData: MutableLiveData<ArrayList<M>> = MutableLiveData()
    var list: ArrayList<M> = ArrayList()

    var limit: Long = 6L

    var lastDocumentId: String = "lasDoc"
    var newDocId = ""
    var isCleared = false
    var pagingCallback: PagingCallback?=null

    open fun getNewDocumentId() = newDocId

    private var loadingMore = false

    private val TAG = "FirestorePaging"


    fun loadMore() {

        try {
            if (lastDocumentId==getNewDocumentId()||loadingMore) {
                return
            }

            loadingMore = true
            pagingCallback?.onLoadMore()

            var query = query.limit(limit)

            if (newDocId.isNotEmpty()){
                query = query.startAfter(newDocId)
            }

            query.get().addOnCompleteListener { it ->
                loadingMore = false
                pagingCallback?.onFinishedLoadMore()
                it.apply {
                    try {
                        if (isSuccessful&&result!=null) {
                            result.documents.apply {  ->
                                forEach { it1 ->
                                    it1.toObject(modelClass)?.let {
                                        list.add(it)
                                    }
                                }
                                if (isNotEmpty()){
                                    lastDocumentId = newDocId
                                    newDocId = last()[ordrerBy].toString()
                                }
                            }
                        }
                        notifyAdapter()
                    } catch (e: Exception) {
                        log(TAG + " ${e.message}")
                    }
                }
            }
        }catch (e: Exception){
            loadingMore = false
            pagingCallback?.onFinishedLoadMore()
            log(TAG + " ${e.message}")
            throw e
        }
    }

    fun notifyAdapter(){
        liveData.postValue(list)
    }

    open fun add(data: M,pos:Int= 0) {
        list.add(pos,data)
        notifyAdapter()
    }

    fun remove(data: M) {
        list.remove(data)
        notifyAdapter()
    }

    fun observe(
        lifecycleOwner: LifecycleOwner,
        observer: Observer<ArrayList<M>>
    ) = liveData.observe(lifecycleOwner,observer).also {
        loadMore()
    }

    fun removeObserver(observer: Observer<ArrayList<M>>) = liveData.removeObserver(observer)

    interface PagingCallback {
        fun onLoadMore()
        fun onFinishedLoadMore()
    }

    fun clear() {
        list.apply {
            lastDoc = null
            lastDocumentId = "last"
            newDocId = ""
            clear()
            liveData.postValue(this)
            isCleared = true
        }
    }

    fun reload() {
        clear()
        loadMore()
    }
}

class CommentPaging(query: Query,ordrerBy: String,val notifyItemChanged: (pos: Int)-> Boolean) : FirestorePaging<Comment>(Comment::class.java,query, ordrerBy) {
    var repliesHashMap: HashMap<String,ArrayList<Comment>> = HashMap()

    override fun add(data: Comment, pos: Int) {
        if (list.size==0) {
            newDocId = data.id
        }
        super.add(data, pos)
    }

    private fun hideShowReplies(boolean: Boolean, commentId: String,pos: Int) {
        AndroidUtilities.runOnUIThread {
            findCommentById(commentId)?.apply {
                if (boolean && showReplies != 0 || !boolean && showReplies != 1) {
                    showReplies = if (boolean) 0 else 1
                    notifyItemChanged(pos)
                }
            }
        }
    }

    fun deleteComment(comment: Comment,result: Result) {
        comment.apply {
            remove(this)
            val isReply = type == COMMENT_TYPE_REPLY
            var parentComment: Comment?=null

            if (isReply) {
                parentCommentId?.let {
                    parentComment = findCommentById(it)?.apply {
                        repliesCount -=1
                    }
                    getRepliesById(it)?.apply {
                        remove(comment)
                    }
                }
            }
            parentComment?.let {
                if (it.repliesCount==0) {
                    closeComment(it.id)
                }
            }
            removeComment(this,isReply,result,parentComment?.repliesCount)
        }
    }

    private fun getRepliesById(id: String) = repliesHashMap[id]

    fun onRepliesLoaded(cached: ArrayList<Comment>,commentId: String) {
        list.apply {
            findPositionForComment(commentId)?.let {
                val comment = get(it)
                (it + 1).let { prPos->
                    getOrNull(prPos)?.apply {
                        if (type == COMMENT_TYPE_PROGRESS) {
                            removeAt(prPos)
                        }
                    }
                    var lastPos = prPos

                    addAll(lastPos,cached)

                    lastPos += cached.size

                    if (cached.size<comment.repliesCount) {

                        add(lastPos,Comment().apply {
                            type = COMMENT_TYPE_SHOW_MORE_REPLIES
                            id = System.currentTimeMillis().toString()
                            repliesCount = it
                            this.comment = commentId
                        })
                        lastPos +=1
                    } else {
                        findPositionForComment(commentId, COMMENT_TYPE_SHOW_MORE_REPLIES)?.let {
                            removeAt(it)
                        }
                    }
                    findPositionForComment(commentId, COMMENT_TYPE_PROGRESS)?.let {
                        removeAt(it)
                    }
                    add(lastPos,Comment().apply {
                        type = COMMENT_TYPE_CLOSE_COMMENT
                        this.id = System.currentTimeMillis().toString()
                        this.comment = commentId
                    })
                }
            }
        }
    }

    private fun findCommentById(id: String): Comment? {
        list.forEach { if (it.id == id) { return it } }
        return null
    }

    private fun findPositionForComment(id: String, type: Int?=null): Int? {
        list.forEachIndexed { index, m -> if (m.id == id && type == null) { return index } else
                if (type !=null && m.comment == id && m.type == type) { return index } }
        return null
    }

    fun addMoreReplies(id: String) {
        findPositionForComment(id,COMMENT_TYPE_SHOW_MORE_REPLIES)?.let {
            addProgressForComment(id,it)
        }
        notifyAdapter()

        findCommentById(id)?.let { c->
            getRepliesById(id)?.apply {
                last().id.let { lastId ->
                    if (lastId.isNotEmpty()) {
                        getRepliesForComment(c,
                            lastId,
                            object : ResultCallback<ArrayList<Comment>>{
                                override fun onSuccess(result: ArrayList<Comment>?) {
                                    result?.let {
                                        addAll(it)
                                        closeComment(id)
                                        showReplies(id)
                                    }
                                }
                            })
                    }
                }
            }
        }
    }

    fun likeComment(commentId: String,lc: Int) {
        findCommentById(commentId)?.apply {
            likesCount = lc
        }
    }

    fun closeComment(id: String) {
        try {
            findCommentById(id)?.apply {
                if (showReplies==0) {
                    return
                }
                getRepliesById(id)?.let {
                    list.apply {
                        val startPos = findPositionForComment(id)!!+1
                        val endPos = it.size + startPos

                        for (i in startPos until endPos) {
                            removeAt(startPos)
                        }

                        findPositionForComment(id)?.let { p->

                            findPositionForComment(id, COMMENT_TYPE_PROGRESS)?.let {
                                removeAt(it)
                            }
                            findPositionForComment(id, COMMENT_TYPE_SHOW_MORE_REPLIES)?.let {
                                removeAt(it)
                            }
                            findPositionForComment(id, COMMENT_TYPE_CLOSE_COMMENT)?.let {
                                removeAt(it)
                            }


                            hideShowReplies(repliesCount != 0,id,p)
                            notifyAdapter()
                        }
                    }
                }
            }
        } catch (e: Exception) {

        }

    }

    fun showReplies(id: String) {
        val cached = getRepliesById(id)

        findPositionForComment(id)?.let {
            hideShowReplies(false,id,it)
            addProgressForComment(id,it+1)
        }
        if (cached != null) {
            onRepliesLoaded(cached, id)
        } else {
            findCommentById(id)?.let { it ->
                getRepliesForComment(
                    it,
                    result = object : ResultCallback<ArrayList<Comment>> {
                        override fun onSuccess(result: ArrayList<Comment>?) {
                            result?.let {
                                repliesHashMap[id] = result
                                onRepliesLoaded(it, id)
                                notifyAdapter()
                            }
                        }
                    })
            }
        }
        notifyAdapter()
    }

    private fun randomId() = System.currentTimeMillis().toString()

    fun addReplyForComment(commentId: String,commentText: String) {

        if (checkCurrentUser()&&commentText.isNotEmpty()){
            findCommentById(commentId)?.let {

                    val newComment = Comment().apply {
                        comment = commentText
                        type = COMMENT_TYPE_REPLY
                        userPhoto = currentUser!!.photo
                        userName = currentUser!!.name
                        uploadedDate = getCurrentTime()
                        likesCount = 0
                        repliesCount = 0
                        userId = currentUser!!.id
                        productId = it.productId
                        id = randomId()
                        val pcId = if (it.parentCommentId != null) it.parentCommentId else it.id
                        this.parentCommentId = pcId
                        repliedTo = it.userName
                        repliedToId = commentId
                    }
                        newComment.parentCommentId?.let {
                            var list = getRepliesById(it)

                            if (list==null) {
                                list = ArrayList()
                                repliesHashMap[it] = list
                            }

                            findCommentById(it)?.apply {
                                repliesCount +=1

                                closeComment(it)
                                list.add(newComment)
                                showReplies(it)

                                addReplyToComment(newComment,repliesCount,object : Result {
                                    override fun onSuccess(any: Any?) {
                                        try {
                                            products.forEach {
                                                if (it.id == productId){
                                                    messageNewCommentAdded(it,newComment,object :Result{
                                                        override fun onSuccess(any: Any?) {
                                                            updateCountForUserInfo(it.sellerId,"comments",true,object :Result{
                                                                override fun onSuccess(any: Any?) {
                                                                    toast("Messaged succesfully")
                                                                }
                                                            })
                                                        }
                                                    })
                                                }
                                            }
                                        }catch (e: Exception){

                                        }
                                        toast(MyApplication.appContext,"Successfully replied")
                                    }
                                })
                            }
                        }
                    }
        }
    }

    private fun addProgressForComment(id: String,pos: Int){
        list.add(pos,Comment().apply {
            type = COMMENT_TYPE_PROGRESS
            comment = id
        })
    }
}

