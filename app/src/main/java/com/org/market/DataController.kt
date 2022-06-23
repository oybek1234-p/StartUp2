package com.org.market

import android.graphics.drawable.Drawable
import com.ActionBar.log
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.org.net.*
import com.org.net.models.*
import java.util.concurrent.ConcurrentHashMap

open class ResultCallback<T> {

    var isRemoved = false

    fun remove() {
        isRemoved = true
    }

    protected open fun onSuccess(data: T?) {}
    protected open fun onFailed(exception: Exception?) {
        toast(exception?.message)
    }

    fun postOnSuccess(data: T?) {
        if (isRemoved) return
        onSuccess(data)
    }

    fun postOnFailed(exception: Exception?) {
        if (isRemoved) return
        onFailed(exception)
    }


}

interface Result {
    fun onSuccess()
    fun onFailed()
}

object DataController {
    val users = ConcurrentHashMap<String, User>()
    val usersFull = ConcurrentHashMap<String, UserFull>()
    private val loadingFullUsers = ArrayList<String>()
    private val loadingUsers = ArrayList<String>()
    val productPhotos = HashMap<String, ArrayList<Photo>>()
    val loadingProductPhotos = arrayListOf<String>()

    var specifications = hashMapOf<String, ArrayList<Specification>>()

    var likedProducts = hashMapOf<String, ProductLike?>()

