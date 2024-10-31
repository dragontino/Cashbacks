package com.cashbacks.data.room

import androidx.room.TypeConverter
import com.cashbacks.data.model.AmountDB
import com.cashbacks.domain.model.MeasureUnit
import com.cashbacks.domain.model.PaymentSystem

internal object PaymentSystemConverter {
    @TypeConverter
    fun PaymentSystem.convertToString() = this.name

    @TypeConverter
    fun String.convertToPaymentSystem() = PaymentSystem.valueOf(this)
}


internal object AmountConverter {
    @TypeConverter
    fun AmountDB.asDouble() = this.value

    @TypeConverter
    fun Double.toAmount() = AmountDB(this)
}


internal object MeasureUnitConverter {
    @TypeConverter
    fun MeasureUnit.convertToString() = this.toString()

    @TypeConverter
    fun String.convertToMeasureUnit() = MeasureUnit(this)
}