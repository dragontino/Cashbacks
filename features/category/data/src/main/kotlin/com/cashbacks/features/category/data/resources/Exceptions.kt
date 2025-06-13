package com.cashbacks.features.category.data.resources

import com.cashbacks.common.resources.MessageException
import com.cashbacks.common.resources.MessageHandler
import com.cashbacks.common.resources.R

private interface CategoryException {
    val MessageHandler.categoryTitle: String get() = getMessage(R.string.category_title)
}

internal object CategoryAlreadyExistsException : MessageException, CategoryException {
    override fun getMessage(messageHandler: MessageHandler): String {
        return messageHandler.getMessage(
            R.string.entry_already_exists_exception,
            messageHandler.categoryTitle
        )
    }
}


internal class InsertionException(private val categoryName: String) : MessageException, CategoryException {
    override fun getMessage(messageHandler: MessageHandler): String {
        return messageHandler.getMessage(
            R.string.insertion_exception,
            messageHandler.categoryTitle.lowercase(),
            categoryName
        )
    }
}


internal class CategoryNotFoundException(private val id: Long) : MessageException, CategoryException {
    override fun getMessage(messageHandler: MessageHandler): String {
        return messageHandler.getMessage(
            R.string.entity_not_found_exception,
            messageHandler.categoryTitle,
            id
        )
    }
}


internal class CategoryDeletionException(private val name: String) : MessageException, CategoryException {
    override fun getMessage(messageHandler: MessageHandler): String {
        return messageHandler.getMessage(
            R.string.deletion_exception,
            messageHandler.categoryTitle,
            name
        )
    }
}