package com.cashbacks.app.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.cashbacks.app.R
import com.cashbacks.app.model.BankCardMapper
import com.cashbacks.app.model.PaymentSystemMapper
import com.cashbacks.app.ui.theme.BackgroundDark
import com.cashbacks.app.util.animate
import com.cashbacks.domain.model.BankCard
import com.cashbacks.domain.model.PaymentSystem

@Composable
fun BankCardCompose(
    bankCard: BankCard,
    onCopy: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isVisible by rememberSaveable { mutableStateOf(false) }

    Box(modifier = modifier.wrapContentSize()) {
        CardFrontSide(
            bankCard = bankCard,
            isVisible = isVisible,
            onCopy = onCopy,
            onChangeVisibility = remember { { isVisible = !isVisible } },
            modifier = Modifier
                .padding(end = 60.dp)
                .zIndex(2f)
                .align(Alignment.TopStart)
                .width(300.dp)
        )

        CardBackSide(
            bankCard = bankCard,
            isVisible = isVisible,
            onCopy = onCopy,
            modifier = Modifier
                .padding(top = 40.dp, start = 60.dp)
                .zIndex(1f)
                .align(Alignment.BottomEnd)
                .width(300.dp)
        )
    }
}


@Composable
private fun CardFrontSide(
    bankCard: BankCard,
    isVisible: Boolean,
    onCopy: (String) -> Unit,
    onChangeVisibility: () -> Unit,
    modifier: Modifier = Modifier
) {
    ShadowElevatedCard(
        modifier = modifier,
        contentPadding = PaddingValues(6.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        shadowElevation = 15.dp,
        containerColor = Color.DarkGray,
        contentColor = Color.White
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            bankCard.paymentSystem?.let {
                PaymentSystemMapper.PaymentSystemImage(
                    paymentSystem = it,
                    maxWidth = 50.dp,
                    modifier = Modifier.align(Alignment.BottomStart)
                )
            }
            IconButton(
                onClick = onChangeVisibility,
                modifier = Modifier.align(Alignment.TopEnd),
            ) {
                Icon(
                    imageVector = when {
                        isVisible -> Icons.Outlined.VisibilityOff
                        else -> Icons.Outlined.Visibility
                    },
                    contentDescription = null
                )
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = when {
                    isVisible -> bankCard.number
                    else -> bankCard.hiddenNumber
                }.let { BankCardMapper.addSpacesToCardNumber(it) },
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace
            )

            IconButton(
                onClick = { onCopy(bankCard.number) },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ContentCopy,
                    contentDescription = "copy card number",
                    modifier = Modifier.scale(.8f)
                )
            }
        }

        Text(
            text = bankCard.validityPeriod,
            modifier = Modifier
                .padding(end = 30.dp)
                .align(Alignment.End),
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodyMedium
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = bankCard.holder,
                modifier = Modifier.align(Alignment.CenterStart),
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodyMedium
            )

            IconButton(
                onClick = { onCopy(bankCard.holder) },
                modifier = Modifier.align(Alignment.BottomEnd),
            ) {
                Icon(
                    imageVector = Icons.Outlined.ContentCopy,
                    contentDescription = "copy card holder",
                    modifier = Modifier.scale(.8f)
                )
            }
        }
    }
}


@Composable
private fun CardBackSide(
    bankCard: BankCard,
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    onCopy: (String) -> Unit,
) {
    ShadowElevatedCard(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        containerColor = Color.DarkGray,
        contentColor = Color.White
    ) {
        Spacer(Modifier.height(30.dp))
        Box(
            modifier = Modifier
                .background(BackgroundDark)
                .height(40.dp)
                .fillMaxWidth(),
        )

        Spacer(Modifier.height(20.dp))

        IconButton(
            onClick = { onCopy(bankCard.cvv) },
            modifier = Modifier.align(Alignment.End).scale(.8f)
        ) {
            Icon(
                imageVector = Icons.Outlined.ContentCopy,
                contentDescription = "copy cvv"
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.End)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.cvv),
                style = MaterialTheme.typography.bodyMedium,
            )

            Text(
                text = if (isVisible) bankCard.cvv else bankCard.getHidden(length = 3),
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}


@Composable
private fun ShadowElevatedCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onBackground,
    shadowElevation: Dp = 5.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 4.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    content: @Composable (ColumnScope.() -> Unit)
) {
    Surface(
        shape = shape,
        color = containerColor.animate(durationMillis = 300),
        contentColor = contentColor.animate(durationMillis = 300),
        shadowElevation = shadowElevation,
        tonalElevation = 15.dp,
        modifier = modifier
    ) {
        Column(
            verticalArrangement = verticalArrangement,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}


@Preview
@Composable
private fun BankCardComposablePreview() {
    BankCardCompose(
        bankCard = BankCard(
            id = 0,
            number = "45555",
            paymentSystem = PaymentSystem.MasterCard
        ),
        onCopy = {}
    )
}