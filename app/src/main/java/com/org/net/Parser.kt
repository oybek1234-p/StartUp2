package com.org.net

import com.ActionBar.log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.firestore.DocumentSnapshot
import java.lang.Exception

fun <T> parseFirestoreDocuments(list: List<DocumentSnapshot?>, clazz: Class<T>): ArrayList<T> {
    val result = ArrayList<T>()
    try {
        list.forEach { doc ->
            if (doc != null) {
                parseFirestoreDocument(doc, clazz)?.let { parsed ->
                    result.add(parsed)
                }
            } else {
                log("Null document ${list.javaClass.canonicalName}")
            }
        }
    } catch (e: Exception) {
        log(e)
    }
    return result
}

fun <T> parseFirestoreDocument(documentSnapshot: DocumentSnapshot, clazz: Class<T>): T? {
    try {
        return documentSnapshot.toObject(clazz)
    } catch (e: Exception) {
        log(e)
    }
    return null
}

fun <T> parseDatabaseDocument(dataSnapshot: DataSnapshot?, clazz: Class<T>): T? {
    try {
        return dataSnapshot?.getValue(clazz)
    } catch (e: Exception) {
        log(e)
    }
    return null
}

fun <T> parseDatabaseDocuments(dataSnapshot: DataSnapshot?, clazz: Class<T>): ArrayList<T> {
    val result = ArrayList<T>()
    try {
        dataSnapshot?.children?.forEach {
            if (it != null) {
                parseDatabaseDocument(it, clazz)?.let { parsed ->
                    result.add(parsed)
                }
            } else {
                log("Database document null ${dataSnapshot.ref}")
            }
        }
    } catch (e: Exception) {
        log(e)
    }
    return result
}

