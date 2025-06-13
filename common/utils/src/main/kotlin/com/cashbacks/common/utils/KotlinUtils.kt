package com.cashbacks.common.utils

fun <T : Comparable<T>> maxOfOrNull(a: T?, b: T?): T? {
    return when {
        a == null -> b
        b == null -> a
        a >= b -> a
        else -> b
    }
}