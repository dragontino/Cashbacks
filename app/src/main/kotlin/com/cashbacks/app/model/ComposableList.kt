package com.cashbacks.app.model

import androidx.compose.runtime.snapshots.SnapshotStateList
import com.cashbacks.domain.model.ListItem

class ComposableList<T : ListItem>(
    private val items: SnapshotStateList<T>
) : MutableList<T> by items {

    constructor(items: Collection<T>) :
            this(SnapshotStateList<T>().apply { addAll(items) })

    val info: MutableMap<Long, ItemStatus> = mutableMapOf()

    enum class ItemStatus {
        Created,
        Updated,
        Deleted
    }

    override fun add(element: T): Boolean {
        info[element.id] = ItemStatus.Created
        return items.add(element)
    }

    fun update(id: Long, afterUpdate: T.() -> Unit = {}) {
        info.putIfAbsent(id, ItemStatus.Updated)
        items.find { it.id == id }?.let(afterUpdate)
    }

    override fun remove(element: T): Boolean {
        info[element.id] = ItemStatus.Deleted
        return items.remove(element)
    }
}