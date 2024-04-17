package com.cashbacks.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cashbacks.domain.model.BankCard
import com.cashbacks.domain.model.BasicBankCard
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.CashbackWithParent
import com.cashbacks.domain.model.CategoryCashback
import com.cashbacks.domain.model.ShopCashback

@Entity(
    tableName = "Cashbacks",
    foreignKeys = [
        ForeignKey(
            entity = BankCardDB::class,
            parentColumns = ["id"],
            childColumns = ["bankCardId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ShopDB::class,
            parentColumns = ["id"],
            childColumns = ["shopId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryDB::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("shopId"), Index("categoryId"), Index("bankCardId")]
)
data class CashbackDB(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val shopId: Long?,
    val categoryId: Long?,
    val bankCardId: Long,
    val amount: Double = -1.0,
    val expirationDate: String? = "",
    val comment: String = ""
) {
    constructor(
        cashback: Cashback,
        categoryId: Long? = null,
        shopId: Long? = null
    ) : this(
        id = cashback.id,
        shopId = shopId,
        categoryId = categoryId,
        bankCardId = cashback.bankCard.id,
        amount = cashback.amount.toDoubleOrNull() ?: -1.0,
        expirationDate = cashback.expirationDate,
        comment = cashback.comment
    )
}


data class BasicCashbackDB(
    val id: Long,
    @Embedded(prefix = "card_")
    val bankCard: BasicBankCard,
    val amount: Double,
    val expirationDate: String?,
    val comment: String
) {
    fun mapToCashback() = Cashback(
        id = id,
        amount = if (amount < 0) "" else amount.toString(),
        expirationDate = expirationDate,
        comment = comment,
        bankCard = bankCard
    )
}


data class CashbackWithBankCardDB(
    val id: Long,
    @Embedded(prefix = "card_")
    val bankCard: BankCard,
    val amount: Double,
    val expirationDate: String?,
    val comment: String
) {
    fun mapToCashback() = Cashback(
        id = id,
        amount = if (amount < 0) "" else amount.toString(),
        expirationDate = expirationDate,
        comment = comment,
        bankCard = bankCard
    )
}



sealed class ParentCashbackWithBankCardDB(
    val id: Long,
    @Embedded(prefix = "card_")
    val bankCard: BasicBankCard,
    val amount: Double,
    val expirationDate: String?,
    val comment: String,
) {
    abstract fun mapToCashback(): CashbackWithParent

    class Category(
        id: Long,
        @Embedded(prefix = "category_")
        val categoryDB: CategoryDB,
        bankCard: BasicBankCard,
        amount: Double,
        expirationDate: String?,
        comment: String
    ) : ParentCashbackWithBankCardDB(id, bankCard, amount, expirationDate, comment) {
        override fun mapToCashback() = CategoryCashback(
            id = id,
            parentCategory = categoryDB.mapToCategory(),
            bankCard = bankCard,
            expirationDate = expirationDate,
            amount = if (amount < 0) "" else amount.toString(),
            comment = comment
        )
    }


    class Shop(
        id: Long,
        @Embedded(prefix = "shop_")
        val shop: ShopDB,
        bankCard: BasicBankCard,
        amount: Double,
        expirationDate: String?,
        comment: String
    ) : ParentCashbackWithBankCardDB(id, bankCard, amount, expirationDate, comment) {
        override fun mapToCashback() = ShopCashback(
            id = id,
            parentShop = shop.mapToShop(),
            bankCard = bankCard,
            expirationDate = expirationDate,
            amount = if (amount < 0) "" else amount.toString(),
            comment = comment
        )
    }
}