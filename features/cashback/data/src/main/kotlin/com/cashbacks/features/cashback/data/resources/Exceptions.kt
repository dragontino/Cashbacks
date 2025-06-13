package com.cashbacks.features.cashback.data.resources

import com.cashbacks.common.resources.MessageException
import com.cashbacks.common.resources.MessageHandler
import com.cashbacks.common.resources.R
import com.cashbacks.features.cashback.domain.model.Cashback
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter

private interface CashbackException {
    val MessageHandler.cashbackTitle: String get() = getMessage(R.string.cashback_title)
}


internal class InsertionException(private val cashbackId: Long) : MessageException, CashbackException {
    override fun getMessage(messageHandler: MessageHandler): String {
        return messageHandler.getMessage(
            R.string.insertion_exception,
            messageHandler.cashbackTitle.lowercase(),
            cashbackId
        )
    }
}


internal class CashbackNotFoundException(private val id: Long) : MessageException, CashbackException {
    override fun getMessage(messageHandler: MessageHandler): String {
        return messageHandler.getMessage(
            R.string.entity_not_found_exception,
            messageHandler.cashbackTitle,
            id
        )
    }
}


internal class UpdateException(private val cashbackId: Long) : MessageException, CashbackException {
    override fun getMessage(messageHandler: MessageHandler): String {
        return messageHandler.getMessage(
            R.string.update_exception,
            messageHandler.cashbackTitle,
            cashbackId
        )
    }
}


internal class DeletionException(private val amount: String) : MessageException, CashbackException {
    override fun getMessage(messageHandler: MessageHandler): String {
        return messageHandler.getMessage(
            R.string.deletion_exception,
            messageHandler.cashbackTitle,
            amount
        )
    }
}


internal data class CashbackOverflowException(
    val cashback: Cashback,
    val failedMonth: LocalDate
) : MessageException {
    override fun getMessage(messageHandler: MessageHandler): String {
        return messageHandler.getMessage(
            R.string.insert_cashback_exception,
            cashback.bankCard.name.ifBlank { cashback.bankCard.number },
            failedMonth.toJavaLocalDate().format(DateTimeFormatter.ofPattern("MMMM uuuu"))
        )
    }
}