package com.cashbacks.features.bankcard.presentation.api.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cashbacks.common.resources.R
import com.cashbacks.common.utils.OnClick
import com.cashbacks.features.bankcard.domain.model.BankCard
import com.cashbacks.features.bankcard.domain.model.FullBankCard
import com.cashbacks.features.bankcard.domain.model.PaymentSystem
import com.cashbacks.features.bankcard.domain.utils.BankCardUtils
import com.cashbacks.features.bankcard.domain.utils.BankCardUtils.getHiddenNumber
import com.cashbacks.features.bankcard.domain.utils.BankCardUtils.withSpaces
import com.cashbacks.features.bankcard.presentation.api.utils.CopyableBankCardPart
import com.cashbacks.features.bankcard.presentation.api.utils.PaymentSystemUtils
import kotlin.math.roundToInt

private object CardDimensions {
    const val HEIGHT_TO_WIDTH_RATIO = 0.63f
    const val CORNER_RADIUS_TO_WIDTH_RATIO = 0.037f
    const val VISIBLE_WIDTH_RATIO = .35f
    const val VISIBLE_HEIGHT_RATIO = .25f
    const val MAGNETIC_STRIPE_THICKNESS_TO_WIDTH_RATIO = 0.148f
    const val MAGNETIC_STRIPE_TOP_MARGIN_TO_WIDTH_RATIO = 0.058f

    val iconButtonSize = 30.dp
}

private object CardMeasurePolicy : MeasurePolicy {
    override fun MeasureScope.measure(
        measurables: List<Measurable>,
        constraints: Constraints
    ): MeasureResult {
        val cardWidth = minOf(constraints.maxWidth, constraints.maxHeight).let {
            it / (1 + CardDimensions.VISIBLE_WIDTH_RATIO)
        }
        val cardHeight = cardWidth * CardDimensions.HEIGHT_TO_WIDTH_RATIO
        val cardShape = RoundedCornerShape(
            size = cardWidth.toDp() * CardDimensions.CORNER_RADIUS_TO_WIDTH_RATIO
        )

        val childConstraints = Constraints.fixed(
            width = cardWidth.roundToInt(),
            height = cardHeight.roundToInt()
        )

        val (cardFrontSide, cardBackSide) = measurables.map { it.measure(childConstraints) }
        val layoutWidth = (cardWidth * (1 + CardDimensions.VISIBLE_WIDTH_RATIO)).roundToInt()
        val layoutHeight = (cardHeight * (1 + CardDimensions.VISIBLE_HEIGHT_RATIO)).roundToInt()

        return layout(layoutWidth, layoutHeight) {
            cardFrontSide.placeWithLayer(x = 0, y = 0, zIndex = 2f) {
                shape = cardShape
            }

            cardBackSide.placeWithLayer(
                x = (cardWidth * CardDimensions.VISIBLE_WIDTH_RATIO).roundToInt(),
                y = (cardHeight * CardDimensions.VISIBLE_HEIGHT_RATIO).roundToInt(),
                zIndex = 1f
            ) {
                shape = cardShape
            }
        }
    }

}

@Composable
fun PlasticBankCard(
    bankCard: BankCard,
    onCopy: (CopyableBankCardPart, String) -> Unit,
    modifier: Modifier = Modifier,
    onClick: OnClick? = null
) {
    var isVisible by rememberSaveable { mutableStateOf(false) }

    Layout(
        measurePolicy = CardMeasurePolicy,
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        content = {
            CardFrontSide(
                bankCard = bankCard,
                isVisible = isVisible,
                onCopy = onCopy,
                onChangeVisibility = { isVisible = !isVisible },
                onClick = onClick
            )

            CardBackSide(
                bankCard = bankCard,
                isVisible = isVisible,
                onCopy = onCopy,
                onClick = onClick
            )
        }
    )
}


