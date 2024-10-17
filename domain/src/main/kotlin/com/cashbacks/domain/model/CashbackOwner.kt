package com.cashbacks.domain.model

import android.os.Parcelable

sealed interface CashbackOwner : Parcelable {
    val id: Long
    val name: String
}


sealed interface MaxCashbackOwner : CashbackOwner {
    val maxCashback: Cashback?
}


sealed interface ParentCashbackOwner : CashbackOwner {
    val parent: CashbackOwner
}