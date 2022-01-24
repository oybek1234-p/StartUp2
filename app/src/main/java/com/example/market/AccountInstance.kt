package com.example.market

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Looper
import android.provider.MediaStore.Images
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.market.camera.DispatchQueue
import com.example.market.categories.Category
import com.example.market.comment.*
import com.example.market.model.*
import com.example.market.utils.*
import com.example.market.viewUtils.toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.Transaction
import com.google.firebase.firestore.*
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.local.QueryResult
import com.imagekit.ImageKitCallback
import com.imagekit.android.entity.UploadError
import com.imagekit.android.entity.UploadResponse
import kotlinx.coroutines.*
import kotlinx.coroutines.test.withTestContext
import java.io.File
import java.lang.reflect.Field
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

const val PRODUCTS = "products"
const val DETAILS = "details"
const val PHOTOS = "photos"
const val AVIABLE = "aviable"
const val SHIPPING = "dostavka"
const val TEXT = "text"
const val DESCRIPTION = "description"
const val SHIPPING_LOCATIONS = "shippingLocations"
const val USERS = "users"
const val SHIPPING_LOCATION_ID = "SHIPPPING_LOCATION_ID"
const val DESCRIPTION_ID = 1
const val PHOTOS_ID = 0
const val DOSTAVKA_ID = 2
const val SPECIFICATION_ID = 3
const val SELLER_INFO = "sellerInfo"
const val USER_LIKES = "userLikes"
const val USER_ID = "userId"
const val SUBSCRIPTIONS = "subscriptions"
const val SUBSCRIBERS = "subscribers"
const val SUBSCRIBE_ID = "subscribe"
const val NOT_EXISTS = "documentNotExists"
const val SEARCH_PRODUCTS = "searchProducts"

class SearchProduct {
    var id = ""
    var title = ""
    var date = ""
}

fun getSearchProductsReference() = FirebaseDatabase.getInstance().getReference(SEARCH_PRODUCTS)

fun searchProducts(text: String,resultListener: ResultCallback<ArrayList<SearchProduct>>): Int {
    val query =  getSearchProductsReference()
        .orderByChild("title")
        .startAt(text)
        .endAt(text + "\uf8ff")

    return newDatabaseRequest(query,object : QueryResultListener {
        override fun onSuccess(result: Any?) {
            if (result is DataSnapshot?) {
                if (result!=null) {
                    if (result.hasChildren()&&result.exists()) {
                        val list = ArrayList<SearchProduct>()
                        result.children.forEach {
                            it.getValue(SearchProduct::class.java)?.let {
                                list.add(it)
                            }
                        }
                        resultListener.onSuccess(list)
                    } else {
                        resultListener.onSuccess(null)
                    }
                } else {
                    resultListener.onSuccess(null)
                }
            }
        }

        override fun onFailed(exception: java.lang.Exception?) {
            resultListener.onFailed(exception)
        }
    })
}

fun setSearchProduct(id: String,title: String,result: Result?) {
    val searchProduct = SearchProduct().apply {
        this.id = id
        this.title = title
        this.date = System.currentTimeMillis().toString()
    }
    getSearchProductsReference().child(searchProduct.id).setValue(searchProduct).addOnCompleteListener {
        if (it.isSuccessful) {
            result?.onSuccess()
        } else {
            result?.onFailed()
            throw it.exception!!
        }
    }
}

var currentUser:User?=null
set(value) {
    field = value
    if (value!=null) {
        currentUserLiveData.postValue(value)
    }
}

/**
 * List user
 */
var subscribedUsers = arrayListOf<String>()
var products = ArrayList<Product>()
var userLikedProducts: ArrayList<Like> = ArrayList()
var categories: ArrayList<Category>?=null

fun uploadCategory(category: Category,result: Result) {
    FirebaseFirestore
        .getInstance()
        .collection(CATEGORIES)
        .document(category.id)
        .set(category)
        .addOnCompleteListener {
            if (it.isSuccessful) {
                result.onSuccess()
            } else {
                result.onFailed()
            }
        }
}

fun getMainCategories(resultListener: ResultCallback<List<Category>?>): Int {
    categories?.apply {
        val mainList = filter { it.parentId == "" }
        if (mainList.isNotEmpty()) {
            resultListener.onSuccess(mainList)
            return 0
        }
    }
    val query = FirebaseFirestore.getInstance().collection(CATEGORIES).whereEqualTo("parentId","")
            return newFirestoreRequest(query,object : QueryResultListener {
                override fun onSuccess(result: Any?) {
                    if (result is QuerySnapshot?) {
                        if (result==null || result.documents.isEmpty()) {
                            resultListener.onSuccess(null)
                            return
                        }
                        val list = parseDocumentSnapshot(result.documents,Category::class.java)
                        categories = list
                        resultListener.onSuccess(list)
                    }
                }

                override fun onFailed(exception: java.lang.Exception?) {
                    resultListener.onFailed(exception)
                }
            })

}

//** Firebasega junatilgan requestlarning onResponse interface listenerlari.
var notificationRequests = HashMap<Int,QueryResultListener>()
private var stageQueue = DispatchQueue("StageQueue")

interface QueryResultListener {
    fun onSuccess(result: Any?)
    fun onFailed(exception: java.lang.Exception?=null)
}

fun newRequestId() = System.currentTimeMillis().toInt()
fun getRequestListener(requestId: Int) = notificationRequests[requestId]
fun cancellRequest(requestId: Int) {
    notificationRequests.remove(requestId)
}
fun addNewRequest(requestId: Int,listener: QueryResultListener) {
    notificationRequests[requestId] = listener
}

fun newFirestoreRequest(query: Query,notificationListener: QueryResultListener): Int {
    val requestId = newRequestId()
    addNewRequest(requestId,notificationListener)

    stageQueue.postRunnable {
        query.get().addOnCompleteListener {
            getRequestListener(requestId)?.apply {
                cancellRequest(requestId)
                if (it.isSuccessful) {
                    onSuccess(it.result)
                } else {
                    onFailed(it.exception)
                }
            }
        }
    }
    return requestId
}

fun newDatabaseRequest(query: com.google.firebase.database.Query,notificationListener: QueryResultListener): Int {
    val requestId = newRequestId()
    addNewRequest(requestId,notificationListener)

    stageQueue.postRunnable {
        query.get().addOnCompleteListener {
            getRequestListener(requestId)?.apply {
                cancellRequest(requestId)
                if (it.isSuccessful) {
                    onSuccess(it.result)
                } else {
                    onFailed(it.exception)
                }
            }
        }
    }
    return requestId
}

