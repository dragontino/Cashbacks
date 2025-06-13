package com.cashbacks.core.database.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

inline fun <T, R> Flow<List<T>>.mapList(crossinline transform: suspend (T) -> R): Flow<List<R>> {
    return this.map { list ->
        list.map { transform(it) }
    }
}