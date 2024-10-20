package com.cashbacks.app.model

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cashbacks.app.ui.features.cashback.CashbackOwnerType
import com.cashbacks.domain.model.BankCardNotSelectedException
import com.cashbacks.domain.model.BasicBankCard
import com.cashbacks.domain.model.CashbackOwner
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.model.CategoryNotSelectedException
import com.cashbacks.domain.model.FullCashback
import com.cashbacks.domain.model.IncorrectCashbackAmountException
import com.cashbacks.domain.model.MessageHandler
import com.cashbacks.domain.model.Shop
import com.cashbacks.domain.model.ShopNotSelectedException
import com.cashbacks.domain.model.roundedAmount
import kotlin.random.Random

@Stable
internal class ComposableCashback private constructor(
    id: Long?,
    private val ownerType: CashbackOwnerType,
    owner: CashbackOwner?,
    bankCard: BasicBankCard?,
    amount: String,
    expirationDate: String?,
    comment: String
) : Updatable {

    constructor(ownerType: CashbackOwnerType) : this(
        id = Random.nextLong(),
        ownerType = ownerType,
        owner = null,
        bankCard = null,
        amount = "",
        expirationDate = null,
        comment = ""
    )

    var id by mutableStateOf(id)
    var amount by mutableStateOf(amount)
    var owner by mutableStateOf(owner)
    var bankCard by mutableStateOf(bankCard)
    var expirationDate by mutableStateOf(expirationDate ?: "")
    var comment by mutableStateOf(comment)

    private val errorMessages = mutableStateMapOf<CashbackError, String>()
    val errors: Map<CashbackError, String> = errorMessages.toMap()

    override val updatedProperties = mutableStateMapOf<String, Pair<String, String>>()

    fun updateCashback(cashback: FullCashback) {
        val ownerType = when (cashback.owner) {
            is Category -> CashbackOwnerType.Category
            is Shop -> CashbackOwnerType.Shop
        }
        if (this.ownerType == ownerType) {
            id = cashback.id
            owner = cashback.owner
            amount = cashback.roundedAmount
            bankCard = cashback.bankCard
            expirationDate = cashback.expirationDate ?: ""
            comment = cashback.comment
        }
    }


    val haveErrors: Boolean get() = errorMessages.isNotEmpty()
    val errorMessage: String? get() = CashbackError.entries.firstNotNullOfOrNull { errorMessages[it] }


    fun updateErrorMessage(error: CashbackError, messageHandler: MessageHandler) {
        val message = when (error) {
            CashbackError.Owner -> {
                when (owner) {
                    null -> {
                        val exception = when (ownerType) {
                            CashbackOwnerType.Category -> CategoryNotSelectedException
                            CashbackOwnerType.Shop -> ShopNotSelectedException
                        }
                        messageHandler.getExceptionMessage(exception)
                    }
                    else -> null
                }
            }

            CashbackError.BankCard -> when {
                bankCard == null || bankCard?.id == 0L -> messageHandler
                    .getExceptionMessage(BankCardNotSelectedException)

                else -> null
            }
            CashbackError.Amount -> when {
                amount.toDoubleOrNull() == null -> messageHandler
                    .getExceptionMessage(IncorrectCashbackAmountException)

                else -> null
            }
        }

        message?.let { errorMessages[error] = it } ?: errorMessages.remove(error)
    }


    fun updateAllErrors(messageHandler: MessageHandler) {
        CashbackError.entries.forEach {
            updateErrorMessage(it, messageHandler)
        }
    }


    fun mapToCashback(): FullCashback? {
        return FullCashback(
            id = this.id ?: Random.nextLong(),
            owner = owner ?: return null,
            bankCard = bankCard ?: return null,
            amount = this.amount,
            expirationDate = this.expirationDate.takeIf { it.isNotBlank() },
            comment = this.comment
        )
    }
}


internal enum class CashbackError {
    Owner,
    BankCard,
    Amount
}