fun getSubCategories(mainCatId: String,resultListener: ResultCallback<List<Category>?>): Int {
    categories?.apply {
        val newList = filter { it.parentId == mainCatId }
        if (newList.isNotEmpty()) {
            resultListener.onSuccess(newList)
            return 0
        }
    }

    val query = FirebaseFirestore.getInstance().collection(CATEGORIES).whereEqualTo("parentId",mainCatId)
    return newFirestoreRequest(query,object : QueryResultListener {
        override fun onSuccess(result: Any?) {
            if (result is QuerySnapshot?) {
                if (result == null || result.documents.isEmpty()) {
                    resultListener.onSuccess(null)
                    return
                }

                val newList = parseDocumentSnapshot(result.documents, Category::class.java)
                if (categories == null) {
                    categories = ArrayList()
                }
                categories?.addAll(newList)
                resultListener.onSuccess(newList)
            }
        }

        override fun onFailed(exception: java.lang.Exception?) {
            toast("Request failed")
        }
    })
}

fun addSubscriber(id: String,add:Boolean) {
    subscribedUsers.remove(id)
    if (add) {
        subscribedUsers.add(id)
    }
}

fun checkSubscribed(id: String): Boolean{
    subscribedUsers.forEach {
        if (id == it){
            toast("Check subscribed true")
            return true
        }
    }
    return false
}

var sharedPreferences = MyApplication.sharedPreferences

fun setShippingLocation(newShippingLocation: ShippingLocation,result: Result?) {
    if (checkCurrentUser()){
        currentUser?.shippingLocation = newShippingLocation
        getUserReference(currentUser!!.id).child("shippingLocation").setValue(newShippingLocation).addOnCompleteListener {
            if (it.isSuccessful) {
                result?.onSuccess()
            }else {
                result?.onFailed()
            }
        }

    }
}


@kotlin.annotation.Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class SellerInfo

class Like {
    var id: String = ""
    var userId: String = ""
    var product: Product?=null
    var productId = ""

    constructor(id: String,userId: String,product: Product,productId: String) {
        this.id = id
        this.userId = userId
        this.product = product
        this.productId = productId
    }
    constructor()
}

const val COMMENT_REPLIES = "commentReplies"
fun getProductsReference() = FirebaseFirestore.getInstance().collection(PRODUCTS)

fun deleteProduct(product: Product,result: Result) {
    if (checkCurrentUser()){
        products.remove(product)
        getProductsReference().document(product.id).delete().addOnCompleteListener {
            if (it.isSuccessful){
                result.onSuccess()
            } else {
                result.onFailed()
            }
        }
    }
}

fun getCommentLikesReference(productId: String) = FirebaseFirestore
    .getInstance()
    .collection(USERS)
    .document(currentUser!!.id)
    .collection("commentLikes")
    .document(productId)

var commentLikes = hashMapOf<String,ArrayList<String>>()

fun checkIsCommentLiked(comment: Comment): Boolean {
    val list = commentLikes[comment.productId] ?: return false
    return list.contains(comment.id)
}

fun addNewLikedComment(comment: Comment,like: Boolean) {

    var list = commentLikes[comment.productId]

    if (list == null) {
        list = ArrayList()
        commentLikes[comment.id] = list
    }
    if (like) {
        list.add(comment.id)
    } else {
        list.remove(comment.id)
    }

}

fun getCommentLikesForProduct(productId: String){
    getCommentLikesReference(productId).get().addOnCompleteListener {
        if (it.isSuccessful) {
            (it.result["commentLikes"] as ArrayList<String>?)?.let {
                commentLikes[productId] = it
            }
        }
    }
}

fun likeComment(comment: Comment,like: Boolean,result: Result) {
    val query = getCommentLikesReference(comment.productId)
    updateCommentLikesCount(comment, like, object : Result{
        override fun onSuccess(any: Any?) {
            query.update("commentLikes",if (like) FieldValue.arrayUnion(comment.id) else FieldValue.arrayRemove(comment.id))
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        result.onSuccess()
                    } else {
                        val commentLikes = arrayListOf(comment.id)
                        query.set(mapOf(Pair("commentLikes", commentLikes))).addOnCompleteListener {
                            if (it.isSuccessful) {
                                result.onSuccess()
                            } else {
                                result.onFailed()
                            }
                        }
                    }
                }
        }
    })
}

fun getRepliesForComment(
    c: Comment,
    lastDocId: String? = null,
    result: ResultCallback<ArrayList<Comment>>
) {
    var query = FirebaseFirestore
        .getInstance()
        .collection(PRODUCTS)
        .document(c.productId)
        .collection(COMMENTS)
        .document(c.id)
        .collection(COMMENT_REPLIES)
        .orderBy("id",Query.Direction.DESCENDING)
        .limit(2)

    lastDocId?.let {
        query = query.startAfter(it)
    }

    query
        .get()
        .addOnCompleteListener {
        if (it.isSuccessful) {
            val arrayList = ArrayList<Comment>()
            if (it.result!=null&&it.result.documents.size>0) {
                for (doc in it.result.documents) {
                    val comment = doc.toObject(Comment::class.java)
                    if (comment != null) {
                        arrayList.add(comment)
                    }
                }
            }
            result.onSuccess(arrayList)
        } else {
           result.onFailed()
        }
    }
}

const val CATEGORIES = "categories"

fun addCategory(category: Category,result: Result) {
 FirebaseFirestore
     .getInstance()
     .collection(CATEGORIES)
     .document(category.id)
     .set(category)
     .addOnCompleteListener {
         if (it.isSuccessful) {
             result.onSuccess()
         } else {
             result.onFailed()
         }
     }
}

fun getCategory(categoryId: String,result: ResultCallback<Category>) {
    FirebaseFirestore
        .getInstance()
        .collection(CATEGORIES)
        .document(categoryId)
        .get()
        .addOnCompleteListener { it ->
            if (it.isSuccessful){
                it.result?.toObject(Category::class.java)?.let {
                    result.onSuccess(it)
                }
            } else {
                result.onFailed()
            }
        }
}

fun deleteCategory(categoryId: String,result: Result) {
    FirebaseFirestore
        .getInstance()
        .collection(CATEGORIES)
        .document(categoryId)
        .delete()
        .addOnCompleteListener {
            if (it.isSuccessful) {
                result.onSuccess()
            } else {
                result.onFailed()
            }
        }
}

