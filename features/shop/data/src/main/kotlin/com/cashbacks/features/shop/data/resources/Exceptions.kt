package com.cashbacks.features.shop.data.resources

import com.cashbacks.common.resources.MessageException
import com.cashbacks.common.resources.MessageHandler
import com.cashbacks.common.resources.R

private interface ShopException {
    val MessageHandler.shopTitle: String get() = getMessage(R.string.shop_title)
}

internal object ShopAlreadyExistsException : MessageException, ShopException {
    override fun getMessage(messageHandler: MessageHandler): String {
        return messageHandler.getMessage(
            R.string.entry_already_exists_exception,
            messageHandler.shopTitle
        )
    }
}


internal class InsertionException(private val shopName: String) : MessageException, ShopException {
    override fun getMessage(messageHandler: MessageHandler): String {
        return messageHandler.getMessage(
            R.string.insertion_exception,
            messageHandler.shopTitle.lowercase(),
            shopName
        )
    }
}


internal class ShopNotFoundException(private val id: Long) : MessageException, ShopException {
    override fun getMessage(messageHandler: MessageHandler): String {
        return messageHandler.getMessage(
            R.string.entity_not_found_exception,
            messageHandler.shopTitle,
            id
        )
    }
}


internal class ShopDeletionException(private val name: String) : MessageException, ShopException {
    override fun getMessage(messageHandler: MessageHandler): String {
        return messageHandler.getMessage(
            R.string.deletion_exception,
            messageHandler.shopTitle,
            name
        )
    }
}