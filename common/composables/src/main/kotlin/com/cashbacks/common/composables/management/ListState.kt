package com.cashbacks.common.composables.management

sealed class ListState<out T> {
    data object Loading : ListState<Nothing>()
    data object Empty : ListState<Nothing>()
    data class Stable<out T>(val data: List<T>) : ListState<T>() {
        constructor(listBuilder: MutableCollection<T>.() -> Unit) : this(buildList(listBuilder))
        constructor(collection: Collection<T>) : this(collection.toList())
    }

    companion object {
        fun <T> fromCollection(collection: Collection<T>?): ListState<T> = when {
            collection == null -> Loading
            collection.isEmpty() -> Empty
            else -> Stable(collection)
        }
    }
}


fun <T> Collection<T>?.toListState() = ListState.fromCollection(this)