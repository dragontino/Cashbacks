package com.cashbacks.app.ui.managment

sealed class ListState <out T : Any> {
    data object Loading : ListState<Nothing>()
    data object Empty : ListState<Nothing>()
    data class Stable<T : Any>(val data: List<T>) : ListState<T>()

    companion object {
        fun <T : Any> fromList(list: List<T>?): ListState<T> = when {
            list == null -> Loading
            list.isEmpty() -> Empty
            else -> Stable(list)
        }
    }
}