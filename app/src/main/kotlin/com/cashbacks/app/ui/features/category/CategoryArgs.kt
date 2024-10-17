package com.cashbacks.app.ui.features.category

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CategoryArgs(
    val id: Long,
    val startTab: CategoryTabItemType = CategoryTabItemType.Cashbacks
) : Parcelable