@Composable
private fun CardFrontSide(
    bankCard: BankCard,
    isVisible: Boolean,
    onCopy: (CopyableBankCardPart, String) -> Unit,
    onChangeVisibility: () -> Unit,
    onClick: OnClick?,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small
) {
    var textSize by with(MaterialTheme.typography) {
        remember(this) { mutableFloatStateOf(bodyMedium.fontSize.value) }
    }
    val fontSizeRange = FontSizeRange(
        min = MaterialTheme.typography.bodySmall.fontSize,
        max = MaterialTheme.typography.bodyMedium.fontSize
    )

    val scrollableModifier = when {
        textSize <= fontSizeRange.min.value -> Modifier
            .horizontalScroll(rememberScrollState())
            .verticalScroll(rememberScrollState())

        else -> Modifier
    }

    VerticalShadowElevatedCard(
        modifier = modifier,
        onClick = onClick,
        shadowElevation = 7.dp,
        shape = shape,
        verticalArrangement = object : Arrangement.Vertical {
            override val spacing = 8.dp

            override fun Density.arrange(
                totalSize: Int,
                sizes: IntArray,
                outPositions: IntArray
            ) {
                outPositions[0] = 0
                outPositions[1] = minOf(
                    spacing.roundToPx() + sizes[0],
                    totalSize - sizes[1]
                )
                sizes.getOrNull(2)?.let { outPositions[2] = totalSize - it }
            }
        },
        contentPadding = PaddingValues(8.dp),
        containerColor = Color.DarkGray,
        contentColor = Color.White
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Absolute.SpaceBetween
        ) {
            bankCard.paymentSystem?.let {
                PaymentSystemUtils.PaymentSystemImage(
                    paymentSystem = it,
                    maxWidth = 40.dp,
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onChangeVisibility)
                    .size(CardDimensions.iconButtonSize)
            ) {
                Icon(
                    imageVector = when {
                        isVisible -> Icons.Outlined.VisibilityOff
                        else -> Icons.Outlined.Visibility
                    },
                    contentDescription = "change visibility",
                    modifier = Modifier.scale(.8f)
                )
            }
        }

        Layout(
            measurePolicy = { measurables, constraints ->
                val iconButtonPlaceable = measurables[1].measure(constraints)
                val textConstraints = constraints.copy(
                    maxWidth = constraints.maxWidth - iconButtonPlaceable.width
                )
                val cardNumberPlaceable = measurables[0].measure(textConstraints)

                val firstLineHeight = maxOf(cardNumberPlaceable.height, iconButtonPlaceable.height)
                val validityPeriodPlaceable = measurables[2].measure(
                    constraints = textConstraints.copy(maxHeight = constraints.maxHeight - firstLineHeight)
                )

                layout(
                    width = constraints.maxWidth,
                    height = firstLineHeight + validityPeriodPlaceable.height
                ) {
                    cardNumberPlaceable.place(
                        x = 0,
                        y = (firstLineHeight - cardNumberPlaceable.height) / 2
                    )
                    iconButtonPlaceable.place(
                        x = constraints.maxWidth - iconButtonPlaceable.width,
                        y = (firstLineHeight - iconButtonPlaceable.height) / 2
                    )
                    validityPeriodPlaceable.place(
                        x = constraints.maxWidth - iconButtonPlaceable.width - validityPeriodPlaceable.width,
                        y = firstLineHeight
                    )
                }
            },
            content = {
                AutoresizeText(
                    text = when {
                        isVisible -> bankCard.number
                        else -> bankCard.getHiddenNumber()
                    }.withSpaces(),
                    fontSize = textSize,
                    fontSizeRange = fontSizeRange,
                    onResizeText = { if (textSize > it) textSize = it },
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .wrapContentSize()
                        .then(scrollableModifier)
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onCopy(CopyableBankCardPart.Number, bankCard.number) }
                        .size(CardDimensions.iconButtonSize)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "copy card number",
                        modifier = Modifier.scale(.8f)
                    )
                }

                AutoresizeText(
                    text = bankCard.validityPeriod,
                    fontSize = textSize,
                    fontSizeRange = FontSizeRange(
                        min = MaterialTheme.typography.bodySmall.fontSize,
                        max = MaterialTheme.typography.bodyMedium.fontSize
                    ),
                    onResizeText = { if (textSize > it) textSize = it },
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(top = 4.dp, end = 8.dp)
                        .wrapContentSize()
                )
            }
        )

        if (bankCard.holder.isNotBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Absolute.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AutoresizeText(
                    text = bankCard.holder,
                    fontSize = textSize,
                    fontSizeRange = fontSizeRange,
                    onResizeText = { if (textSize > it) textSize = it },
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = scrollableModifier
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onCopy(CopyableBankCardPart.Holder, bankCard.holder) }
                        .size(CardDimensions.iconButtonSize)
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
}


