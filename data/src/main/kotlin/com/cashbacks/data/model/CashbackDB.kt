package com.cashbacks.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cashbacks.domain.model.BasicInfoBankCard
import com.cashbacks.domain.model.Cashback

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
    val amount: String = "",
    val expirationDate: String? = "",
    val comment: String = ""
) {
    fun mapToCashback(bankCard: BasicInfoBankCard) = Cashback(
        id = id,
        amount = amount,
        expirationDate = expirationDate,
        comment = comment,
        bankCard = bankCard
    )
}


data class CashbackWithBankCardDB(
    val id: Long,
    @Embedded(prefix = "card_")
    val bankCard: BasicBankCardDB,
    val amount: String,
    val expirationDate: String?,
    val comment: String
) {
    fun mapToCashback() = Cashback(
        id = id,
        amount = amount,
        expirationDate = expirationDate,
        comment = comment,
        bankCard = bankCard.mapToBankCard()
    )
}