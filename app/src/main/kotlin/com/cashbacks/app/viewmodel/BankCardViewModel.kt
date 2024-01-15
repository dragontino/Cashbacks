package com.cashbacks.app.viewmodel

import androidx.lifecycle.ViewModel
import com.cashbacks.domain.model.PaymentSystem

class BankCardViewModel : ViewModel() {

    fun getPaymentSystemByNumber(number: String) = when {
        number.startsWith("4") -> PaymentSystem.Visa
        number.startsWith("5") -> PaymentSystem.MasterCard
        number.startsWith("22") -> PaymentSystem.Mir
        number.startsWith("35") -> PaymentSystem.JCB
        number.startsWith("62") -> PaymentSystem.UnionPay
        number.startsWith("3") -> PaymentSystem.AmericanExpress
        else -> null
    }
}