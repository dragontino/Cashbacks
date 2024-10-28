package com.cashbacks.app.ui.managment

sealed class ListState<out T> {
    open val data = emptyList<T>()

    data object Loading : ListState<Nothing>()
    data object Empty : ListState<Nothing>()
    data class Stable<T>(override val data: List<T>) : ListState<T>() {
        constructor(listBuilder: MutableList<T>.() -> Unit) : this(buildList<T>(listBuilder))
    }

    companion object {
        fun <T> fromList(list: List<T>?): ListState<T> = when {
            list == null -> Loading
            list.isEmpty() -> Empty
            else -> Stable(list)
        }
    }
}