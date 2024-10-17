package com.cashbacks.domain.model

import kotlinx.parcelize.Parcelize
import kotlin.random.Random

sealed interface Category : CashbackOwner {
    override val id: Long
    override val name: String
}


@Parcelize
data class BasicCategory(
    override val id: Long = Random.nextLong(),
    override val name: String = "",
    override val maxCashback: Cashback? = null,
) : Category, MaxCashbackOwner


@Parcelize
data class FullCategory(
    override val id: Long = Random.nextLong(),
    override val name: String = "",
    val shops: List<BasicShop> = emptyList(),
    val cashbacks: List<Cashback> = emptyList()
) : Category