    fun checkProductLiked(
        productId: String,
        callback: (isLiked: Boolean, error: String?) -> Unit,
    ): Boolean {
        val checkedBefore = likedProducts.containsKey(productId)
        var isLiked: Boolean
        val fromNetwork: Boolean
        if (checkedBefore) {
            isLiked = likedProducts[productId] != null
            callback(isLiked, null)
            fromNetwork = false
        } else {
            likedProductsReference
                .document(getLikedProductId(productId))
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val productLike = it.result?.toObject(ProductLike::class.java)
                        isLiked = productLike != null
                        likedProducts[productId] = productLike
                        callback(isLiked, null)
                    } else {
                        callback(false, it.exception!!.message)
                    }
                }
            fromNetwork = true
        }
        return fromNetwork
    }

    fun uploadProductSpecifications(
        productId: String,
        specifications: ArrayList<Specification>,
        onFinished: (success: Boolean) -> Unit,
    ) {
        val product = getProductSpecificReference(productId)
        specifications.forEach {
            product.document(it.id).set(it).addOnCompleteListener { t ->
                if (it.id == specifications.last().id) {
                    onFinished(t.isSuccessful)
                }
            }
        }
    }

    fun uploadProduct(product: Product, completed: (success: Boolean) -> Unit) {
        getProductReference(product.id).set(product).addOnCompleteListener {
            completed(it.isSuccessful)
            updateUserField(product.sellerId, false, PRODUCTS, FieldValue.increment(1))
        }
    }

    fun getLikedProductId(productId: String) = productId + currentUserId()

    fun setLikedProduct(
        product: Product,
        callback: (isSuccess: Boolean, liked: Boolean, errorMessage: String?) -> Unit,
    ) {
        val cacheProduct = likedProducts[product.id]
        val remove = cacheProduct != null

        val task: Task<Void>
        if (remove) {
            likedProducts[product.id] = null
            task = likedProductsReference.document(getLikedProductId(product.id)).delete()
        } else {
            val like = ProductLike().apply {
                id = getLikedProductId(product.id)
                productId = product.id
                productPhoto = product.photo
                productPhotoScale = product.photoScaleRatio
                productTitle = product.title
                productCost = product.cost
                sellerId = product.sellerId
                userId = currentUserId()
            }
            likedProducts[product.id] = like
            task = likedProductsReference.document(like.id).set(like)
        }
        productsReference.document(product.id)
            .update("likes", FieldValue.increment(if (remove) -1 else 1))
        if (!remove) {
            val message = Message().apply {
                productId = product.id
                id = getLikedProductId(productId)
                type = MESSAGE_TYPE_PRODUCT_LIKED
                photo = product.photo
                receiverId = product.sellerId
            }
            sendMessage(message, null)
        } else {
            deleteMessage(getLikedProductId(product.id), product.sellerId, null)
        }
        task.addOnCompleteListener {
            callback(it.isSuccessful, !remove, it.exception?.message)
        }
    }

    fun updateUserField(
        userId: String,
        full: Boolean,
        field: String,
        value: Any,
        parentChild: String? = null,
    ) {
            var query = (if (full) userFullReference(userId) else userReference(userId))
            if (parentChild != null) {
                query = query.child(parentChild)
            }
            query.child(field)
                .setValue(value)
    }

    fun getSpecification(
        productId: String,
        resultCallback: ResultCallback<ArrayList<Specification>>,
    ) {
        firestoreInstance().collection(PRODUCTS).document(productId).collection(SPECIFICATIONS)
            .get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val list =
                        it.result.toObjects(Specification::class.java) as ArrayList<Specification>
                    specifications[productId] = list
                    resultCallback.postOnSuccess(list)
                } else {
                    resultCallback.postOnFailed(it.exception)
                }
            }
    }

    fun loadProductPhotos(productId: String, result: ResultCallback<ArrayList<Photo>>? = null) {
        if (loadingProductPhotos.contains(productId) && result == null) {
            return
        }
        if (result == null) {
            loadingProductPhotos.add(productId)
        }
        firestoreInstance().collection(PRODUCTS).document(productId).collection(PHOTOS).get()
            .addOnCompleteListener {
                loadingProductPhotos.remove(productId)
                if (it.isSuccessful) {
                    val photos = it.result.toObjects(Photo::class.java) as ArrayList<Photo>
                    productPhotos[productId] = photos
                    result?.postOnSuccess(photos)
                    postNotificationName(productPhotosDidLoad, productId, photos)
                } else {
                    result?.postOnFailed(it.exception)
                    postNotificationName(productPhotosDidLoad, productId, Error(it.exception))
                }
            }
    }

    fun saveUserLocation(location: ShippingLocation) {
        firestoreInstance().collection(LOCATIONS).document(location.userId).set(location)
    }

    fun getUserLocation(userId: String, callback: ResultCallback<ShippingLocation?>) {
        firestoreInstance().collection(LOCATIONS).document(userId).get().addOnCompleteListener {
            if (it.isSuccessful) {
                callback.postOnSuccess(it.result.toObject(ShippingLocation::class.java))
            } else {
                callback.postOnFailed(it.exception)
            }
        }
    }

    fun getPhotoScaleRatio(photoUrl: String, onReady: (scaleRatio: Float) -> Unit) {
        Glide.with(getApplicationContext()).load(photoUrl).priority(Priority.HIGH).into(object :
            SimpleTarget<Drawable>() {
            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                val scaleRatio =
                    resource.intrinsicHeight.toFloat() / resource.intrinsicWidth.toFloat()
                onReady(scaleRatio)
            }
        })
    }

    fun uploadProduct() {

    }

    fun uploadProductPhotos(
        productId: String,
        photosPaths: ArrayList<String>,
        resultCallback: ResultCallback<ArrayList<String>>,
        pos: Int = 0,
        urlsList: ArrayList<String> = ArrayList(),
    ) {
        val photoToUpload = photosPaths[pos]
        uploadImageFromPath(photoToUpload) { it ->
            if (it != null) {
                urlsList.add(it)
            }
            if (pos == photosPaths.size - 1) {
                if (urlsList.isEmpty()) {
                    toast("Something went wrong")
                    resultCallback.postOnFailed(null)
                    return@uploadImageFromPath
                }
                urlsList.forEach { ph ->
                    val photo = Photo().apply { url = ph }
                    firestoreInstance().collection(PRODUCTS).document(productId).collection(PHOTOS)
                        .document(photo.id).set(photo)
                }
                resultCallback.postOnSuccess(urlsList)
            } else {
                uploadProductPhotos(productId, photosPaths, resultCallback, pos + 1, urlsList)
            }
        }
    }

    var categories = arrayListOf<Category>()
    var subCategories = HashMap<String, ArrayList<Category>>()

    fun getSubCategories(catId: String) = subCategories[catId]

    fun uploadCategory(category: Category, callback: ResultCallback<*>?) {
        firestoreInstance().collection(CATEGORIES).document(category.id).set(category)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback?.postOnSuccess(null)
                } else {
                    callback?.postOnFailed(it.exception)
                }
            }
    }

    fun uploadPaymentCard(paymentCard: PayCard, resultCallback: Result) {
        firestoreInstance().collection(PAY_CARDS).document(paymentCard.id).set(paymentCard)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    resultCallback.onSuccess()
                } else {
                    resultCallback.onFailed()
                }
            }
    }

    fun loadParentCategories(
        resultCallback: ResultCallback<ArrayList<Category>>,
        hasLimit: Boolean = false,
    ) {
        firestoreInstance().collection(CATEGORIES).whereEqualTo(PARENT_ID, "")
            .limit(if (hasLimit) 5 else 100).get().addOnCompleteListener { it ->
                if (it.isSuccessful) {
                    resultCallback.postOnSuccess((it.result.toObjects(Category::class.java) as ArrayList<Category>).also { list ->
                        if (!hasLimit) {
                            categories = list
                        }
                    })
                } else {
                    resultCallback.postOnFailed(it.exception)
                }
            }
    }

    fun addCategory(cat: Category) {
        val catList: ArrayList<Category>
        if (cat.parentId.isNotEmpty()) {
            var list = subCategories[cat.parentId]
            if (list == null) {
                list = arrayListOf()
            }
            catList = list
        } else {
            catList = categories
        }
        var contains = false
        catList.forEachIndexed { index, category ->
            if (category.id == cat.id) {
                catList[index] = cat
                contains = true
                return@forEachIndexed
            }
        }
        if (!contains) {
            catList.add(cat)
        }
    }

    fun loadSubCategories(catId: String, result: ResultCallback<ArrayList<Category>>) {
        firestoreInstance().collection(CATEGORIES).whereEqualTo(PARENT_ID, catId).get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    result.postOnSuccess((it.result.toObjects(Category::class.java) as ArrayList<Category>).also { list ->
                        subCategories[catId] = list
                    })
                } else {
                    result.postOnFailed(it.exception)
                }
            }
    }

    fun loadCategory(catId: String, resultCallback: ResultCallback<Category>) {
        firestoreInstance().collection(CATEGORIES).document(catId).get().addOnCompleteListener {
            if (it.isSuccessful) {
                resultCallback.postOnSuccess(it.result.toObject(Category::class.java)?.also { cat ->
                    addCategory(cat)
                })
            } else {
                resultCallback.postOnFailed(it.exception)
            }
        }
    }

    fun deleteCategory(catId: String) {
        firestoreInstance().collection(CATEGORIES).document(catId).delete()
    }

    fun getUserFull(
        userId: String,
        force: Boolean,
        resultCallback: ResultCallback<UserFull>? = null,
        reload: Boolean = false,
    ): UserFull? {
        if (userId.isEmpty()) return null
        val userFull = usersFull[userId]
        if (force && userFull == null || reload) {
            if (!loadingFullUsers.contains(userId) || resultCallback != null) {
                if (resultCallback == null) {
                    loadingFullUsers.add(userId)
                }
                userFullReference(userId).get().addOnCompleteListener {
                    loadingFullUsers.remove(userId)
                    it.apply {
                        if (isSuccessful) {
                            parseDatabaseDocument(result, UserFull::class.java)?.let { userFull ->
                                putUserFull(userFull)

                                resultCallback?.postOnSuccess(userFull)
                                postNotificationName(userInfoDidLoad, userId, userFull)
                            }
                        } else {
                            handleException(exception)
                        }
                    }
                }
            }
        }
        return userFull
    }

    fun loadUser(userId: String, result: ResultCallback<User>) {
        if (userId.isEmpty()) return
        userReference(userId).get().addOnCompleteListener {
            if (it.isSuccessful) {
                result.postOnSuccess(it.result.getValue(User::class.java).also { user ->
                    if (user != null) {
                        putUser(user)
                    }
                })
            } else {
                result.postOnFailed(exception = it.exception)
            }
        }
    }

    fun addSnapshotUser(userId: String, onChange: (user: UserFull) -> Unit): ValueEventListener {
        return userFullReference(userId).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                toast(error.message)
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(UserFull::class.java)
                    if (user != null) {
                        putUserFull(user)
                        onChange(user)
                        postNotificationName(userInfoDidLoad, userId)
                    }
                }
            }
        })
    }

    var callbacks = ArrayList<ResultCallback<*>>()

    fun addCallback(callback: ResultCallback<*>?) {
        if (callback == null) return
        callbacks.add(callback)
    }

    fun cancelCallback(callback: ResultCallback<*>) {
        callbacks.remove(callback)
    }

    fun needRunCallback(callback: ResultCallback<*>?): Boolean {
        if (callback == null) return false
        return callbacks.contains(callback)
    }

    fun registerUser(email: String, password: String, result: ResultCallback<UserFull>) {
        addCallback(result)
        authInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                val newUserFull = UserFull()
                newUserFull.user.apply {
                    it.result.user?.apply {
                        id = uid
                        name = displayName ?: ""
                        phone = phoneNumber ?: ""
                        photo = photoUrl?.toString() ?: ""
                    }
                    if (id == "") {
                        id = currentTimeMillis().toString()
                    }
                }
                newUserFull.email = email
                if (!needRunCallback(result)) {
                    return@addOnCompleteListener
                }
                userFullReference(newUserFull.user.id).setValue(newUserFull).addOnCompleteListener {
                    if (it.isSuccessful) {
                        if (needRunCallback(result)) {
                            UserConfig.apply {
                                clearConfig()
                                userFull = newUserFull
                                saveConfig()
                            }
                            putUserFull(newUserFull)
                            result.postOnSuccess(newUserFull)
                        }
                    } else {
                        result.postOnFailed(Exception("Can't load to database"))
                    }
                }
            } else {
                result.postOnFailed(it.exception)
            }
        }
    }

    fun loginUser(email: String, password: String, result: ResultCallback<UserFull>) {
        authInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                val uid = it.result.user?.uid ?: authInstance().currentUser?.uid!!

                toast("Uid $uid")
                getUserFull(uid, true,
                    object : ResultCallback<UserFull>() {
                        override fun onSuccess(data: UserFull?) {
                            if (data != null) {
                                UserConfig.apply {
                                    clearConfig()
                                    userFull = data
                                    putUserFull(userFull)
                                    saveConfig()
                                }
                                result.postOnSuccess(data)
                            } else {
                                authInstance().signOut()

                            }
                        }

                        override fun onFailed(exception: Exception?) {
                            result.postOnFailed(exception)
                        }
                    }, true)
            } else {
                result.postOnFailed(it.exception)
            }
        }
    }

    fun updateUserInfo(
        userId: String,
        name: String? = null,
        photo: String? = null,
        bio: String? = null,
        phone: String? = null,
        likes: Int? = 0,
        products: Int? = 0,
        subscribers: Int? = null,
        subscriptions: Int? = null,
        lastSeenTime: Long? = null,
        status: UserStatus? = null,
        activeOrders: Int? = null,
        ordersInCart: Int? = null,
        messages: Int? = null,
        unreadMessages: Int? = null,
    ) {
        val updateFields = HashMap<String, Any>().apply {
            name?.let { put(NAME, it) }
            photo?.let { put(PHOTO, it) }
            phone?.let { put(PHONE, it) }
            likes?.let { put(LIKES, it) }
            products?.let { put(PRODUCTS, it) }
            subscribers?.let { put(SUBSCRIBERS, it) }
            subscriptions?.let { put(SUBSCRIPTIONS, it) }
            lastSeenTime?.let { put(LAST_SEEN_TIME, it) }
            status?.let { put(STATUS, it) }
        }
        if (updateFields.isNotEmpty()) {
            userReference(userId).updateChildren(updateFields)
        }
        updateFields.apply {
            clear()
            activeOrders?.let { put(ACTIVE_ORDERS, it) }
            bio?.let { put(BIO, it) }
            ordersInCart?.let { put(ORDERS_IN_CART, it) }
            messages?.let { put(MESSAGES, it) }
            unreadMessages?.let { put(UNREAD_MESSAGES, it) }
        }
        if (updateFields.isNotEmpty()) {
            userFullReference(userId).updateChildren(updateFields)
        }
    }

    const val MY_PRODUCTS = 1010

    var products = ConcurrentHashMap<Any, ArrayList<Product>>()

    enum class ProductFilter {
        PriceUp,
        PriceDown,
        MostLiked,
        Popular,
        Newest,
        Oldest
    }

    private val loadingRequests = ArrayList<Any>()

    fun loadBanners(result: ResultCallback<ArrayList<Banner>?>) {
        firestoreInstance().collection(BANNERS).get().addOnCompleteListener {
            if (it.isSuccessful) {
                val list = it.result.toObjects(Banner::class.java)
                result.postOnSuccess(list as ArrayList<Banner>)
            } else {
                result.postOnFailed(it.exception)
            }
        }
    }

    fun loadBanner(bannerId: String, result: ResultCallback<Banner>) {
        firestoreInstance().collection(BANNERS).document(bannerId).get().addOnCompleteListener {
            if (it.isSuccessful) {
                result.postOnSuccess(it.result.toObject(Banner::class.java))
            } else {
                result.postOnFailed(it.exception)
            }
        }
    }

    fun uploadBanner(banner: Banner, resultCallback: Result?) {
        firestoreInstance().collection(BANNERS).document(banner.id).set(banner)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    resultCallback?.onSuccess()
                } else {
                    resultCallback?.onFailed()
                }
            }
    }

    fun deleteBanner(bannerId: String) {
        firestoreInstance().collection(BANNERS).document(bannerId).delete()
    }

    fun loadProducts(
        requestedId: Any,
        limitSize: Long = 8L,
        filter: ProductFilter,
        brand: String? = null,
        priceRange: Pair<Long, Long>? = null,
        freeShipping: Boolean = false,
        resultCallback: ResultCallback<ArrayList<Product>>? = null,
        includeAll: Boolean = true,
        sellerId: String? = null,
        reload: Boolean = false,
        catId: String? = null,
        force: Boolean = false,
    ) {
        if (loadingRequests.contains(requestedId) && !force) return
        loadingRequests.remove(requestedId)
        loadingRequests.add(requestedId)
        var startAfter: String? = null
        var list = products[requestedId]
        if (list == null) {
            list = ArrayList()
            products[requestedId] = list
        } else {
            if (!reload) {
                startAfter = list.lastOrNull()?.id
            } else {
                list.clear()
            }
        }

        var query = productsReference.limit(limitSize)

        if (priceRange != null) {
            if (priceRange.first != -1L) {
                query = query.whereGreaterThanOrEqualTo(COST, priceRange.first)
            }
            if (priceRange.second != -1L) {
                query = query.whereLessThanOrEqualTo(COST, priceRange.second)
            }
        } else {
            query = when (filter) {
                ProductFilter.PriceDown -> query.orderBy(COST, Query.Direction.DESCENDING)
                ProductFilter.PriceUp -> query.orderBy(COST, Query.Direction.ASCENDING)
                ProductFilter.Newest -> query.orderBy(ID, Query.Direction.DESCENDING)
                ProductFilter.Oldest -> query.orderBy(ID, Query.Direction.ASCENDING)
                ProductFilter.MostLiked -> query.orderBy(LIKES, Query.Direction.DESCENDING)
                ProductFilter.Popular -> query.orderBy(LIKES, Query.Direction.DESCENDING)
            }
        }
        if (startAfter?.isNotEmpty() == true) {
            query = query.startAfter(startAfter)
        }
        if (brand != null) {
            query = query.whereEqualTo(BRAND, brand)
        }
        if (sellerId != null) {
            query = query.whereEqualTo(SELLER_ID, sellerId)
        }
        if (freeShipping) {
            query = query.whereEqualTo(FREE_SHIPPING, true)
        }
        if (catId != null) {
            query = query.whereEqualTo(CATEGORY_ID, catId)
        }
        query.get().addOnCompleteListener {
            it.apply {
                loadingRequests.remove(requestedId)
                if (!isSuccessful) {
                    resultCallback?.postOnFailed(exception)
                    return@addOnCompleteListener
                }
                val newList = parseFirestoreDocuments(result.documents, Product::class.java)
                list.addAll(newList)
                resultCallback?.postOnSuccess(if (includeAll) list else newList)
                postNotificationName(newProductsDidLoad, requestedId, newList)
            }
        }

    }

    fun clearProducts(requestId: Int) {
        products[requestId]?.clear()
    }

    fun findProduct(id: String): Product? {
        products.forEach {
            it.value.forEach { pr ->
                if (pr.id == id) {
                    return pr
                }
            }
        }
        return null
    }

    fun getProducts(requestId: Any): ArrayList<Product> {
        var list = products[requestId]
        if (list == null) {
            list = ArrayList()
            products[requestId] = list
        }
        return list
    }

    fun putUser(user: User): Boolean {
        user.apply {
            users[id] = this
            var userFull = usersFull[id]
            if (userFull == null) {
                userFull = UserFull()
            }
            userFull.user = this
            UserConfig.apply {
                if (user().id == id) {
                    userFull.user = user
                    saveConfig()
                }
            }
        }
        return true
    }

    fun putUserFull(userFull: UserFull) {
        userFull.apply {
            usersFull[user.id] = this
            users[userFull.user.id] = userFull.user
            UserConfig.apply {
                if (user().id == user.id) {
                    this.userFull = userFull
                    saveConfig()
                }
            }
        }
    }

    fun getUser(id: String): User? {
        return users[id] ?: usersFull[id]?.user
    }

    fun getPopularSearchList(result: ResultCallback<ArrayList<SearchProduct>>) {
        productSearchesReference.orderBy(REQUESTS_COUNT, Query.Direction.DESCENDING).limit(6).get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val searches =
                        parseFirestoreDocuments(it.result.documents, SearchProduct::class.java)
                    result.postOnSuccess(searches)
                } else {
                    result.postOnFailed(it.exception)
                }
            }
    }

    fun saveUserInfo(userId: String, fieldName: String, data: Any, full: Boolean = false) {
        val reference = if (full) userFullReference(userId) else userReference(userId)
        reference.child(fieldName).setValue(data)
    }

    var allMessages = hashMapOf<String, ArrayList<Message>>()

    fun sendMessage(message: Message, resultCallback: Result?) {
        message.apply {
            senderId = currentUserId()
            senderName = currentUser().name
            senderPhoto = currentUser().photo
        }
        increaseUserMessagesCount(message.receiverId, unread = true,true)
        increaseUserMessagesCount(message.receiverId, unread = false,true)
        messageReference.document(message.id).set(message).addOnCompleteListener {
            if (it.isSuccessful) {
                resultCallback?.onSuccess()
            } else {
                resultCallback?.onFailed()
            }
        }
    }

    fun clearUnreadMessages(userId: String) {
        userFullReference(userId).child("messagesCount").child("unreadMessages").setValue(0)
    }

    fun getMessages(userId: String, type: Int): ArrayList<Message> {
        var messages = allMessages[userId]
        if (messages == null) {
            messages = ArrayList()
            allMessages[userId] = messages
        }
        return if (type == MESSAGE_TYPE_ALL) messages else messages.filter { it.type == type } as ArrayList<Message>
    }

    fun putMessages(userId: String,messages: ArrayList<Message>) {
        getMessages(userId, MESSAGE_TYPE_ALL).apply {
            addAll(0,messages)
            val filtered = distinctBy { it.id }
            clear()
            addAll(filtered)
        }
    }

    fun loadNextMessages(
        userId: String,
        type: Int,
        callback: Result,
        loadNewMessages: Boolean = false
    ) {
        val list = getMessages(userId, type)
        val endMessageId = list.firstOrNull()?.sendDate

        val lastMessageId = list.lastOrNull()?.sendDate

        var query = messageReference.orderBy("sendDate", Query.Direction.DESCENDING)
            .whereEqualTo("receiverId", userId)

        if (type != MESSAGE_TYPE_ALL) {
            query = query.whereEqualTo(TYPE, type)
        }

        if (loadNewMessages) {
            query = query.endBefore(endMessageId)
        } else {
            if (lastMessageId != null) {
                query = query.startAfter(lastMessageId)
            }
        }

        query.get().addOnCompleteListener {
            if (it.isSuccessful) {
                val newMessages = it.result.toObjects(Message::class.java)
                putMessages(userId,newMessages as ArrayList<Message>)
                callback.onSuccess()
            } else {
                callback.onFailed()
            }
        }
    }

    interface MessageChangeCallback {
        fun onChanged(messageCount: MessagesCount)
    }

    fun ValueEventListener.remove(userId: String) {
        userFullReference(userId).child("messagesCount").removeEventListener(this)
    }

    fun startMessageSnapshot(
        userId: String,
        onChange: (messageCount: MessagesCount) -> Unit,
    ): ValueEventListener {
        return userFullReference(userId).child("messagesCount")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val count = snapshot.getValue(MessagesCount::class.java)
                    if (count != null) {
                        UserConfig.userFull.messagesCount = count
                        UserConfig.saveConfig()
                        onChange(count)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    toast("Error ${error.message}")
                }
            })
    }

    fun newMessageId(id: String) = currentUserId() + id

    fun deleteMessage(messageId: String, receiverId: String, result: Result?) {
        messageReference.document(messageId).delete().addOnCompleteListener {
            if (it.isSuccessful) {
                increaseUserMessagesCount(receiverId, unread = false,false)
                result?.onSuccess()
            } else {
                result?.onFailed()
            }
        }
    }

    fun increaseUserMessagesCount(userId: String,unread: Boolean,increase: Boolean) {
        updateUserField(userId,true,if (unread) UNREAD_MESSAGES else MESSAGES,ServerValue.increment(if (increase) 1 else -1),"messagesCount")
    }

    var subscribedUsers = hashMapOf<String, Subscriber?>()

    fun getIdForSubscriber(currentUserId: String, subscId: String) = "$currentUserId|$subscId"

    fun checkSubscribed(userId: String, result: (subscribed: Boolean) -> Unit): Boolean {
        if (currentUserId().isEmpty()) return false
        val checkedBefore = subscribedUsers.containsKey(userId)
        var subscribed: Boolean
        val fromNetwork: Boolean
        if (checkedBefore) {
            subscribed = subscribedUsers[userId] != null
            result(subscribed)
            fromNetwork = false
        } else {
            subscribersReference.document(getIdForSubscriber(currentUserId(), userId)).get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val subs = it.result.toObject(Subscriber::class.java)
                        subscribed = subs != null
                        subscribedUsers[userId] = subs
                        result(subscribed)
                    }
                }
            fromNetwork = true
        }
        return fromNetwork
    }

    fun subscribeToUser(
        userId: String,
        callback: (isSuccess: Boolean, subscribed: Boolean) -> Unit,
    ) {
        if (currentUserId().isEmpty()) return
        if (!subscribedUsers.containsKey(userId)) {
            checkSubscribed(userId) {
                subscribeToUser(userId, callback)
            }
            return
        }
        val subscribed = subscribedUsers[userId]
        val subscribe = subscribed == null

        val subId = getIdForSubscriber(currentUserId(), userId)
        val task: Task<*> = if (subscribe) {
            subscribersReference.document(subId).set(Subscriber(subId).also {
                subscribedUsers[userId] = it
            })
        } else {
            subscribedUsers[userId] = null
            subscribersReference.document(subId).delete()
        }
        task.addOnCompleteListener {
            callback(it.isSuccessful, subscribe)
            updateUserField(currentUserId(),
                false,
                SUBSCRIPTIONS,
                ServerValue.increment(if (subscribe) +1 else -1))
            updateUserField(userId,
                false,
                SUBSCRIBERS,
                ServerValue.increment(if (subscribe) +1 else -1))
            val currentUser = currentUser()
            if (subscribe) {
                val message = Message(
                    id = subId,
                    receiverID = userId,
                    type = MESSAGE_TYPE_SUBSCRIBED,
                    senderId = currentUser.id,
                    senderName = currentUser.name,
                    senderPhoto = currentUser.photo)
                sendMessage(message, null)
            } else {
                deleteMessage(subId, userId, null)
            }
        }
    }
}