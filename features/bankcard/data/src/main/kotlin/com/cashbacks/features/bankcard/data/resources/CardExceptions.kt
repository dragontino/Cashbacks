package com.cashbacks.features.bankcard.data.resources

import com.cashbacks.common.resources.MessageException
import com.cashbacks.common.resources.MessageHandler
import com.cashbacks.common.resources.R

private interface CardException {
    val MessageHandler.cardTitle: String get() = getMessage(R.string.bank_card)
}

internal data class CardInsertionException(private val entityName: String) : MessageException, CardException {
    override fun getMessage(messageHandler: MessageHandler): String {
        return messageHandler.getMessage(
            R.string.insertion_exception,
            messageHandler.cardTitle.lowercase(),
            entityName
        )
    }
}

internal data class CardUpdateException(val name: String) : MessageException, CardException {
    override fun getMessage(messageHandler: MessageHandler): String {
        return messageHandler.getMessage(
            R.string.update_exception,
            messageHandler.cardTitle.lowercase(),
            name
        )
    }
}

internal data class CardNotFoundException(private val id: Long) : MessageException, CardException {
    override fun getMessage(messageHandler: MessageHandler): String {
        return messageHandler.getMessage(
            R.string.entity_not_found_exception,
            messageHandler.cardTitle,
            id
        )
    }
}

internal data class CardDeletionException(val name: String) : MessageException, CardException {
    override fun getMessage(messageHandler: MessageHandler): String {
        return messageHandler.getMessage(
            R.string.deletion_exception,
            messageHandler.cardTitle,
            name
        )
    }
}