fun getSubCategoryies(
    parentId: String,
    resultCallback: ResultCallback<ArrayList<Category>>
) {
    FirebaseFirestore
        .getInstance()
        .collection(CATEGORIES)
        .whereEqualTo("parentId",parentId)
        .get()
        .addOnCompleteListener { it ->
            if (it.isSuccessful){
                it.result?.documents?.let {
                    resultCallback.onSuccess(
                        parseDocumentSnapshot(it,Category::class.java)
                    )
                }
            } else {
                resultCallback.onFailed()
            }
        }
}

fun getParentCategories(result: ResultCallback<ArrayList<Category>>,limit: Long?=null) {
    var query = FirebaseFirestore
        .getInstance()
        .collection(CATEGORIES)
        .whereEqualTo("parentId",null)
    limit?.let {
        query = query.limit(it)
    }
    query.get().addOnCompleteListener {
       getResult(it,result,Category::class.java)
    }
}

fun <T> getResult(task: Task<QuerySnapshot>,resultCallback: ResultCallback<ArrayList<T>>,classType: Class<T>) {
    task.apply {
        if (isSuccessful) {
            result?.documents?.let {
                resultCallback.onSuccess(parseDocumentSnapshot(it,classType))
            }
        } else {
            resultCallback.onFailed()
        }
    }
}

fun <T> parseDocumentSnapshot(list: List<DocumentSnapshot>,classType: Class<T>): ArrayList<T> {
    val arrayList = ArrayList<T>()
    for (i in list) {
        i.toObject(classType)?.let {
            arrayList.add(it)
        }
    }
    return arrayList
}

fun updateRepliesCount(
    productId: String,
    commentId: String,
    count: Int,
    result: Result
) {
    FirebaseFirestore
        .getInstance()
        .collection(PRODUCTS)
        .document(productId)
        .collection(COMMENTS)
        .document(commentId)
        .update("repliesCount",count)
        .addOnCompleteListener {
            if (it.isSuccessful){
                result.onSuccess()
            } else {
                result.onFailed()
            }
        }
}

fun addReplyToComment(repliedComment: Comment,repliesCount: Int,result: Result) {
    FirebaseFirestore
        .getInstance()
        .collection(PRODUCTS)
        .document(repliedComment.productId)
        .collection(COMMENTS)
        .document(repliedComment.parentCommentId!!)
        .collection(COMMENT_REPLIES)
        .document(repliedComment.id)
        .set(repliedComment)
        .addOnCompleteListener {
            if (it.isSuccessful){
                updateRepliesCount(repliedComment.productId,repliedComment.parentCommentId!!,repliesCount,object : Result{
                    override fun onSuccess(any: Any?) {

                        updateCommentCount(repliedComment.productId,true,result)
                    }
                })
            } else {
                result.onFailed()
            }
        }
}

const val COMMENT_LIKES = "commentLikes"


fun setProductCommentCount(
    productId: String,
    commentCount: Int,
    result: Result
) {
    FirebaseFirestore
        .getInstance()
        .collection(PRODUCTS)
        .document(productId)
        .update(
            "commentCount",
            commentCount
        )
        .addOnCompleteListener {
            if (it.isSuccessful) {
                result.onSuccess()
            } else {
                throw it.exception!!
                result.onFailed()
            }
        }
}


fun updateCommentLikesCount(comment: Comment,like: Boolean,result: Result) {
    val reply = comment.type == COMMENT_TYPE_REPLY
    var query = FirebaseFirestore
        .getInstance()
        .collection(PRODUCTS)
        .document(comment.productId)
        .collection(COMMENTS)
        .document(if (reply) comment.parentCommentId!! else comment.id)

    if (reply) {
        query = query.collection(COMMENT_REPLIES).document(comment.id)
    }
    query.update("likesCount",if (like) FieldValue.increment(1) else FieldValue.increment(-1))
        .addOnCompleteListener {
            if (it.isSuccessful) {
                result.onSuccess()
            } else {
                result.onFailed()
            }
        }
}

fun removeComment(comment: Comment,reply: Boolean,result: Result,repliesCount: Int?=null) {
    var query = FirebaseFirestore
        .getInstance()
        .collection(PRODUCTS)
        .document(comment.productId)
        .collection(COMMENTS)
        .document(if (reply) comment.parentCommentId!! else comment.id)

    if (reply) {
        query = query.collection(COMMENT_REPLIES).document(comment.id)
    }

    query
        .delete()
        .addOnCompleteListener {
            if (it.isSuccessful) {
                if (reply&&repliesCount!=null){
                    comment.parentCommentId?.let {
                        updateRepliesCount(comment.productId,it,repliesCount,object : Result {
                            override fun onSuccess(any: Any?) {
                                updateCommentCount(comment.productId,false,object : Result{
                                    override fun onSuccess(any: Any?) {

                                    }
                                })
                            }
                        })
                    }
                    return@addOnCompleteListener
                } else {
                    updateCommentCount(comment.productId,false,result)
                }
            } else {
                result.onFailed()
            }
        }
}

fun messageNewCommentAdded(product: Product,comment: Comment,result: Result) {
    val message = Message()
    message.id =  currentUser!!.id + comment.id
    message.sendDate = getCurrentTime()
    message.product = product
    message.user = currentUser
    message.type = MESSAGE_TYPE_PRODUCT_NEW_COMMENT
    message.message = comment.comment
    message.recieverId = product.sellerId
    messageToSeller(message,result)
}

fun addComment(product: Product,comment: Comment,result: Result) {
    if (!checkCurrentUser()) {
        result.onFailed()
        return
    }
    addCommentToProduct(comment.productId,comment,object: Result {
        override fun onSuccess(any: Any?) {
            updateCommentCount(product.id,true,object : Result {
                override fun onSuccess(any: Any?) {
                    messageNewCommentAdded(product,comment,object : Result{
                        override fun onSuccess(any: Any?) {
                            updateCountForUserInfo(comment.userId,"comments",true,result)
                        }
                    })

                }
            })
        }
    })
}

fun updateCommentCount(productId: String,increase: Boolean,result: Result) {
    FirebaseFirestore
        .getInstance()
        .collection(PRODUCTS)
        .document(productId)
        .update("commentCount",if (increase) FieldValue.increment(1) else FieldValue.increment(-1))
        .addOnCompleteListener {
            if (it.isSuccessful) {
                toast("Comment count")
                result.onSuccess()
            } else {
                result.onFailed()
            }
        }
}

