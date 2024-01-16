package com.cashbacks.app.viewmodel

import androidx.lifecycle.ViewModel
import com.cashbacks.domain.usecase.CashbackShopUseCase
import com.cashbacks.domain.usecase.GetCashbackUseCase

class CashbackViewModel(
    private val cashbackShopUseCase: CashbackShopUseCase,
    private val getCashbackUseCase: GetCashbackUseCase
) : ViewModel() {
}