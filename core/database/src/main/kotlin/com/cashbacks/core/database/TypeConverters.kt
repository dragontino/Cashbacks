package com.cashbacks.core.database

import androidx.room.TypeConverter
import com.cashbacks.common.utils.parseToDate
import com.cashbacks.core.database.entity.AmountDB
import com.cashbacks.features.bankcard.domain.model.PaymentSystem
import com.cashbacks.features.cashback.domain.model.MeasureUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format

data object PaymentSystemConverter {
    @TypeConverter
    fun convertToString(paymentSystem: PaymentSystem) = paymentSystem.name

    @TypeConverter
    fun convertFromString(str: String) = PaymentSystem.valueOf(str)
}


object AmountConverter {
    @TypeConverter
    fun amountAsDouble(amountDB: AmountDB) = amountDB.value

    @TypeConverter
    fun doubleAsAmount(value: Double) = AmountDB(value)
}


object MeasureUnitConverter {
    @TypeConverter
    fun measureUnitToString(measureUnit: MeasureUnit) = measureUnit.toString()

    @TypeConverter
    fun stringToMeasureUnit(string: String) = MeasureUnit(string)
}


object LocalDateConverter {
    private val format = LocalDate.Formats.ISO

    @TypeConverter
    fun convertLocalDateToString(date: LocalDate) = date.format(format)

    @TypeConverter
    fun convertStringToLocalDate(string: String): LocalDate = try {
        string.parseToDate()
    } catch (_: IllegalArgumentException) {
        string.let { LocalDate.parse(it, format) }
    }
}