package com.cashbacks.app.model

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cashbacks.domain.model.BasicShop
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.model.FullCategory

@Stable
class ComposableCategory(
    id: Long = 0L,
    name: String = "",
    shops: List<BasicShop> = emptyList(),
    cashbacks: List<Cashback> = emptyList()
) : Updatable {

    var id by mutableLongStateOf(id)
        private set

    var name by mutableStateOf(name)
    var shops by mutableStateOf(shops)
    var cashbacks by mutableStateOf(cashbacks)

    override val updatedProperties = mutableStateMapOf<String, Pair<String, String>>()


    fun update(category: Category) {
        id = category.id
        name = category.name

        if (category is FullCategory) {
            shops = category.shops
            cashbacks = category.cashbacks
        }
    }


    fun mapToCategory() = FullCategory(
        id = this.id,
        name = this.name,
        shops = shops,
        cashbacks = cashbacks
    )
}