fun addCommentToProduct(productId: String,comment: Comment,result: Result){
    FirebaseFirestore
        .getInstance()
        .collection(PRODUCTS)
        .document(productId)
        .collection(COMMENTS)
        .document(comment.id)
        .set(comment)
        .addOnCompleteListener {
            if (it.isSuccessful){
                result.onSuccess()
            } else {
                result.onFailed()
            }
        }
}

fun removeCommentFromProduct(productId: String,commentId: String, result: Result) {
    FirebaseFirestore
        .getInstance()
        .collection(PRODUCTS)
        .document(productId)
        .collection(COMMENTS)
        .document(commentId)
        .delete()
        .addOnCompleteListener {
            if (it.isSuccessful) {
                result.onSuccess()
            } else {
                result.onFailed()
            }
        }
}

fun addSubscriber(id: String,subscirberId: String,add: Boolean){
    FirebaseDatabase.getInstance().getReference("subscribers").child(id).child(subscirberId).setValue(if (add) 0 else null)
}

fun addSubscription(id: String,sellerId: String,add: Boolean) {
    FirebaseDatabase.getInstance().getReference("subscriptions").child(id).child(sellerId).setValue(if (add) 0 else null)
}

fun getSubscriptionsReference(id: String,sellerId: String) = FirebaseDatabase.getInstance().getReference("subscriptions").child(id).child(sellerId)

fun checkSubscribed(id: String,sellerId: String,result: ResultCallback<Boolean>): ValueEventListener? {
    try {
        return getSubscriptionsReference(id, sellerId).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()&&snapshot.value!=null) {
                    result.onSuccess(true)
                } else {
                    result.onSuccess(false)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }catch (e: Exception) {
        result.onSuccess(false)
        result.onFailed()
    }
    return null
}

fun getSellerInfo(id: String,mainResult: ResultCallback<Pair<Boolean,User?>>) {

    getSubscriptionStoreInfo(id,object : ResultCallback<User>{
        override fun onSuccess(result: User?) {
           mainResult.onSuccess(Pair(true,result))
        }
        override fun onFailed() {
            super.onFailed()
            getUser(id,object : ResultCallback<User>{
                override fun onSuccess(result: User?) {
                    mainResult.onSuccess(Pair(false,result))
                }

                override fun onFailed() {
                    mainResult.onFailed()
                }
            })
        }
    })
}

fun getSubscriptionStoreInfo(sellerId: String,result: ResultCallback<User>) {
    if (currentUser==null){
        result.onFailed()
        return
    }
    FirebaseFirestore
        .getInstance()
        .collection(USERS)
        .document(currentUser!!.id)
        .collection(SUBSCRIPTIONS)
        .document(sellerId)
        .get()
        .addOnCompleteListener {
            if (it.isSuccessful) {
                if (it.result.exists()) {
                    val sellerAccount = it.result.toObject(User::class.java)
                    result.onSuccess(sellerAccount)
                } else {
                    result.onFailed()
                }
            } else {
                result.onFailed()
            }
        }
}

fun deleteSubscriberFromStore(subscriberId: String,sellerId: String,result: Result) {
    FirebaseFirestore
        .getInstance()
        .collection(USERS)
        .document(sellerId)
        .collection(SUBSCRIBERS)
        .document(subscriberId)
        .delete()
        .addOnCompleteListener {
            if (it.isSuccessful) {
                result.onSuccess()
            } else {
                result.onFailed()
            }
        }
}

fun addSubscriberToStore(subscriber: User,sellerId: String,result: Result) {
    FirebaseFirestore
        .getInstance()
        .collection(USERS)
        .document(sellerId)
        .collection(SUBSCRIBERS)
        .document(subscriber.id)
        .set(subscriber)
        .addOnCompleteListener {
            if (it.isSuccessful) {
                result.onSuccess()
            } else {
                result.onFailed()
            }
        }
}

fun getSubscriber(storeId: String,subscriberId: String,result: ResultCallback<User?>) {
    FirebaseFirestore
        .getInstance()
        .collection(USERS)
        .document(storeId)
        .collection(SUBSCRIBERS)
        .document(subscriberId)
        .get()
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (task.result!=null) {
                    task.result?.apply {
                        val user = toObject(User::class.java)
                        user?.let {
                            result.onSuccess(it)
                        }
                    }
                } else {
                    result.onSuccess(null)
                }
            } else {
             result.onFailed()
            }
        }
}

fun increaseProductViewCount(id: String) {
    try {
        getProductsReference().document(id).update("viewsCount",FieldValue.increment(1))

    }catch (e: Exception) {

    }
}

fun subscibeToStore(sellerAccountId: String,subscribe: Boolean,result: Result) {
    try {
        if (subscribe) {
            subscribedUsers.add(sellerAccountId)
        }else {
            subscribedUsers.forEach {
                if (it==sellerAccountId) {
                    subscribedUsers.remove(it)
                }
            }
        }
        if (subscribe) {
            val message = Message().apply {
                id = getSubscribtionMessageId(currentUser!!,sellerAccountId)
                date = getCurrentDateTimestamp()
                message = null
                product = null
                sendDate = getCurrentTime()
                recieverId = sellerAccountId
                type = MESSAGE_TYPE_SUBSCRIBE
                read = false
                user = currentUser
            }
            messageToSeller(message, object : Result{
                override fun onSuccess(any: Any?) {
                    addSubscriber(sellerAccountId, currentUser!!.id,true)
                    addSubscription(currentUser!!.id,sellerAccountId,true)
                    increaseFieldUser(sellerAccountId,"subscribers",true)
                    increaseFieldUser(currentUser!!.id,"subscriptions",true)
                    result.onSuccess()
                }

                override fun onFailed() {
                    super.onFailed()
                    result.onFailed()
                }
            })
        } else {
            addSubscriber(sellerAccountId, currentUser!!.id,false)
            addSubscription(currentUser!!.id,sellerAccountId,false)
            increaseFieldUser(sellerAccountId,"subscribers",false)
            increaseFieldUser(currentUser!!.id,"subscriptions",false)

            deleteSentMessage(currentUser!!.id, getSubscribtionMessageId(currentUser!!,sellerAccountId),object : Result {
                override fun onSuccess(any: Any?) {
                    result.onSuccess()
                }

                override fun onFailed() {
                    super.onFailed()
                    result.onFailed()
                }
            })
        }
    }catch (e: Exception){

    }
}

fun increaseFieldUser(userId: String,fieldName: String,increase: Boolean) {
    getUserReference(userId).child(fieldName).setValue(ServerValue.increment(if (increase) 1 else -1))
}
private fun getSubscribtionMessageId(
    currentUser: User,
    sellerId: String
): String {
    return currentUser.id + sellerId + SUBSCRIBE_ID
}

