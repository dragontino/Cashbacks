package com.cashbacks.features.category.presentation.api.resources

import com.cashbacks.common.resources.MessageException
import com.cashbacks.common.resources.MessageHandler
import com.cashbacks.common.resources.R

data object CategoryNotSelectedException : MessageException {
    override fun getMessage(messageHandler: MessageHandler): String {
        return messageHandler.getMessage(R.string.category_not_selected)
    }
}

data object IncorrectCategoryNameException : MessageException {
    override fun getMessage(messageHandler: MessageHandler): String {
        return messageHandler.getMessage(R.string.incorrect_category_name)
    }
}