package com.cashbacks.data.room

import androidx.room.TypeConverter
import com.cashbacks.domain.model.PaymentSystem

object PaymentSystemConverter {
    @TypeConverter
    fun PaymentSystem.convertToString() = this.name

    @TypeConverter
    fun String.convertToPaymentSystem() = PaymentSystem.valueOf(this)
}