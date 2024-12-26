package com.cashbacks.app.ui.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cashbacks.app.ui.theme.CashbacksTheme
import com.cashbacks.app.util.animate
import com.cashbacks.app.util.composableBlock
import com.cashbacks.app.util.composableLet
import kotlinx.coroutines.delay


@Composable
fun DataTextField(
    text: String,
    heading: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingActions: @Composable (RowScope.() -> Unit) = {}
) {
    TextField(
        value = text,
        onValueChange = {},
        textStyle = textStyle,
        label = {
            Text(
                text = heading,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.5.sp,
                ),
            )
        },
        visualTransformation = visualTransformation,
        trailingIcon = {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                trailingActions()
            }
        },
        readOnly = true,
        enabled = false,
        keyboardOptions = KeyboardOptions(),
        colors = TextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onBackground.animate(),
            disabledTextColor = MaterialTheme.colorScheme.onBackground.animate(),
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedLabelColor = MaterialTheme.colorScheme.onBackground.animate(),
            unfocusedLabelColor = MaterialTheme.colorScheme.onBackground.animate(),
            disabledLabelColor = MaterialTheme.colorScheme.onBackground.animate(),
            focusedTrailingIconColor = MaterialTheme.colorScheme.onBackground.animate(),
            unfocusedTrailingIconColor = MaterialTheme.colorScheme.onBackground.animate(),
            disabledTrailingIconColor = MaterialTheme.colorScheme.onBackground.animate()
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun NewNameTextField(
    modifier: Modifier = Modifier,
    placeholder: String = "",
    onSave: (name: String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember(::FocusRequester)

    LaunchedEffect(Unit) {
        delay(50)
        focusRequester.requestFocus()
    }

    val keyboardParams = KeyboardParams(
        imeAction = ImeAction.Done,
        capitalization = KeyboardCapitalization.Sentences,
        onImeActionClick = { onSave(name) }
    )

    OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        placeholder = {
            Text(text = placeholder, style = MaterialTheme.typography.bodyMedium)
        },
        textStyle = MaterialTheme.typography.bodyMedium,
        keyboardOptions = keyboardParams.options,
        keyboardActions = keyboardParams.actions,
        shape = MaterialTheme.shapes.medium,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onBackground.animate(),
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground.animate(),
            focusedContainerColor = MaterialTheme.colorScheme.surface.animate(),
            unfocusedContainerColor = MaterialTheme.colorScheme.background.animate(),
            focusedBorderColor = MaterialTheme.colorScheme.onBackground.animate(),
            unfocusedBorderColor = Color.Transparent
        ),
        modifier = Modifier
            .focusRequester(focusRequester)
            .padding(16.dp)
            .then(modifier)
            .fillMaxWidth()
    )
}




@Composable
fun EditableTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    error: Boolean = false,
    errorMessage: String? = null,
    textStyle: TextStyle = when {
        enabled -> MaterialTheme.typography.bodyMedium
        else -> MaterialTheme.typography.bodyLarge.copy(
            textAlign = TextAlign.Center,
            letterSpacing = 3.sp,
            fontWeight = FontWeight.Bold
        )
    },
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    keyboardCapitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    onImeActionClick: KeyboardActionScope.(text: String) -> Unit = {},
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingActions: @Composable (RowScope.() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    shape: Shape = MaterialTheme.shapes.medium,
    colors: EditableTextFieldColors = EditableTextFieldDefaults.colors()
) {
    EditableTextField(
        value = value,
        onValueChange = { onValueChange(it as TextFieldValue) },
        text = value.text,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        error = error,
        errorMessage = errorMessage,
        textStyle = textStyle,
        maxLines = maxLines,
        keyboardType = keyboardType,
        imeAction = imeAction,
        keyboardCapitalization = keyboardCapitalization,
        onImeActionClick = onImeActionClick,
        visualTransformation = visualTransformation,
        leadingIcon = leadingIcon,
        trailingActions = trailingActions,
        prefix = prefix,
        suffix = suffix,
        shape = shape,
        colors = colors
    )
}




