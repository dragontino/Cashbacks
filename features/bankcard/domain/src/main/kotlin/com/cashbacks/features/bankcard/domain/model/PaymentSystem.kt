package com.cashbacks.features.bankcard.domain.model

enum class PaymentSystem(val prefix: String) {
    Visa("4"),
    MasterCard("5"),
    Mir("22"),
    JCB("35"),
    UnionPay("62"),
    AmericanExpress("3"),
}