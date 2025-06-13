package com.cashbacks.features.shop.presentation.api.resources

import com.cashbacks.common.resources.MessageException
import com.cashbacks.common.resources.MessageHandler
import com.cashbacks.common.resources.R

data object ShopNotSelectedException : MessageException {
    override fun getMessage(messageHandler: MessageHandler): String {
        return messageHandler.getMessage(R.string.shop_not_selected)
    }
}

data object ShopNameNotSelectedException : MessageException {
    override fun getMessage(messageHandler: MessageHandler): String {
        return messageHandler.getMessage(R.string.shop_name_not_selected)
    }
}