@Composable
fun EditableTextField(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    error: Boolean = false,
    errorMessage: String? = null,
    textStyle: TextStyle = when {
        enabled -> MaterialTheme.typography.bodyMedium
        else -> MaterialTheme.typography.bodyLarge.copy(
            textAlign = TextAlign.Center,
            letterSpacing = 3.sp,
            fontWeight = FontWeight.Bold
        )
    },
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    keyboardCapitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    onImeActionClick: KeyboardActionScope.(text: String) -> Unit = {},
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingActions: @Composable (RowScope.() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    shape: Shape = MaterialTheme.shapes.medium,
    colors: EditableTextFieldColors = EditableTextFieldDefaults.colors()
) {
    EditableTextField(
        value = text,
        onValueChange = { onTextChange(it as String) },
        text = text,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        error = error,
        errorMessage = errorMessage,
        textStyle = textStyle,
        maxLines = maxLines,
        keyboardType = keyboardType,
        imeAction = imeAction,
        keyboardCapitalization = keyboardCapitalization,
        onImeActionClick = onImeActionClick,
        visualTransformation = visualTransformation,
        leadingIcon = leadingIcon,
        trailingActions = trailingActions,
        prefix = prefix,
        suffix = suffix,
        shape = shape,
        colors = colors
    )
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> EditableTextField(
    value: T,
    onValueChange: (Any) -> Unit,
    text: String,
    modifier: Modifier,
    label: String?,
    placeholder: String?,
    enabled: Boolean,
    readOnly: Boolean,
    singleLine: Boolean,
    error: Boolean,
    errorMessage: String?,
    textStyle: TextStyle,
    maxLines: Int,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    keyboardCapitalization: KeyboardCapitalization,
    onImeActionClick: KeyboardActionScope.(text: String) -> Unit,
    visualTransformation: VisualTransformation,
    leadingIcon: @Composable (() -> Unit)?,
    trailingActions: @Composable (RowScope.() -> Unit)?,
    prefix: @Composable (() -> Unit)?,
    suffix: @Composable (() -> Unit)?,
    shape: Shape,
    colors: EditableTextFieldColors
) {
    val focusManager = LocalFocusManager.current
    var showSupportingText by rememberSaveable { mutableStateOf(true) }

    val keyboardParams = KeyboardParams(
        type = keyboardType,
        imeAction = imeAction,
        capitalization = keyboardCapitalization,
        autoCorrect = false
    ) {
        onImeActionClick(text)
        when (imeAction) {
            ImeAction.Next -> focusManager.moveFocus(FocusDirection.Down)
            ImeAction.Done -> focusManager.clearFocus()
        }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderBrush = when {
        error -> SolidColor(MaterialTheme.colorScheme.error)
        readOnly -> SolidColor(colors.focusedTextColor)
        else -> colors.borderColors(enabled)
            .takeIf { it.isNotEmpty() }
            ?.let { Brush.horizontalGradient(colors = it.toList()) }
    }
    val cursorBrush = when {
        error || readOnly -> borderBrush
        else -> colors.cursorColors
            .takeIf { it.isNotEmpty() }
            ?.let { Brush.verticalGradient(colors = it.toList(), tileMode = TileMode.Mirror) }
    } ?: SolidColor(colors.textColor(enabled, readOnly = false, isError = false, isFocused))


    @Composable
    fun DecorationBox(innerTextField: @Composable () -> Unit) {
        val label: @Composable (() -> Unit)? = label?.composableLet { label ->
            Text(
                text = label,
                style = when {
                    text.isEmpty() && !isFocused -> MaterialTheme.typography.bodyMedium
                    else -> MaterialTheme.typography.bodySmall
                },
                maxLines = 1,
                color = when {
                    error -> colors.errorLabelColor
                    readOnly -> colors.disabledLabelColor
                    text.isEmpty() && !isFocused -> colors.unfocusedLabelColor
                    else -> colors.focusedLabelColor
                }
            )
        }

        val placeholder: @Composable (() -> Unit)? = placeholder?.composableLet { placeholder ->
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = colors.unfocusedTextColor
            )
        }

        val supportingText: @Composable (() -> Unit)? = errorMessage
            ?.takeIf { enabled && error && errorMessage.isNotBlank() }
            ?.composableLet {
                AnimatedVisibility(
                    visible = showSupportingText,
                    enter = expandVertically(
                        animationSpec = tween(
                            durationMillis = 250,
                            easing = LinearEasing
                        )
                    ),
                    exit = shrinkVertically(
                        animationSpec = tween(
                            durationMillis = 250,
                            easing = LinearEasing
                        )
                    )
                ) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

        val leadingIcon: @Composable (() -> Unit)? = composableBlock(
            condition = error || leadingIcon != null
        ) {
            when {
                error -> IconButton(
                    onClick = { showSupportingText = !showSupportingText },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.Error, contentDescription = "error")
                }

                leadingIcon != null -> leadingIcon()
            }
        }

        val trailingIcon: @Composable (() -> Unit)? = trailingActions?.composableLet {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Spacer(modifier = Modifier.width(4.dp))
                trailingActions()
            }
        }


        when (borderBrush) {
            null -> TextFieldDefaults.DecorationBox(
                value = text,
                innerTextField = innerTextField,
                enabled = enabled,
                singleLine = singleLine,
                visualTransformation = visualTransformation,
                interactionSource = interactionSource,
                isError = error,
                label = label,
                placeholder = placeholder,
                supportingText = supportingText,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                prefix = prefix,
                suffix = suffix,
                contentPadding = TextFieldDefaults.contentPaddingWithLabel(),
                colors = colors.textFieldColors
            )

            else -> OutlinedTextFieldDefaults.DecorationBox(
                value = text,
                innerTextField = innerTextField,
                enabled = enabled,
                singleLine = singleLine,
                visualTransformation = visualTransformation,
                interactionSource = interactionSource,
                isError = error,
                label = label,
                placeholder = placeholder,
                supportingText = supportingText,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                prefix = prefix,
                suffix = suffix,
                contentPadding = OutlinedTextFieldDefaults.contentPadding(),
                colors = colors.textFieldColors,
                container = {
                    val borderModifier = Modifier.border(
                        border = animateBorderStrokeAsState(
                            borderBrush = borderBrush,
                            enabled = !readOnly,
                            interactionSource = interactionSource,
                            focusedBorderThickness = 2.dp,
                            unfocusedBorderThickness = 1.dp
                        ).value,
                        shape = shape
                    )

                    Box(
                        modifier = Modifier
                            .then(borderModifier)
                            .background(
                                color = colors.containerColor(enabled, error, isFocused),
                                shape = shape
                            )
                    )
                }
            )
        }
    }

    when (value) {
        is String -> BasicTextField(
            modifier = modifier.padding(vertical = 2.dp),
            text = value,
            onTextChange = onValueChange,
            readOnly = readOnly,
            singleLine = singleLine,
            maxLines = maxLines,
            enabled = enabled,
            cursorBrush = cursorBrush,
            textStyle = textStyle.copy(color = colors.textColor(enabled, readOnly, error, isFocused)),
            interactionSource = interactionSource,
            keyboardParams = keyboardParams,
            visualTransformation = visualTransformation,
            decorationBox = { DecorationBox(it) }
        )

        is TextFieldValue -> BasicTextField(
            modifier = modifier.padding(top = 1.dp),
            text = value,
            onTextChange = onValueChange,
            readOnly = readOnly,
            singleLine = singleLine,
            maxLines = maxLines,
            enabled = enabled,
            cursorBrush = cursorBrush,
            textStyle = textStyle.copy(color = colors.textColor(enabled, readOnly, error, isFocused)),
            interactionSource = interactionSource,
            keyboardParams = keyboardParams,
            visualTransformation = visualTransformation,
            decorationBox = { DecorationBox(it) }
        )
    }
}




@Composable
private fun BasicTextField(
    modifier: Modifier,
    text: String,
    onTextChange: (String) -> Unit,
    readOnly: Boolean,
    singleLine: Boolean,
    maxLines: Int,
    enabled: Boolean,
    cursorBrush: Brush,
    textStyle: TextStyle,
    interactionSource: MutableInteractionSource,
    keyboardParams: KeyboardParams,
    visualTransformation: VisualTransformation,
    decorationBox: @Composable (innerTextField: @Composable () -> Unit) -> Unit
) {
    BasicTextField(
        value = text,
        onValueChange = onTextChange,
        readOnly = readOnly,
        singleLine = singleLine,
        maxLines = maxLines,
        enabled = enabled,
        cursorBrush = cursorBrush,
        textStyle = textStyle,
        interactionSource = interactionSource,
        keyboardOptions = keyboardParams.options,
        keyboardActions = keyboardParams.actions,
        visualTransformation = visualTransformation,
        decorationBox = decorationBox,
        modifier = modifier
    )
}


@Composable
private fun BasicTextField(
    modifier: Modifier,
    text: TextFieldValue,
    onTextChange: (TextFieldValue) -> Unit,
    readOnly: Boolean,
    singleLine: Boolean,
    maxLines: Int,
    enabled: Boolean,
    cursorBrush: Brush,
    textStyle: TextStyle,
    interactionSource: MutableInteractionSource,
    keyboardParams: KeyboardParams,
    visualTransformation: VisualTransformation,
    decorationBox: @Composable (innerTextField: @Composable () -> Unit) -> Unit
) {
    BasicTextField(
        value = text,
        onValueChange = onTextChange,
        readOnly = readOnly,
        singleLine = singleLine,
        maxLines = maxLines,
        enabled = enabled,
        cursorBrush = cursorBrush,
        textStyle = textStyle,
        interactionSource = interactionSource,
        keyboardOptions = keyboardParams.options,
        keyboardActions = keyboardParams.actions,
        visualTransformation = visualTransformation,
        decorationBox = decorationBox,
        modifier = modifier
    )
}




internal class KeyboardParams private constructor(
    val options: KeyboardOptions = KeyboardOptions(),
    private val onImeActionClick: (KeyboardActionScope.() -> Unit)? = null
) {
    constructor(
        type: KeyboardType = KeyboardType.Text,
        imeAction: ImeAction = ImeAction.Next,
        capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
        autoCorrect: Boolean = true,
        onImeActionClick: (KeyboardActionScope.() -> Unit)? = null
    ) : this(
        options = KeyboardOptions(
            capitalization = capitalization,
            autoCorrectEnabled = autoCorrect,
            keyboardType = type,
            imeAction = imeAction
        ),
        onImeActionClick = onImeActionClick
    )


    val actions = KeyboardActions(
        onDone = { onImeClick(ImeAction.Done) },
        onGo = { onImeClick(ImeAction.Go) },
        onNext = { onImeClick(ImeAction.Next) },
        onPrevious = { onImeClick(ImeAction.Previous) },
        onSearch = { onImeClick(ImeAction.Search) },
        onSend = { onImeClick(ImeAction.Send) }
    )


    private fun KeyboardActionScope.onImeClick(imeAction: ImeAction) =
        when (options.imeAction) {
            imeAction -> onImeActionClick?.invoke(this) ?: defaultKeyboardAction(imeAction)
            else -> defaultKeyboardAction(imeAction)
        }
}



object EditableTextFieldDefaults {
    @Composable
    fun colors(
        focusedTextColor: Color = MaterialTheme.colorScheme.onBackground,
        unfocusedTextColor: Color = focusedTextColor,
        disabledTextColor: Color = focusedTextColor,
        errorTextColor: Color = focusedTextColor,
        focusedContainerColor: Color = Color.Transparent,
        unfocusedContainerColor: Color = Color.Transparent,
        disabledContainerColor: Color = Color.Transparent,
        errorContainerColor: Color = Color.Transparent,
        focusedLeadingIconColor: Color = focusedTextColor,
        unfocusedLeadingIconColor: Color = focusedTextColor,
        disabledLeadingIconColor: Color = unfocusedTextColor,
        errorLeadingIconColor: Color = MaterialTheme.colorScheme.error,
        focusedTrailingActionsColor: Color = Color.Unspecified,
        unfocusedTrailingActionsColor: Color = Color.Unspecified,
        disabledTrailingActionsColor: Color = Color.Unspecified,
        errorTrailingActionsColor: Color = MaterialTheme.colorScheme.error,
        focusedLabelColor: Color = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor: Color = disabledTextColor,
        disabledLabelColor: Color = disabledTextColor,
        errorLabelColor: Color = MaterialTheme.colorScheme.error,
        focusedSupportingTextColor: Color = MaterialTheme.colorScheme.error,
        unfocusedSupportingTextColor: Color = MaterialTheme.colorScheme.error,
        disabledSupportingTextColor: Color = MaterialTheme.colorScheme.error,
        errorSupportingTextColor: Color = MaterialTheme.colorScheme.error,
        focusedIndicatorColor: Color = Color.Unspecified,
        unfocusedIndicatorColor: Color = focusedIndicatorColor,
        disabledIndicatorColor: Color = focusedIndicatorColor,
        errorIndicatorColor: Color = focusedIndicatorColor,
        enabledBorderColors: Collection<Color> = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.onBackground
        ),
        disabledBorderColors: Collection<Color> = enabledBorderColors,
        cursorColors: Collection<Color> = enabledBorderColors,
    ): EditableTextFieldColors = EditableTextFieldColors(
        focusedTextColor = focusedTextColor,
        unfocusedTextColor = unfocusedTextColor,
        disabledTextColor = disabledTextColor,
        errorTextColor = errorTextColor,
        focusedContainerColor = focusedContainerColor,
        unfocusedContainerColor = unfocusedContainerColor,
        disabledContainerColor = disabledContainerColor,
        errorContainerColor = errorContainerColor,
        focusedLeadingIconColor = focusedLeadingIconColor,
        unfocusedLeadingIconColor = unfocusedLeadingIconColor,
        disabledLeadingIconColor = disabledLeadingIconColor,
        errorLeadingIconColor = errorLeadingIconColor,
        focusedTrailingActionsColor = focusedTrailingActionsColor,
        unfocusedTrailingActionsColor = unfocusedTrailingActionsColor,
        disabledTrailingActionsColor = disabledTrailingActionsColor,
        errorTrailingActionsColor = errorTrailingActionsColor,
        focusedLabelColor = focusedLabelColor,
        unfocusedLabelColor = unfocusedLabelColor,
        disabledLabelColor = disabledLabelColor,
        errorLabelColor = errorLabelColor,
        focusedSupportingTextColor = focusedSupportingTextColor,
        unfocusedSupportingTextColor = unfocusedSupportingTextColor,
        disabledSupportingTextColor = disabledSupportingTextColor,
        errorSupportingTextColor = errorSupportingTextColor,
        focusedIndicatorColor = focusedIndicatorColor,
        unfocusedIndicatorColor = unfocusedIndicatorColor,
        disabledIndicatorColor = disabledIndicatorColor,
        errorIndicatorColor = errorIndicatorColor,
        enabledBorderColors = enabledBorderColors,
        disabledBorderColors = disabledBorderColors,
        cursorColors = cursorColors
    )

    fun passwordVisualTransformation(isVisible: Boolean) = when {
        isVisible -> VisualTransformation.None
        else -> PasswordVisualTransformation()
    }
}



