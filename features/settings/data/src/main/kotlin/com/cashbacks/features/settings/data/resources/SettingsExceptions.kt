package com.cashbacks.features.settings.data.resources

import com.cashbacks.common.resources.MessageException
import com.cashbacks.common.resources.MessageHandler
import com.cashbacks.common.resources.R

object SaveSettingsException : MessageException {
    override fun getMessage(messageHandler: MessageHandler): String {
        return messageHandler.getMessage(R.string.save_settings_exception)
    }
}

object SettingsNotFoundException : MessageException {
    override fun getMessage(messageHandler: MessageHandler): String {
        return messageHandler.getMessage(R.string.settings_not_found_exception)
    }
}