package com.cashbacks.app.viewmodel

import androidx.lifecycle.ViewModel
import com.cashbacks.domain.usecase.cashback.CashbackShopUseCase
import com.cashbacks.domain.usecase.cashback.EditCashbackUseCase

class CashbackViewModel(
    private val cashbackShopUseCase: CashbackShopUseCase,
    private val editCashbackUseCase: EditCashbackUseCase
) : ViewModel() {
}