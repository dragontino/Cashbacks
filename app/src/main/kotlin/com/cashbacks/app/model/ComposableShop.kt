package com.cashbacks.app.model

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cashbacks.domain.model.BasicCategoryShop
import com.cashbacks.domain.model.BasicShop
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.model.CategoryNotSelectedException
import com.cashbacks.domain.model.CategoryShop
import com.cashbacks.domain.model.FullCategoryShop
import com.cashbacks.domain.model.MessageHandler
import com.cashbacks.domain.model.Shop
import com.cashbacks.domain.model.ShopNameNotSelectedException
import kotlin.random.Random

@Stable
internal class ComposableShop(
    id: Long? = null,
    parentCategory: Category? = null,
    name: String = "",
    cashbacks: List<Cashback> = emptyList()
) : Updatable {

    var id by mutableStateOf(id)
    var parentCategory by mutableStateOf(parentCategory)
    var name by mutableStateOf(name)
    var cashbacks by mutableStateOf(cashbacks)

    private val _errors = mutableStateMapOf<ShopError, String>()
    val errors = _errors.toMap()

    override val updatedProperties = mutableStateMapOf<String, Pair<String, String>>()

    fun update(shop: CategoryShop) {
        parentCategory = shop.parent
        name = shop.name

        if (shop is FullCategoryShop) {
            cashbacks = shop.cashbacks
        }
    }

    val haveErrors: Boolean get() = _errors.isNotEmpty()
    val errorMessage: String? get() = ShopError.entries.firstNotNullOfOrNull { _errors[it] }

    fun updateErrorMessage(error: ShopError, messageHandler: MessageHandler) {
        val exception = when (error) {
            ShopError.Parent -> CategoryNotSelectedException
            ShopError.Name -> ShopNameNotSelectedException
        }
        messageHandler.getExceptionMessage(exception)
            ?.let { _errors[error] = it }
            ?: _errors.remove(error)
    }

    fun updateErrorMessages(messageHandler: MessageHandler) {
        ShopError.entries.forEach {
            updateErrorMessage(it, messageHandler)
        }
    }

    fun mapToShop(): Shop = BasicShop(id = id ?: Random.nextLong(), name = this.name)

    fun mapToCategoryShop(): CategoryShop? {
        return BasicCategoryShop(
            id = id ?: Random.nextLong(),
            parent = parentCategory ?: return null,
            name = this.name,
            maxCashback = null
        )
    }
}


internal enum class ShopError {
    Parent,
    Name
}