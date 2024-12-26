package com.cashbacks.data.room

import androidx.room.TypeConverter
import com.cashbacks.data.model.AmountDB
import com.cashbacks.domain.model.MeasureUnit
import com.cashbacks.domain.model.PaymentSystem
import com.cashbacks.domain.util.format
import com.cashbacks.domain.util.parseToDate
import kotlinx.datetime.LocalDate

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


internal object LocalDateConverter {
    @TypeConverter
    fun LocalDate?.convertToString(): String? = this?.format()

    @TypeConverter
    fun String?.convertToLocalDate(): LocalDate? = this?.parseToDate()
}