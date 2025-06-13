package com.cashbacks.core.database.utils

import com.cashbacks.core.database.entity.AmountDB
import com.cashbacks.core.database.entity.BankCardEntity
import com.cashbacks.core.database.entity.BasicCashbackEntity
import com.cashbacks.core.database.entity.CashbackEntity
import com.cashbacks.core.database.entity.CategoryEntity
import com.cashbacks.core.database.entity.CategoryShopEntity
import com.cashbacks.core.database.entity.FullCashbackEntity
import com.cashbacks.core.database.entity.PrimaryBankCardEntity
import com.cashbacks.core.database.entity.SettingsEntity
import com.cashbacks.core.database.entity.ShopEntity
import com.cashbacks.features.bankcard.domain.model.FullBankCard
import com.cashbacks.features.bankcard.domain.model.PrimaryBankCard
import com.cashbacks.features.cashback.domain.model.BasicCashback
import com.cashbacks.features.cashback.domain.model.Cashback
import com.cashbacks.features.cashback.domain.model.FullCashback
import com.cashbacks.features.cashback.domain.utils.asCashbackOwner
import com.cashbacks.features.category.domain.model.Category
import com.cashbacks.features.settings.domain.model.ColorDesign
import com.cashbacks.features.settings.domain.model.Settings
import com.cashbacks.features.shop.domain.model.BasicShop
import com.cashbacks.features.shop.domain.model.CategoryShop
import com.cashbacks.features.shop.domain.model.Shop

fun FullBankCard.mapToEntity() = BankCardEntity(
    id = id,
    name = name,
    number = number,
    paymentSystem = paymentSystem,
    holder = holder,
    validityPeriod = validityPeriod,
    cvv = cvv,
    pin = pin,
    comment = comment,
    maxCashbacksNumber = maxCashbacksNumber
)

fun BankCardEntity.mapToBankCard() = FullBankCard(
    id = id,
    name = name,
    number = number,
    paymentSystem = paymentSystem,
    holder = holder,
    validityPeriod = validityPeriod,
    cvv = cvv,
    pin = pin,
    comment = comment,
    maxCashbacksNumber = maxCashbacksNumber
)

fun PrimaryBankCardEntity.mapToDomainBankCard() = PrimaryBankCard(
    id = id,
    name = name,
    number = number,
    paymentSystem = paymentSystem,
    holder = holder,
    validityPeriod = validityPeriod,
    cvv = cvv,
    maxCashbacksNumber = maxCashbacksNumber
)


fun Category.mapToEntity() = CategoryEntity(id, name)
fun CategoryEntity.mapToDomainCategory() = Category(id, name)


fun Shop.mapToEntity(categoryId: Long) = ShopEntity(
    id = id,
    categoryId = categoryId,
    name = name
)

fun CategoryShop.mapToEntity() = ShopEntity(
    id = id,
    categoryId = parent.id,
    name = name
)

fun ShopEntity.mapToDomainShop() = BasicShop(
    id = id,
    name = name
)

fun CategoryShopEntity.mapToDomainShop() = CategoryShop(
    id = id,
    name = name,
    parent = category.mapToDomainCategory()
)


fun Cashback.mapToEntity(
    categoryId: Long? = null,
    shopId: Long? = null
) = CashbackEntity(
    id = id,
    shopId = shopId,
    categoryId = categoryId,
    bankCardId = bankCard.id,
    amount = AmountDB(amount),
    measureUnit = measureUnit,
    startDate = startDate,
    expirationDate = expirationDate,
    comment = comment
)

fun BasicCashbackEntity.mapToDomainCashback() = BasicCashback(
    id = id,
    bankCard = bankCard,
    amount = amount.toString(),
    measureUnit = measureUnit,
    startDate = startDate,
    expirationDate = expirationDate,
    comment = comment
)

fun FullCashbackEntity.mapToDomainCashback(): FullCashback? {
    return FullCashback(
        basicCashback = basicCashbackEntity.mapToDomainCashback(),
        owner = category?.mapToDomainCategory()?.asCashbackOwner()
            ?: shop?.mapToDomainShop()?.asCashbackOwner()
            ?: return null
    )
}


fun Settings.mapToEntity() = SettingsEntity(
    colorDesign = colorDesign.name,
    dynamicColor = dynamicColor,
    autoDeleteExpiredCashbacks = autoDeleteExpiredCashbacks
)

fun SettingsEntity.mapToDomainSettings() = Settings(
    colorDesign = ColorDesign.valueOf(this.colorDesign),
    dynamicColor = this.dynamicColor,
    autoDeleteExpiredCashbacks = this.autoDeleteExpiredCashbacks
)