data class EditableTextFieldColors(
    val focusedTextColor: Color,
    val unfocusedTextColor: Color,
    val disabledTextColor: Color,
    val errorTextColor: Color,
    val focusedContainerColor: Color,
    val unfocusedContainerColor: Color,
    val disabledContainerColor: Color,
    val errorContainerColor: Color,
    val focusedLeadingIconColor: Color,
    val unfocusedLeadingIconColor: Color,
    val disabledLeadingIconColor: Color,
    val errorLeadingIconColor: Color,
    val focusedTrailingActionsColor: Color,
    val unfocusedTrailingActionsColor: Color,
    val disabledTrailingActionsColor: Color,
    val errorTrailingActionsColor: Color,
    val focusedLabelColor: Color,
    val unfocusedLabelColor: Color,
    val disabledLabelColor: Color,
    val errorLabelColor: Color,
    val focusedSupportingTextColor: Color,
    val unfocusedSupportingTextColor: Color,
    val disabledSupportingTextColor: Color,
    val errorSupportingTextColor: Color,
    val focusedIndicatorColor: Color,
    val unfocusedIndicatorColor: Color,
    val disabledIndicatorColor: Color,
    val errorIndicatorColor: Color,
    val enabledBorderColors: Collection<Color>,
    val disabledBorderColors: Collection<Color>,
    val cursorColors: Collection<Color>,
) {
    val textFieldColors: TextFieldColors
        @Composable
        @Stable
        get() = TextFieldDefaults.colors(
            focusedTextColor = focusedTextColor.animate(),
            unfocusedTextColor = unfocusedTextColor.animate(),
            disabledTextColor = disabledTextColor.animate(),
            errorTextColor = errorTextColor.animate(),
            focusedContainerColor = focusedContainerColor.animate(),
            unfocusedContainerColor = unfocusedContainerColor.animate(),
            disabledContainerColor = disabledContainerColor.animate(),
            errorContainerColor = errorContainerColor.animate(),
            focusedLeadingIconColor = focusedLeadingIconColor.animate(),
            unfocusedLeadingIconColor = unfocusedLeadingIconColor.animate(),
            disabledLeadingIconColor = disabledLeadingIconColor.animate(),
            errorLeadingIconColor = errorLeadingIconColor.animate(),
            focusedTrailingIconColor = focusedTrailingActionsColor.animate(),
            unfocusedTrailingIconColor = unfocusedTrailingActionsColor.animate(),
            disabledTrailingIconColor = disabledTrailingActionsColor.animate(),
            errorTrailingIconColor = errorTrailingActionsColor.animate(),
            focusedSupportingTextColor = focusedSupportingTextColor.animate(),
            unfocusedSupportingTextColor = unfocusedSupportingTextColor.animate(),
            disabledSupportingTextColor = disabledSupportingTextColor.animate(),
            errorSupportingTextColor = errorSupportingTextColor.animate(),
            focusedIndicatorColor = focusedIndicatorColor.animate(),
            unfocusedIndicatorColor = unfocusedIndicatorColor.animate(),
            disabledIndicatorColor = disabledIndicatorColor.animate(),
            errorIndicatorColor = errorIndicatorColor.animate()
        )


    @Stable
    internal fun borderColors(enabled: Boolean) = when {
        enabled -> enabledBorderColors
        else -> disabledBorderColors
    }

    @Stable
    internal fun textColor(
        enabled: Boolean,
        readOnly: Boolean,
        isError: Boolean,
        focused: Boolean
    ): Color = when {
        !enabled || readOnly -> disabledTextColor
        isError -> errorTextColor
        focused -> focusedTextColor
        else -> unfocusedTextColor
    }

    internal fun containerColor(
        enabled: Boolean,
        isError: Boolean,
        focused: Boolean
    ): Color = when {
        !enabled -> disabledContainerColor
        isError -> errorContainerColor
        focused -> focusedContainerColor
        else -> unfocusedContainerColor
    }
}



