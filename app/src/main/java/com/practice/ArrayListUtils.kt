@file:kotlin.jvm.JvmName("ArrayListUtilsKt")

package com.practice

import java.io.*

operator fun <T> List<T>.component1(): T {
    return get(0)
}

operator fun <T> List<T>.component2(): T {
    return get(1)
}

operator fun <T> List<T>.component3(): T {
    return get(2)
}

operator fun <T> List<T>.component4(): T {
    return get(3)
}

operator fun <T> Iterable<T>.contains(element: T): Boolean {
    if (this is Collection)
        return contains(element)
    return indexOf(element) >= 0
}

fun <T> Iterable<T>.elementAt(index: Int): T {
    if (this is List) return get(index)
    return elementAtOrElse(index) { throw IndexOutOfBoundsException("Index out of bounds") }
}

fun <T> List<T>.elementAt(index: Int): T {
    return get(index)
}

fun <T> Iterable<T>.elementAtOrElse(index: Int, defaultValue: (Int) -> T): T {
    if (this is List) return getOrElse(index, defaultValue)
    if (index < 0) return defaultValue(index)
    val iterator = iterator().withIndex()

    while (iterator.hasNext()) {
        val elementWithIndex = iterator.next()
        if (elementWithIndex.index == index) {
            return elementWithIndex.value
        }
    }
    return defaultValue(index)
}

fun <T> List<T>.elementAtOrElse(index: Int, defaultValue: (Int) -> T): T {
    return getOrNull(index) ?: defaultValue(index)
}

fun <T> Iterable<T>.elementAtOrNull(index: Int): T? {
    if (this is List) return getOrNull(0)
    if (index < 0) return null
    val iterator = iterator().withIndex()
    while (iterator.hasNext()) {
        val next = iterator.next()
        if (next.index == index) {
            return next.value
        }
    }
    return null
}

inline fun <T> Iterable<T>.firstOrNull(predicate: (T) -> Boolean): T? {
    for (i in this) {
        if (predicate(i)) return i
    }
    return null
}

inline fun <T> Iterable<T>.find(predicate: (T) -> Boolean): T? {
    return firstOrNull(predicate)
}

fun <T> Iterable<T>.first(): T {
    return when (this) {
        is List -> this.first()
        else -> {
            val iterator = iterator()
            if (!iterator.hasNext()) throw NoSuchElementException("Collection is empty")
            iterator.next()
        }
    }
}

fun <T> Iterable<T>.first(predicate: (T) -> Boolean): T {
    for (e in this) if (predicate(e)) return e
    throw NoSuchElementException("Collection is empty")
}

inline fun <T, R> Iterable<T>.firstNotNullOfOrNull(transform: (T) -> R?): R? {
    for (e in this) {
        val transformed = transform(e)
        if (transformed != null) return transformed
    }
    return null
}

inline fun <T, R : Any> Iterable<T>.mapTest(transform: (T) -> R?): ArrayList<R?> {
    if (this is List) return map(transform) as ArrayList<R?>
    val list = arrayListOf<R?>()
    for (i in this) {
        val element = transform(i)
        if (element != null) {
            list.add(element)
        }
    }

    return list
}

fun testDistinctBy() {
    val list = arrayListOf<String>(
        "Tik tok",
        "Instagram",
        "Facebook"
    )

    val distinctList = list.distinct()
}

fun comparableTest() {
    val comparable = MyName()
    val comparable1 = MyName()
    when(comparable.compareTo(comparable1)) {
        0-> {}
        1-> {}
        2-> {}
    }
}

class MyName : Comparable<MyName> {
    var id = ""

    override fun compareTo(other: MyName): Int {
        return if (id == other.id) return 0 else if (other.id == "yes") return 1 else 3
    }
}

class FileWriteOperation: AutoCloseable{
    var file: File?=null
    var fileInputStream: FileInputStream?=null
    var fileOutputStream: FileOutputStream?=null

    fun writeFileIntoFile(file: File) {
        fileInputStream = FileInputStream(file)
        fileOutputStream = FileOutputStream(file)

    }
    override fun close() {

    }

    fun writeString() {
        val fileWriter = FileWriter(file)
        fileWriter.flush()
        fileWriter.close()

    }
}






