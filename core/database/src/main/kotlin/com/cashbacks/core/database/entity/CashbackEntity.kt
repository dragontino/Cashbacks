package com.cashbacks.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cashbacks.features.bankcard.domain.model.PreviewBankCard
import com.cashbacks.features.cashback.domain.model.MeasureUnit
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
            entity = BankCardEntity::class,
            parentColumns = ["id"],
            childColumns = ["bankCardId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ShopEntity::class,
            parentColumns = ["id"],
            childColumns = ["shopId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("shopId"), Index("categoryId"), Index("bankCardId")]
)
data class CashbackEntity(
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
)


data class BasicCashbackEntity(
    val id: Long,
    @Embedded(prefix = "card_")
    val bankCard: PreviewBankCard,
    val amount: AmountDB,
    val measureUnit: MeasureUnit,
    val startDate: LocalDate?,
    val expirationDate: LocalDate?,
    val comment: String
)


data class FullCashbackEntity(
    @Embedded
    val basicCashbackEntity: BasicCashbackEntity,
    @Embedded(prefix = "category_")
    val category: CategoryEntity?,
    @Embedded(prefix = "shop_")
    val shop: ShopEntity?
)