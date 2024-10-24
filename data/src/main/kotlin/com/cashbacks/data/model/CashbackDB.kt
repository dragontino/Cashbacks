package com.cashbacks.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cashbacks.domain.model.BasicCashback
import com.cashbacks.domain.model.CalculationUnit
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.FullCashback
import com.cashbacks.domain.model.PreviewBankCard
import com.cashbacks.domain.util.format
import com.cashbacks.domain.util.parseToDate

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
        expirationDate = cashback.expirationDate?.format(),
        comment = cashback.comment
    )
}


data class BasicCashbackDB(
    val id: Long,
    @Embedded(prefix = "card_")
    val bankCard: PreviewBankCard,
    val amount: AmountDB,
    val expirationDate: String?,
    val comment: String
) {
    fun mapToDomainCashback() = BasicCashback(
        id = id,
        amount = amount.toString(),
        calculationUnit = CalculationUnit.Percent,
        expirationDate = expirationDate?.parseToDate(),
        comment = comment,
        bankCard = bankCard
    )
}


data class FullCashbackDB(
    @Embedded
    val basicCashbackDB: BasicCashbackDB,
    @Embedded(prefix = "category_")
    val category: CategoryDB?,
    @Embedded(prefix = "shop_")
    val shop: ShopDB?
) {
    fun mapToDomainCashback(): FullCashback? {
        return FullCashback(
            id = basicCashbackDB.id,
            owner = category?.mapToDomainCategory() ?: shop?.mapToDomainShop() ?: return null,
            bankCard = basicCashbackDB.bankCard,
            amount = basicCashbackDB.amount.toString(),
            calculationUnit = CalculationUnit.Percent,
            expirationDate = basicCashbackDB.expirationDate?.parseToDate(),
            comment = basicCashbackDB.comment
        )
    }
}