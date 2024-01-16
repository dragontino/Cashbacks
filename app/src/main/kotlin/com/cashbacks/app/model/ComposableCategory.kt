package com.cashbacks.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cashbacks.domain.model.Category

class ComposableCategory(
    val id: Long = 1,
    name: String = ""
) {
    constructor(category: Category): this(
        id = category.id,
        name = category.name
    )

    var name by mutableStateOf(name)

    fun mapToCategory() = Category(
        id = this.id,
        name = this.name,
        maxCashback = null
    )
}