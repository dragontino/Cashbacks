package com.cashbacks.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cashbacks.domain.model.BasicShop
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.Category

class ComposableCategory(
    val id: Long = 1,
    name: String = "",
    shops: List<BasicShop> = listOf(),
    cashbacks: List<Cashback> = listOf()
) {
    constructor(category: Category): this(
        id = category.id,
        name = category.name,
        shops = category.shops,
        cashbacks = category.cashbacks
    )

    var name by mutableStateOf(name)
    val shops = ComposableList(shops)
    val cashbacks = ComposableList(cashbacks)

    fun mapToCategory() = Category(
        id = this.id,
        name = this.name,
        shops = this.shops,
        cashbacks = this.cashbacks
    )
}