@Composable
private fun CardBackSide(
    bankCard: BankCard,
    isVisible: Boolean,
    onClick: OnClick?,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small,
    onCopy: (CopyableBankCardPart, String) -> Unit,
) {
    ShadowElevatedCard(
        modifier = modifier,
        onClick = onClick,
        shape = shape,
        shadowElevation = 7.dp,
        containerColor = Color.DarkGray,
        contentColor = Color.White
    ) {
        Layout(
            modifier = Modifier.fillMaxSize(),
            measurePolicy = { measurables, constraints ->
                val topMargin = (
                        CardDimensions.MAGNETIC_STRIPE_TOP_MARGIN_TO_WIDTH_RATIO *
                                constraints.maxWidth
                        ).roundToInt()
                val magneticStripePlaceable = measurables[0].measure(
                    constraints = Constraints.fixed(
                        width = constraints.maxWidth,
                        height = with(CardDimensions) {
                            (MAGNETIC_STRIPE_THICKNESS_TO_WIDTH_RATIO * constraints.maxWidth)
                                .roundToInt()
                        }
                    )
                )
                val cvvRowPlaceable = measurables.last().measure(
                    Constraints.fixed(
                        width = constraints.maxWidth,
                        height = (constraints.maxHeight * CardDimensions.VISIBLE_HEIGHT_RATIO)
                            .roundToInt()
                    )
                )
                val copyCvvIconButtonPlaceable = measurables[1].measure(
                    constraints = Constraints(
                        minWidth = 0,
                        minHeight = 0,
                        maxWidth = constraints.maxWidth,
                        maxHeight = constraints.maxHeight
                                - topMargin
                                - magneticStripePlaceable.height
                                - cvvRowPlaceable.height
                    )
                )

                layout(constraints.maxWidth, constraints.maxHeight) {
                    magneticStripePlaceable.place(x = 0, y = topMargin)
                    copyCvvIconButtonPlaceable.place(
                        x = constraints.maxWidth - copyCvvIconButtonPlaceable.width,
                        y = constraints.maxHeight
                                - cvvRowPlaceable.height
                                - copyCvvIconButtonPlaceable.height
                    )
                    cvvRowPlaceable.place(
                        x = 0,
                        y = constraints.maxHeight - cvvRowPlaceable.height
                    )
                }
            },
            content = {
                Box(
                    Modifier
                        .background(Color.Black)
                        .fillMaxWidth())

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .clip(CircleShape)
                        .clickable { onCopy(CopyableBankCardPart.Cvv, bankCard.cvv) }
                        .size(CardDimensions.iconButtonSize)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "copy card cvv",
                        modifier = Modifier.scale(.8f)
                    )
                }

                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Absolute.Right
                ) {
                    AutoresizeText(
                        text = stringResource(R.string.cvv),
                        fontSizeRange = FontSizeRange(
                            min = MaterialTheme.typography.bodySmall.fontSize,
                            max = MaterialTheme.typography.bodyMedium.fontSize
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )

                    Spacer(Modifier.size(16.dp))

                    Text(
                        text = if (isVisible) bankCard.cvv else BankCardUtils.getHidden(length = 3),
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                }
            }
        )
    }
}


@Composable
private fun VerticalShadowElevatedCard(
    modifier: Modifier = Modifier,
    onClick: OnClick? = null,
    shape: Shape = MaterialTheme.shapes.small,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onBackground,
    shadowElevation: Dp = 5.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 4.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    content: @Composable (ColumnScope.() -> Unit)
) {
    ShadowElevatedCard(modifier, onClick, shape, containerColor, contentColor, shadowElevation) {
        Column(
            verticalArrangement = verticalArrangement,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}


@Composable
private fun ShadowElevatedCard(
    modifier: Modifier = Modifier,
    onClick: OnClick? = null,
    shape: Shape = MaterialTheme.shapes.small,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onBackground,
    shadowElevation: Dp = 15.dp,
    content: @Composable (() -> Unit)
) {
    when (onClick) {
        null -> Surface(
            shape = shape,
            color = containerColor,
            contentColor = contentColor,
            shadowElevation = shadowElevation,
            tonalElevation = 15.dp,
            modifier = modifier,
            content = content
        )

        else -> Surface(
            shape = shape,
            onClick = onClick,
            color = containerColor,
            contentColor = contentColor,
            shadowElevation = shadowElevation,
            tonalElevation = 15.dp,
            modifier = modifier,
            content = content
        )
    }
}


@Composable
private fun AutoresizeText(
    text: String,
    fontSizeRange: FontSizeRange,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    style: TextStyle = LocalTextStyle.current,
) {
    var fontSizeValue by remember { mutableFloatStateOf(fontSizeRange.max.value) }

    AutoresizeText(
        text = text,
        fontSize = fontSizeValue,
        fontSizeRange = fontSizeRange,
        onResizeText = { fontSizeValue = it },
        modifier = modifier,
        color = color,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        style = style,
    )
}


@Composable
private fun AutoresizeText(
    text: String,
    fontSize: Float,
    fontSizeRange: FontSizeRange,
    onResizeText: (Float) -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    style: TextStyle = LocalTextStyle.current,
) {
    var readyToDraw by remember { mutableStateOf(false) }

    Text(
        text = text,
        color = color,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        style = style,
        fontSize = fontSize.sp,
        onTextLayout = { result ->
            if (result.didOverflowHeight && !readyToDraw) {
                val nextFontSize = fontSize - fontSizeRange.step.value
                if (nextFontSize <= fontSizeRange.min.value) {
                    onResizeText(fontSizeRange.min.value)
                    readyToDraw = true
                } else {
                    onResizeText(nextFontSize)
                }
            } else {
                readyToDraw = true
            }
        },
        modifier = modifier.drawWithContent {
            if (readyToDraw) drawContent()
        }
    )
}


private data class FontSizeRange(
    val min: TextUnit,
    val max: TextUnit,
    val step: TextUnit = 1.sp
) {
    init {
        require(min <= max) { "min should be less or equal than max, $this" }
        require(step.value > 0) { "step should be greater than 0, $this" }
    }
}


@Preview
@PreviewFontScale
@Composable
private fun BankCardComposablePreview() {
    PlasticBankCard(
        bankCard = FullBankCard(
            id = 0,
            number = "2225123423453456",
            paymentSystem = PaymentSystem.MasterCard,
            cvv = "123",
            validityPeriod = "12 / 34"
        ),
        onCopy = { _, _ -> }
    )
}