fun unsubscribeFromStore(
    sellerId: String,
    result: Result
) {
    if (currentUser==null){
        result.onFailed()
        return
    }
    log("Unsubscribe $sellerId")
    FirebaseFirestore
        .getInstance()
        .collection(USERS)
        .document(currentUser!!.id)
        .collection(SUBSCRIPTIONS)
        .document(sellerId)
        .delete()
        .addOnCompleteListener {
            if (it.isSuccessful){
                deleteSentMessage(
                            sellerId,
                            getSubscribtionMessageId(
                                currentUser!!,
                                sellerId),object : Result {
                                override fun onSuccess(any: Any?) {
                                    updateCountForUserInfo(sellerId,"subscribers",false,result)
                                }
                                })
            } else {
                result.onFailed()
            }
        }
}


fun getUserLikedProducts(result: ResultCallback<ArrayList<Like>>?=null,fromNetwork: Boolean = false,startFrom: String?=null,limitCount: Long ?=null) {
    if (currentUser==null) {
        result?.onFailed(USER_IDENTITY)
        return
    }
    var mFromNetwork = fromNetwork

    if (!mFromNetwork) {
        mFromNetwork = if (userLikedProducts!=null) {
            result?.onSuccess(userLikedProducts)
            false
        } else {
            true
        }
    }

    if (mFromNetwork) {
        currentUser?.let {
           var query =  FirebaseFirestore
                .getInstance()
                .collection(USER_LIKES)
                .whereEqualTo(USER_ID, currentUser!!.id)
                .limit(10)

            startFrom?.let {
                query = query.startAfter(it)
            }
            limitCount?.let {
                query = query.limit(it)
            }

            query.get()
                .addOnCompleteListener {
                    if (it.isSuccessful&&it.result!=null) {
                        it.result?.apply {

                            if (userLikedProducts == null) {
                                userLikedProducts = ArrayList()
                            }
                            userLikedProducts?.clear()

                            for (i in it.result.documents) {
                                i.toObject(Like::class.java)?.let { like->
                                    userLikedProducts?.add(like)
                                }
                            }
                            result?.onSuccess(userLikedProducts)
                        }
                    }
                }
        }
    }
}

 fun checkIsProductLiked(product: Product,result: ResultCallback<Boolean>) {
    if (currentUser==null){
        result.onFailed(USER_IDENTITY)
        return
    }
    if (isProductLiked(product)) {
        result.onSuccess(true)
        return
    }

    FirebaseFirestore
        .getInstance()
        .collection(USER_LIKES)
        .document(currentUser!!.id+product.id)
        .get()
        .addOnCompleteListener {
            if (it.isSuccessful) {
            val exists = it.result.exists()
            result.onSuccess(exists)

            if (exists){
                it.result.toObject(Like::class.java)?.let { like->
                    userLikedProducts?.add(like)
                }

            }
        } else {
            result.onFailed(it.exception)
        }
    }
}

fun checkCurrentUser() : Boolean {
    return currentUser != null
}

 fun isProductLiked(product: Product): Boolean {
    if (!checkCurrentUser()) {
        return false
    }
     var contains = false

         for (i in userLikedProducts) {
             i.product?.let {
                 if (it.id == product.id) {
                     contains = true
                 }
             }
         }

    return contains
}

fun setUnreadMessagesCount(id: String,count: Int) {
        try {
            getUserReference(id).child("unreadMessages").setValue(if (count==0) 0 else ServerValue.increment(count.toLong()))
        }catch (e: Exception) {

        }

}
fun clearUnreadMessages() {
    currentUser?.let {
        setUnreadMessagesCount(it.id,0)
        it.unreadMessages = 0
        SharedConfig.getInstance().saveUserConfig()
    }
}

fun setMessagesCount(id: String,increase: Boolean) {
    try {
        getUserReference(id).child("messages").setValue(ServerValue.increment(if (increase) +1 else -1))
    }catch (e: Exception) {

    }
}

