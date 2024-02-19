package com.cashbacks.app.model

import androidx.compose.runtime.snapshots.SnapshotStateMap
import kotlin.reflect.KMutableProperty0

internal interface Updatable {
    val updatedProperties: SnapshotStateMap<String, Pair<String, String>>

    val haveChanges: Boolean get() = updatedProperties.isNotEmpty()

    fun <T> updateValue(property: KMutableProperty0<T>, newValue: T) {
        val previousValue = property.get()
        property.set(newValue)

        with(updatedProperties) {
            val changeHistory = this[property.name]

            when {
                changeHistory == null -> this[property.name] =
                    previousValue.toString() to newValue.toString()

                changeHistory.first == newValue.toString() -> remove(property.name)

                else -> this[property.name] = changeHistory.copy(second = newValue.toString())
            }
        }
    }
}