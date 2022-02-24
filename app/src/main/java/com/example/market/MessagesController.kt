package com.example.market

import androidx.lifecycle.MutableLiveData
import com.example.market.messages.MESSAGE_TYPE_ALL
import com.example.market.messages.VIEW_TYPE_PROGRESS
import com.example.market.models.Message
import com.example.market.utils.AndroidUtilities
import com.example.market.viewUtils.toast
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

class MessagesController(val userId: String = currentUser?.id ?: "") {
    companion object {
        private var INSTANCE: MessagesController?=null

        fun getInstance():MessagesController {
            return if (INSTANCE!=null) INSTANCE!! else MessagesController().also { INSTANCE = it }
        }
    }

    var messages = ArrayList<Message>()
    var messagesLiveData = MutableLiveData<ArrayList<Message>>(null)

    private var last: Long?= 1L
    private var lastMessageId: Long? = 44L
    private var isFinishedData = false
    var loadingMoreObserver = MutableLiveData(false)

    fun loadMore(type: Int,force: Boolean = false): Boolean {
        return getMessages(type,force)
    }

    var allMessagesLastId = 5L
    private var snapshotQuery: Task<QuerySnapshot>?=null
    var callback: MessagesCallback?=null

    interface MessagesCallback {
        fun onLoadMore()
        fun onLoadMoreFinished()
    }

    var isFirstLoadMore = true
    private var executor = Executors.newSingleThreadExecutor()

    private fun getMessages(type: Int,force: Boolean = false) : Boolean {
        isFirstLoadMore = lastMessageId == 44L

        lastMessageId = getLastMessageId(type)?.date

        snapshotQuery = null

        if (lastMessageId == last&&!force) {
            return false
        }

        last = lastMessageId
        try {
            var query = FirebaseFirestore
                .getInstance()
                .collection(USERS)
                .document(userId)
                .collection(USER_MESSAGES)
                .orderBy("date", Query.Direction.DESCENDING)

            if (type != MESSAGE_TYPE_ALL) {
                query = query.whereEqualTo("type",type)
            }

            lastMessageId?.let {
                query = query.startAfter(it)
            }

            snapshotQuery = query
                .limit(6L)
                .get()

            loadingMoreObserver.postValue(true)
            callback?.onLoadMore()
            snapshotQuery!!.addOnCompleteListener { it ->
                    try {
                        executor.submit {
                            loadingMoreObserver.postValue(false)
                            if (it.isSuccessful&&!it.result.metadata.hasPendingWrites()) {
                                it.result?.documents?.let {
                                    val list = parseDocumentSnapshot(it,Message::class.java).also {
                                        if (type == MESSAGE_TYPE_ALL) {
                                            if (it.size<6) {
                                                isFinishedData = true
                                            }
                                            if (it.isNotEmpty()) {
                                                allMessagesLastId = it.last().date
                                            }
                                        }
                                    }
                                    list.forEach { m->
                                        var isSameItem = false
                                        messages.forEach {
                                            if (m.id == it.id) {
                                                toast("Same item")
                                                isSameItem = true
                                            } else {
                                                isSameItem = false
                                            }
                                        }
                                        if (!isSameItem) {
                                            messages.add(m)
                                        }
                                    }
                                }
                            } else {
                                last = -1
                            }
                            AndroidUtilities.runOnUIThread {
                                notifyObserver()
                                callback?.onLoadMoreFinished()
                            }
                        }
                    }catch (e: Exception) {
                        last = -1L
                        throw e
                        toast("Error messagh")
                    }
                }

        }catch (e: Exception){
            last = -1
            notifyObserver()
            callback?.onLoadMoreFinished()
        }
        return true
    }

    fun notifyObserver() {
        messagesLiveData.postValue(messages)
    }

    fun getListByType(type: Int): ArrayList<Message> {
        if (type == MESSAGE_TYPE_ALL) {
            return messages
        }
        val list = ArrayList<Message>()
        messages.forEach {
            if (it.type == type||it.type== VIEW_TYPE_PROGRESS) {
                list.add(it)
            }
        }
        return list
    }

    private fun getLastMessageId(type: Int): Message? {
        if (messages.isEmpty()) return null

        if (type == MESSAGE_TYPE_ALL){
            messages.forEachIndexed { index, it ->
                if (it.date == allMessagesLastId) {
                    return it
                }
                if (messages.size-1 == index) {
                    return null
                }
            }
        }

        var m: Message?=null
        var list: ArrayList<Long>?= ArrayList<Long>()

        messages.forEach {
            if (it.type == type){
                list!!.add(it.date)
            }
        }

        if (list!!.isEmpty()) {
            return null
        }

        val minDate = Collections.min(list)
        messages.forEach {
            if (it.date == minDate&&type==it.type) {
                m = it
            }
        }
        list = null
        return m
    }
}