fun startMessagesSnapshot(callback: MessageCallback){
    if (currentUser==null) {
        callback.onFailed(USER_IDENTITY)
        return
    }
    try {
        currentUser?.let { it ->
            getUserReference(it.id).child("unreadMessages").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        snapshot.value?.let {
                            callback.onUnReadMessage(it.toString().toInt())
                        }
                    }catch (e: Exception){

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
    }catch (e: Exception) {

    }

}

const val LIKES_COUNT = "likesCount"
const val USER_MESSAGES = "messages"

fun getLikeForProduct(product: Product) : Like? {
        for (i in userLikedProducts) {
            if (i.productId == product.id) {
                return i
            }
        }
    return null
}

fun likeProduct(likedProduct: Product,like: Boolean,mLike: Like,resultCallback: Result) {
    if (!checkCurrentUser()) {
        resultCallback.onFailed(USER_IDENTITY)
        return
    }
    updateLikeCount(likedProduct.id,likedProduct.likesCount,object : Result {
        override fun onSuccess(any: Any?) {
            if (like) {
                addToUserLikes(mLike,object : Result {
                    override fun onSuccess(any: Any?) {
                        val message = Message().apply {
                            id = mLike.id
                            sendDate = getCurrentTime()
                            product = likedProduct
                            user = currentUser
                            type = MESSAGE_TYPE_LIKE
                            message = null
                            recieverId = likedProduct.sellerId
                        }
                        messageToSeller(message,object : Result {
                            override fun onSuccess(any: Any?) {
                                updateCountForUserInfo(likedProduct.sellerId,"likes",true,resultCallback)
                            }
                        })
                    }
                })
            } else {

                  removeFromUserLikes(
                      mLike,
                      object : Result{
                          override fun onSuccess(any: Any?) {
                              deleteSentMessage(likedProduct.sellerId,mLike.id,object : Result {
                                  override fun onSuccess(any: Any?) {
                                      updateCountForUserInfo(likedProduct.sellerId,"likes",false,resultCallback)
                                  }
                              })
                          }
                      }
                  )

            }
        }
    })
}

/**
 * Updates like count of specified product
 */
private fun updateLikeCount(productId: String,count: Int,result: Result) {
    log("Like: Update")
    FirebaseFirestore
        .getInstance()
        .collection(PRODUCTS)
        .document(productId)
        .update(LIKES_COUNT,count)
        .addOnCompleteListener {
            if (it.isSuccessful) {
                result.onSuccess()
            } else {
                result.onFailed()
            }
        }
}

/**
 * Removes liked prodyuct from cached list
 */
 fun removeFromUserLikesCache(like: Like): Boolean {
    return userLikedProducts.remove(like)
}

/**
 *Add liked product to cache
 */
 fun addLikedProductToCache(like: Like) : Boolean {
    return userLikedProducts.add(like)
}

/**
 * Removes liked product from user likes
 */
private fun removeFromUserLikes(like: Like,result: Result) {
    toast("Like id ${like.id}")
    FirebaseFirestore
        .getInstance()
        .collection(USER_LIKES)
        .document(like.id)
        .delete()
        .addOnCompleteListener {
            if (it.isSuccessful) {
                result.onSuccess()
            }else {
                result.onFailed()
            }
        }
}

fun updateFieldCountForUser(userId: String,fieldName: String,count:Int,result: Result) {
    FirebaseFirestore
        .getInstance()
        .collection(USERS)
        .document(userId)
        .update(fieldName,count)
        .addOnCompleteListener {
            if (it.isSuccessful) {
                result.onSuccess()
            } else {
                result.onFailed()
            }
        }
}

/**
 * Send message to seller which product was liked
 */
private fun messageToSeller(message: Message,result: Result) {
    FirebaseFirestore
        .getInstance()
        .collection(USERS)
        .document(message.recieverId)
        .collection(USER_MESSAGES)
        .document(message.id)
        .set(message)
        .addOnCompleteListener {
            if (it.isSuccessful){
                setMessagesCount(message.recieverId,true)
                setUnreadMessagesCount(message.recieverId,+1)
                result.onSuccess()
            } else {
                result.onFailed()
            }
        }
}

/**
 * Remove message
 */
private fun deleteSentMessage(userId: String,messageId: String,result: Result){
    FirebaseFirestore
        .getInstance()
        .collection(USERS)
        .document(userId)
        .collection(USER_MESSAGES)
        .document(messageId)
        .delete()
        .addOnCompleteListener {
            if (it.isSuccessful){
                setMessagesCount(userId,false)
                result.onSuccess()
            } else {
                result.onFailed()
            }
        }
}

/**
 * Adds liked product to user likes
 */
 fun addToUserLikes(like: Like,result: Result) {
    FirebaseFirestore
        .getInstance()
        .collection(USER_LIKES)
        .document(like.id)
        .set(like)
        .addOnCompleteListener {
        if (it.isSuccessful){
            result.onSuccess()
        } else {
            result.onFailed()
        }
    }
}

/**
 * Loads info about seller
 */
suspend fun loadSellerInfo(@SellerInfo sellerId: String,result: ResultCallback<SellerAccount>) = withContext(Dispatchers.Default){
    FirebaseFirestore
        .getInstance()
        .collection(USERS)
        .document(sellerId)
        .collection(USER_SELLER)
        .document(SELLER_INFO)
        .get()
        .addOnCompleteListener { it ->
            if (it.isSuccessful){
                it.result?.let {
                    documentSnapshot ->

                    documentSnapshot.toObject(SellerAccount::class.java)?.let {
                        result.onSuccess(it)
                    }
                }
            } else {
                result.onFailed(it.exception)
            }
        }
}

    suspend fun loadBannerData(result: Result){
        return withContext(Dispatchers.IO) {
            FirebaseFirestore.getInstance().collection("bannerImages").get().addOnCompleteListener {
                if (it.isSuccessful){
                    result.onSuccess(it.result!!.documents)
                }
            }
        }
    }
    suspend fun loadCategoryData(result: Result){
        return withContext(Dispatchers.IO) {
            FirebaseFirestore.getInstance().collection("categories").get().addOnCompleteListener {
                if (it.isSuccessful){
                    result.onSuccess(it.result!!.documents)
                }
            }
        }
    }


    suspend fun loadMainCatgeory(result: Result){
        return  withContext(Dispatchers.IO) {
            FirebaseFirestore.getInstance().collection("mainCategories").get().addOnCompleteListener {
                if (it.isSuccessful){
                    result.onSuccess(it.result!!.documents)
                }
            }
        }
    }

    fun getShippingLocations(result: Result) {
        if (currentUser == null){
            result.onFailed()

            return
        }
        FirebaseFirestore.getInstance().collection(USERS).document(currentUser!!.id).collection(SHIPPING_LOCATIONS).get().addOnCompleteListener { it ->
            if (it.isSuccessful){
                it.result?.apply {
                    if (documents.size>0){
                        val list = arrayListOf<ShippingLocation>()
                        for (i in documents){
                            i.toObject(ShippingLocation::class.java)?.let {
                                list.add(it)
                            }
                        }
                        result.onSuccess(list)
                    } else {
                        result.onFailed()
                    }
                }
            } else {
                result.onFailed()
            }
        }.addOnFailureListener {
            result.onFailed()
        }.addOnCanceledListener {
            result.onFailed()
        }
    }

    suspend fun uploadProduct(product: Product, description: String, photoList: ArrayList<String>, options: ArrayList<Specification>, result: Result,context: CoroutineScope){
        context.launch(Dispatchers.IO){
                val productID = System.currentTimeMillis().toString()

                if (currentUser==null){
                    result.onFailed()
                }

                product.apply {
                    id = productID
                    sellerId = currentUser!!.id
                }

                uploadPhoto(photoList,productID,0,object : Result{
                    override fun onSuccess(any: Any?) {

                        if (any!=null&&any is Pair<*, *>){
                            product.apply {
                                photo = (any.first as Map<*, *>)["url"].toString()
                                scaleRatio = any.second as Float
                            }
                        }

                        FirebaseFirestore.getInstance().collection("products").document(productID).set(product).addOnCompleteListener { it ->
                            if (it.isSuccessful){
                                FirebaseFirestore.getInstance().collection("products").document(productID).collection("details").document("description").set(
                                    mapOf(Pair("text",description))).addOnCompleteListener { it ->
                                    if (it.isSuccessful){
                                        if (options.isNotEmpty()){
                                            FirebaseFirestore.getInstance().collection("products").document(productID).collection("details").document("options").set(mapOf(Pair("options",options))).addOnCompleteListener {
                                                if (it.isSuccessful){
                                                    result.onSuccess()
                                                }
                                            }
                                        } else {
                                            result.onSuccess()
                                        }
                                    }else {
                                        result.onFailed()
                                    }
                                }
                            } else {
                                result.onFailed()
                            }
                        }
                    }

                    override fun onFailed(message: String?) {
                        result.onFailed()
                    }
                },ArrayList(),context)


        }
    }
      fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
          //Check the thread
          val mainThread = Looper.myLooper() == Looper.getMainLooper()
          if (mainThread) {
              Log.w("Get image uri","Make sure you are calling this methon in background thread")
          }
          val path = Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
          return Uri.parse(path)
    }

    fun getDetailsForProduct(productId: String,result: Result) {
        FirebaseFirestore.getInstance().collection(PRODUCTS).document(productId).collection(DETAILS).get().addOnCompleteListener { it ->
            val dataResult = it.result
            if (it.isSuccessful&&dataResult!=null){
                val details = Details()
                val documents = dataResult.documents
                documents.apply {

                    var photos = getOrNull(2)
                    var description = getOrNull(0)
                    var dostavka = getOrNull(DOSTAVKA_ID)
                    var options = getOrNull(1)

                        photos?.let {
                            if (it.exists()) {
                                var array:ArrayList<String>? = arrayListOf()
                                var arrayPhotos = it[PHOTOS]
                                var hashMap: ArrayList<HashMap<String, String>>? =
                                    arrayPhotos as ArrayList<HashMap<String, String>>?

                                    hashMap?.let {
                                        for (i in it) {
                                            val url = i["url"]
                                            if (url != null) {
                                                array?.add(url)
                                            }
                                        }
                                        if (!array.isNullOrEmpty()){
                                            log("Photos not null")
                                            details.photos = array
                                        } else {
                                            log("Photos null")
                                            array = null
                                        }
                                        hashMap = null
                                        arrayPhotos = null
                                    }
                            }
                        }
                        description?.let {
                            if (it.exists()){
                                val mDescription = it[TEXT]
                                mDescription?.let {
                                    log("Description not null")
                                    details.description = it as String
                                }
                            }
                        }

                        dostavka?.let {
                            if (it.exists()){
                                val mDostavka = it.toObject(Shipping::class.java)
                                mDostavka?.let {
                                    log("Shipppint not null")
                                    details.shipping = mDostavka
                                }
                            }
                        }

                        options?.let {
                            if (it.exists()){
                                var mOptions: ArrayList<Specification>? = arrayListOf()
                                val hashMap = it["options"] as ArrayList<*>?

                                hashMap?.forEach { specMap ->
                                    val map = specMap as Map<*, *>
                                    val specification = Specification()
                                    specification.id = (map["id"] as Long).toInt()
                                    specification.name = map["name"] as String
                                    specification.value = map["value"] as String
                                    mOptions?.add(specification)
                                }
                                if (!mOptions.isNullOrEmpty()){
                                    log("Options not null")
                                    details.specification = mOptions
                                } else {
                                    log("Options null")
                                    mOptions = null
                                }
                            }
                        }
                        photos = null
                        description = null
                        options = null
                        dostavka = null

                        result.onSuccess(details)


                }
            } else {
                it.exception?.message?.let {
                    result.onFailed(it)
                }
            }
    }.addOnCanceledListener {
        result.onFailed()
        }.addOnFailureListener {
            result.onFailed()
        }
   }

   fun updateUserPhoto(path: String,result: Result) {
       uploadImageFromPath(path,{
           if (it!=null) {
               currentUser?.let { us->

                   us.photo = it

                   getUserReference(us.id).child("photo").setValue(it).addOnSuccessListener {
                       result.onSuccess()
                   }.addOnFailureListener {
                       result.onFailed()
                   }
               }
           } else {
               result.onFailed()
           }
       })
   }

    private fun uploadPhoto(list: ArrayList<String>, productId: String, index: Int, result: Result,downlaodUrls: ArrayList<Map<String,String>>,context: CoroutineScope) {
        var scaleRatio = 1f
        if (index==0) {
            BitmapFactory.decodeFile(list[index]).apply {
                scaleRatio = height / width.toFloat()
                recycle()
            }
        }
        AndroidUtilities.runOnUIThread {
            toast("Upload photo")
        }
        var id = "${System.currentTimeMillis()}.png"
        val path = list[index]

        BitmapFactory.decodeFile(path)?.let {
            context.launch(Dispatchers.IO) {
                uploadImageFile(it,id,object : ImageKitCallback {
                    override fun onSuccess(uploadResponse: UploadResponse?) {
                        try {
                            it.recycle()
                        }catch (e: Exception){

                        }
                        uploadResponse?.let {
                            id = it.name
                        }
                        downlaodUrls.add(mapOf(Pair("url",id)))
                        if (list.size > index + 1) {
                            uploadPhoto(list, productId, index, result, downlaodUrls, context)
                        } else {
                            FirebaseFirestore.getInstance()
                                .collection("products")
                                .document(productId)
                                .collection("details")
                                .document("photos")
                                .set(mapOf(Pair("photos",downlaodUrls)))
                                .addOnCompleteListener {
                                    if (it.isSuccessful){
                                        result.onSuccess(Pair(downlaodUrls[0],scaleRatio))
                                    }
                                }
                        }
                    }

                    override fun onError(uploadError: UploadError) {
                        AndroidUtilities.runOnUIThread {
                            toast("Uploading message error ${uploadError.message}")
                        }
                    }
                })
            }
        }

    }

    fun getUserIdentity() = currentUser?.seller?: false

    var currentUserLiveData = MutableLiveData<User>(currentUser)

    suspend fun setUp(r: Result) = withContext(Dispatchers.IO) {
        val id = if (currentUser!=null) currentUser!!.id else FirebaseAuth.getInstance().uid
        id?.let {
            listenForUserChange(id,object : ResultCallback<User>{
                override fun onSuccess(result: User?) {
                    try {
                        if (result!=null) {
                            currentUser = result
                            toast("user changed")
                            SharedConfig.getInstance().saveUserConfig()
                        } else {
                            r.onFailed()
                        }
                    }catch (e: java.lang.Exception) {

                    }
                }
            })
        }

    }

    suspend fun createSellerAccount(result: Result,sellerAccount: User) = withContext(Dispatchers.IO) {
        sellerAccount.let { it ->
            if (checkCurrentUser()){
                getUserReference(sellerAccount.id).apply {
                    child("seller").setValue(true)
                    child("phone").setValue(sellerAccount.phone)
                    child("shippingLocation").setValue(sellerAccount.shippingLocation).addOnCompleteListener {
                        if (it.isSuccessful) {
                            result.onSuccess()
                        } else {
                            result.onFailed()
                        }
                    }
                }
            }else {
                result.onFailed()
            }
        }
    }

    suspend fun shareProduct(product: Product,context: Context) {
        try {
            withContext (Dispatchers.IO){
                    var path = ""
                    var file: File? = null
                    try {
                        file = Glide.with(MyApplication.appContext).asFile().load(product.photo).submit().get()
                    } catch (e: ExecutionException) {
                        e.printStackTrace()
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    } finally {
                        if (file != null && file.exists()) {
                            path = file.absolutePath
                            createChooser(path, product.title,context)
                        }
                    }

            }
        } catch (e: java.lang.Exception) {
            if (e.message == null) {
                return
            }
        }
    }

      fun signOut(){
          currentUser = null
          SharedConfig.getInstance().saveUserConfig()
          FirebaseAuth.getInstance().signOut()
    }

    suspend fun loginUser(email: String,password: String,res: Result) {
        withContext(Dispatchers.IO) {
        if (currentUser!=null){
            FirebaseAuth.getInstance().signOut()
            currentUser = null
        }
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password).addOnCompleteListener {
            if (it.isSuccessful&&it.result!=null&&it.result!!.user!=null) {
                val uid = it.result!!.user!!.uid
                getUser(uid,object : ResultCallback<User>{
                    override fun onSuccess(result: User?) {
                        result?.let {
                            currentUser = result
                            SharedConfig.getInstance().saveUserConfig()
                            res.onSuccess()
                        }
                    }

                    override fun onFailed() {
                        res.onFailed()
                    }
                })
            } else {
                res.onFailed()
            }
            }
        }
    }

   fun getUserReference(id: String) = FirebaseDatabase.getInstance().getReference(USERS).child(id)

   fun getCurrentUserId() = currentUser?.id

   fun getUser(id: String,result: ResultCallback<User>){
       getUserReference(id)
           .get()
           .addOnCompleteListener { it ->
               if (it.isSuccessful) {
               it.result?.apply {
                   val user = getValue(User::class.java)
                   user?.let { us->
                       result.onSuccess(us)
                   }
               }
           } else {
               throw it.exception!!
               result.onFailed()
           }
       }
   }

