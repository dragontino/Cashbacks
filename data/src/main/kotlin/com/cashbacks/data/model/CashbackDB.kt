package com.cashbacks.data.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cashbacks.domain.model.BasicCashback
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.FullCashback
import com.cashbacks.domain.model.MeasureUnit
import com.cashbacks.domain.model.PreviewBankCard
import kotlinx.datetime.LocalDate

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
    val amount: AmountDB,
    @ColumnInfo(defaultValue = MeasureUnit.PERCENT_MARK)
    val measureUnit: MeasureUnit,
    @ColumnInfo(defaultValue = "null")
    val startDate: LocalDate?,
    val expirationDate: LocalDate?,
    val comment: String
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
        measureUnit = cashback.measureUnit,
        startDate = cashback.startDate,
        expirationDate = cashback.expirationDate,
        comment = cashback.comment
    )
}


data class BasicCashbackDB(
    val id: Long,
    @Embedded(prefix = "card_")
    val bankCard: PreviewBankCard,
    val amount: AmountDB,
    val measureUnit: MeasureUnit,
    val startDate: LocalDate?,
    val expirationDate: LocalDate?,
    val comment: String
) {
    fun mapToDomainCashback() = BasicCashback(
        id = id,
        bankCard = bankCard,
        amount = amount.toString(),
        measureUnit = measureUnit,
        startDate = startDate,
        expirationDate = expirationDate,
        comment = comment
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
            basicCashback = basicCashbackDB.mapToDomainCashback(),
            owner = category?.mapToDomainCategory() ?: shop?.mapToDomainShop() ?: return null
        )
    }
}