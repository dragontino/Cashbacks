package com.cashbacks.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cashbacks.features.bankcard.domain.model.PaymentSystem

@Entity(tableName = "Cards")
data class BankCardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val number: String,
    val paymentSystem: PaymentSystem?,
    val holder: String,
    val validityPeriod: String,
    val cvv: String,
    val pin: String,
    val comment: String,
    @ColumnInfo(defaultValue = "null")
    val maxCashbacksNumber: Int?
)


data class PrimaryBankCardEntity(
    val id: Long,
    val name: String,
    val number: String,
    val paymentSystem: PaymentSystem?,
    val holder: String = "",
    val validityPeriod: String = "",
    val cvv: String = "",
    val maxCashbacksNumber: Int? = null
)