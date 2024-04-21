package com.cashbacks.app.model

import android.content.res.Resources
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.cashbacks.domain.R
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.model.CategoryShop
import com.cashbacks.domain.model.Shop

class ComposableShop(
    val id: Long = 0,
    var category: Category? = null,
    name: String = ""
) : Updatable {
    constructor(shop: CategoryShop) : this(
        id = shop.id,
        category = shop.parentCategory,
        name = shop.name
    )

    var name by mutableStateOf(name)

    private val _categoryErrorMessage = mutableStateOf("")
    val categoryErrorMessage = derivedStateOf { _categoryErrorMessage.value }

    private val _nameErrorMessage = mutableStateOf("")
    val nameErrorMessage = derivedStateOf { _nameErrorMessage.value }

    override val updatedProperties: SnapshotStateMap<String, Pair<String, String>> = mutableStateMapOf()

    val haveErrors: Boolean
        get() = categoryErrorMessage.value.isNotBlank()
                || nameErrorMessage.value.isNotBlank()


    val errorMessage: String?
        get() = categoryErrorMessage.value.takeIf { it.isNotBlank() }
            ?: nameErrorMessage.value.takeIf { it.isNotBlank() }

    fun updateCategoryError(resources: Resources) {
        _categoryErrorMessage.value = when (category) {
            null -> resources.getString(R.string.category_not_selected)
            else -> ""
        }
    }

    fun updateNameError(resources: Resources) {
        _nameErrorMessage.value = when {
            name.isBlank() -> resources.getString(R.string.shop_name_not_selected)
            else -> ""
        }
    }

    fun updateErrors(resources: Resources) {
        updateCategoryError(resources)
        updateNameError(resources)
    }

    fun mapToShop() = Shop(
        id = id,
        name = this.name,
        maxCashback = null
    )
}