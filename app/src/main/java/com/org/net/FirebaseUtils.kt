package com.org.net

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.*
import com.google.firebase.firestore.model.ResourcePath
import com.google.firebase.storage.FirebaseStorage
import com.org.net.models.*
import java.lang.Exception
import java.util.HashMap
import java.util.concurrent.Executors

fun firebaseInstance() = FirebaseApp.getInstance()
fun firestoreInstance() = FirebaseFirestore.getInstance()
fun databaseInstance() = FirebaseDatabase.getInstance()
fun storageInstance() = FirebaseStorage.getInstance()
fun authInstance() = FirebaseAuth.getInstance()

fun handleException(exception: Exception?) {
    //Handle exception
}

private val firestoreSnapshots = HashMap<Int, ListenerRegistration>()
private val firestoreReferences = HashMap<String, DocumentReference>()

fun CollectionReference.addNewSnapshotListener(
    requestId: Int,
    listener: EventListener<QuerySnapshot>,
) {
    removeSnapshotListener(requestId)
    val registration = addSnapshotListener(listener)
    addNewSnapshotListenerToList(requestId, registration)
}

fun removeSnapshotListener(requestId: Int) {
    firestoreSnapshots[requestId]?.remove()
}

fun addNewSnapshotListenerToList(requestId: Int, listener: ListenerRegistration) {
    firestoreSnapshots[requestId] = listener
}

fun DocumentReference.addNewSnapshotListener(
    requestId: Int,
    listener: EventListener<DocumentSnapshot>,
) {
    removeSnapshotListener(requestId)
    val registration = addSnapshotListener(Executors.newSingleThreadExecutor(), listener)
    addNewSnapshotListenerToList(requestId, registration)
}

fun removeAllSnapshots() {
    firestoreSnapshots.forEach {
        it.value.remove()
    }
}

fun CollectionReference.getDocument(documentPath: String): DocumentReference {
    var oldRef = firestoreReferences[documentPath]
    if (oldRef == null) {
        oldRef = document(documentPath)
    }
    return oldRef
}

var usersReference = databaseInstance().getReference(USERS)
fun userFullReference(userId: String) = usersReference.child(userId)
fun userReference(userId: String) = userFullReference(userId).child(USER)

var productsReference = firestoreInstance().collection(PRODUCTS)
fun getProductReference(productId: String) = productsReference.getDocument(productId)

var productSearchesReference = firestoreInstance().collection(PRODUCT_SEARCHES)
var likedProductsReference = firestoreInstance().collection(LIKED_PRODUCTS)
var messageReference = firestoreInstance().collection(MESSAGES)

var subscribersReference = firestoreInstance().collection(SUBSCRIBERS)

fun getProductSpecificReference(productId: String) =  getProductReference(productId).collection(SPECIFICATIONS)







