package com.cashbacks.domain.model

import kotlinx.parcelize.Parcelize
import kotlin.random.Random

sealed interface Shop : CashbackOwner {
    override val id: Long
    override val name: String
}


sealed interface CategoryShop : Shop, ParentCashbackOwner {
    override val parent: Category
}


@Parcelize
data class BasicShop(
    override val id: Long = Random.nextLong(),
    override val name: String = "",
    override val maxCashback: Cashback? = null
) : Shop, MaxCashbackOwner


@Parcelize
data class BasicCategoryShop(
    override val id: Long,
    override val name: String,
    override val parent: Category,
    override val maxCashback: Cashback?
) : CategoryShop, MaxCashbackOwner


@Parcelize
data class FullCategoryShop(
    override val id: Long = Random.nextLong(),
    override val parent: Category = BasicCategory(),
    override val name: String = "",
    val cashbacks: List<Cashback> = emptyList()
) : CategoryShop
