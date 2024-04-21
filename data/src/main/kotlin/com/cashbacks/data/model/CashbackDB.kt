package com.cashbacks.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cashbacks.domain.model.BasicBankCard
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.CashbackWithOwner
import com.cashbacks.domain.model.CategoryCashback
import com.cashbacks.domain.model.ShopCashback

data class AmountDB(val value: Double) : Comparable<AmountDB> {
    constructor(value: String) : this(value.toDoubleOrNull() ?: -1.0)

    override fun compareTo(other: AmountDB): Int {
        return this.value.compareTo(other.value)
    }

    override fun toString() = when {
        value < 0.0 -> ""
        else -> value.toString()
    }
}



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
    val amount: AmountDB = AmountDB(-1.0),
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
        amount = AmountDB(cashback.amount),
        expirationDate = cashback.expirationDate,
        comment = cashback.comment
    )
}


data class BasicCashbackDB(
    val id: Long,
    @Embedded(prefix = "card_")
    val bankCard: BasicBankCard,
    val amount: AmountDB,
    val expirationDate: String?,
    val comment: String
) {
    fun mapToCashback() = Cashback(
        id = id,
        amount = amount.toString(),
        expirationDate = expirationDate,
        comment = comment,
        bankCard = bankCard
    )
}


data class CashbackWithOwnersDB(
    val id: Long,
    @Embedded(prefix = "category_")
    val category: CategoryDB?,
    @Embedded(prefix = "shop_")
    val shop: ShopDB?,
    @Embedded(prefix = "card_")
    val bankCard: BasicBankCard,
    val amount: AmountDB,
    val expirationDate: String?,
    val comment: String
) {
    fun mapToCashback(): CashbackWithOwner? {
        return when {
            category != null -> CategoryCashback(
                id = id,
                category = category.mapToCategory(),
                bankCard = bankCard,
                amount = amount.toString(),
                expirationDate = expirationDate,
                comment = comment
            )
            shop != null -> ShopCashback(
                id = id,
                shop = shop.mapToShop(),
                bankCard = bankCard,
                amount = amount.toString(),
                expirationDate = expirationDate,
                comment = comment
            )
            else -> null
        }
    }
}



sealed class CashbackWithOwnerAndBankCardDB(
    val id: Long,
    @Embedded(prefix = "card_")
    val bankCard: BasicBankCard,
    val amount: AmountDB,
    val expirationDate: String?,
    val comment: String,
) {
    abstract fun mapToCashback(): CashbackWithOwner

    class Category(
        id: Long,
        @Embedded(prefix = "category_")
        val categoryDB: CategoryDB,
        bankCard: BasicBankCard,
        amount: AmountDB,
        expirationDate: String?,
        comment: String
    ) : CashbackWithOwnerAndBankCardDB(id, bankCard, amount, expirationDate, comment) {
        override fun mapToCashback() = CategoryCashback(
            id = id,
            category = categoryDB.mapToCategory(),
            bankCard = bankCard,
            expirationDate = expirationDate,
            amount = amount.toString(),
            comment = comment
        )
    }


    class Shop(
        id: Long,
        @Embedded(prefix = "shop_")
        val shop: ShopDB,
        bankCard: BasicBankCard,
        amount: AmountDB,
        expirationDate: String?,
        comment: String
    ) : CashbackWithOwnerAndBankCardDB(id, bankCard, amount, expirationDate, comment) {
        override fun mapToCashback() = ShopCashback(
            id = id,
            shop = shop.mapToShop(),
            bankCard = bankCard,
            expirationDate = expirationDate,
            amount = amount.toString(),
            comment = comment
        )
    }
}