package com.cashbacks.app.model

import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import kotlin.reflect.KMutableProperty0

@Stable
internal interface Updatable {
    val updatedProperties: SnapshotStateMap<String, Pair<String, String>>

    val haveChanges: Boolean get() = updatedProperties.isNotEmpty()

    infix fun <T> KMutableProperty0<T>.updateTo(newValue: T): KMutableProperty0<T> {
        val previousValue = get()
        set(newValue)

        val changeHistory = updatedProperties[name]
        when {
            changeHistory == null -> {
                updatedProperties[name] = previousValue.toString() to newValue.toString()
            }

            changeHistory.first == newValue.toString() -> updatedProperties.remove(name)

            else -> updatedProperties[name] = changeHistory.copy(second = newValue.toString())
        }

        return this
    }
}