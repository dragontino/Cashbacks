package com.cashbacks.app.model

import android.content.Context
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
    parentCategory: Category? = null,
    name: String = ""
) : Updatable {
    constructor(shop: CategoryShop) : this(
        id = shop.id,
        parentCategory = shop.parentCategory,
        name = shop.name
    )

    var parentCategory by mutableStateOf(parentCategory)
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

    fun updateCategoryError(context: Context) {
        _categoryErrorMessage.value = when {
            parentCategory == null -> context.getString(R.string.category_not_selected)
            else -> ""
        }
    }

    fun updateNameError(context: Context) {
        _nameErrorMessage.value = when {
            name.isBlank() -> context.getString(R.string.shop_name_not_selected)
            else -> ""
        }
    }

    fun updateErrors(context: Context) {
        updateCategoryError(context)
        updateNameError(context)
    }

    fun mapToShop() = Shop(
        id = id,
        name = this.name,
        maxCashback = null
    )
}