package com.cashbacks.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cashbacks.domain.model.Category

class ComposableCategory(
    val id: Long = 0,
    private val initialName: String = ""
) {
    constructor(category: Category): this(
        id = category.id,
        initialName = category.name
    )

    var name by mutableStateOf(initialName)
    val isChanged get() = name != initialName


    fun mapToCategory() = Category(
        id = this.id,
        name = this.name,
        maxCashback = null
    )
}