@Composable
private fun animateBorderStrokeAsState(
    borderBrush: Brush,
    enabled: Boolean,
    interactionSource: InteractionSource,
    focusedBorderThickness: Dp = OutlinedTextFieldDefaults.FocusedBorderThickness,
    unfocusedBorderThickness: Dp = OutlinedTextFieldDefaults.UnfocusedBorderThickness
): State<BorderStroke> {

    val focused by interactionSource.collectIsFocusedAsState()
    val targetThickness = if (focused) focusedBorderThickness else unfocusedBorderThickness

    val animatedThickness = when {
        enabled -> targetThickness.animate(durationMillis = 150)
        else -> rememberUpdatedState(unfocusedBorderThickness).value
    }

    return rememberUpdatedState(
        BorderStroke(animatedThickness, borderBrush)
    )
}


@Preview
@Composable
private fun NewNameTextFieldPreview() {
    CashbacksTheme(isDarkTheme = false) {
        NewNameTextField(
            placeholder = "Imagine Dragons",
            onSave = {}
        )
    }
}





@Preview(showBackground = true)
@Composable
private fun EditableTextFieldPreview() {
    CashbacksTheme(isDarkTheme = false, dynamicColor = false) {
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            EditableTextField(
                enabled = false,
                text = "Wrecked",
                onTextChange = {},
                label = "Imagine Dragons"
            )

            EditableTextField(
                text = "Wrecked",
                onTextChange = {},
                label = "Imagine Dragons",
                error = false,
                errorMessage = "Error Message",
            )
        }
    }
}