fun listenForUserChange(id: String,result: ResultCallback<User>){
    getUserReference(id)
        .addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(User::class.java)?.let {
                    result.onSuccess(it)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                result.onFailed()
            }
        })

}

    fun changeUserPhoto(photo: String,result: Result) {
        currentUser?.id?.let {
            getUserReference(it)
                .child("photo")
                .setValue(photo)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        result.onSuccess()
                    } else {
                        result.onFailed()
                    }
                }
        }
    }

    suspend fun registerUser(name: String,email: String,password: String,result: Result) {
        withContext(Dispatchers.IO) {
            if (currentUser!=null){
                FirebaseAuth.getInstance().signOut()
            }
            FirebaseAuth
                .getInstance()
                .createUserWithEmailAndPassword(
                    email,
                    password
                ).addOnCompleteListener { it ->
                if (it.isSuccessful&&it.result!=null) {
                    if (it.result!!.user!=null){
                        val uid = it.result!!.user!!.uid
                        val currentTime = Calendar.getInstance()
                        val time = currentTime.get(Calendar.HOUR_OF_DAY)
                        val month= currentTime.get(Calendar.MONTH)
                        val year = currentTime.get(Calendar.YEAR)
                        val day = currentTime.get(Calendar.DAY_OF_MONTH)
                        val date = RegisteredDate(time,month,year, day,"Client")
                        currentUser = User().apply {
                            this.id = uid
                            this.name = name
                            this.email = email
                            this.password = password
                            this.registeredDate = date

                            getUserReference(uid).setValue(this).addOnCompleteListener {
                                if (it.isSuccessful) {
                                    result.onSuccess()
                                } else {
                                    result.onFailed()
                                }
                            }
                            SharedConfig.getInstance().saveUserConfig()
                        }
                    }else {
                        result.onFailed()
                    }
                } else {
                    result.onFailed()
                }
            }
        }
    }

        const val USER_CLIENT = "client"
        const val USER_SELLER = "seller"
        const val USER_IDENTITY = "NO_IDENTITIY"

        fun createChooser(photoUri: String, text: String,context: Context) {
            AndroidUtilities.runOnUIThread {
                val intent = Intent(Intent.ACTION_SEND)
                intent.putExtra(Intent.EXTRA_TEXT, text)
                if (photoUri != "") {
                    intent.type = "image/*"
                    intent.putExtra(Intent.EXTRA_STREAM, photoUri)
                    (context).startActivity(Intent.createChooser(intent, "Share product"))
                }
            }
        }

interface Result {
    fun onSuccess(any:Any? = null)
    fun onFailed(message: String?=null) = log(message)
    fun onFailed() = log("OnFailed")
}

interface ResultCallback<T> {
    fun onSuccess(result: T ?= null)
    fun onFailed(message: String ?=null) = log(message)
    fun onFailed(message: Exception ?=null) = log(message)
    fun onFailed() = log("Failed result callback")
}

 class RegisteredDate {
      var time: Int = 0
      var month: Int = 0
      var year: Int = 0
      var day: Int = 0

     constructor( time: Int, month: Int, year: Int, day: Int, who: String){
         this.time = time
         this.month = month
         this.year = year
         this.day = day
     }
     constructor()
 }

class Subscription {
    var userId = ""
    var userName = ""
    var userPhoto = ""
    var userWho = ""
    var storeId = ""
    var sellerName = ""
    var companyName = ""
    var companyPhoto = ""
    var companyNumber = ""
}

class SellerAccount {
    var id: String = ""
    var sellerName = ""
    var companyName = ""
    var companyPhoto = ""
    var aboutCompany = ""
    var contactNumber = "Not known"
    var companyAdress = ""
    var likes = 0
    var productsCount = 0
    var subscribers = 0
}

class UserInfo {
    var id: String = ""
    var name = ""
    var photo = ""
    var about = ""
    var mobileNumber = ""
    var likes = 0
    var subscribers = 0
    var subscriptions = 0
}

fun updateCountForUserInfo(userId: String,pathId: String,increase: Boolean,result: Result) {
    getUserReference(userId).child(pathId).apply {
        get().addOnCompleteListener {
            if (it.isSuccessful){
                it.result?.let {
                   val count = it.value as Long?
                    count?.let {
                        setValue(if (increase) count + 1 else count - 1)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    result.onSuccess()
                                } else {
                                    result.onFailed()
                                }
                            }
                    }
                }
            }
        }
    }
}

interface MessageCallback {
    fun onUnReadMessage(count: Int)
    fun onFailed(message: String?=null)
}
