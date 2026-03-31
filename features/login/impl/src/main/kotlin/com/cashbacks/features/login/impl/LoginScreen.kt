package com.cashbacks.features.login.impl

import android.content.res.Configuration
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cashbacks.common.composables.BoundedSnackbar
import com.cashbacks.common.composables.LoadingInBox
import com.cashbacks.common.composables.theme.CashbacksTheme
import com.cashbacks.common.composables.utils.animate
import com.cashbacks.common.composables.utils.loadingContentAnimationSpec
import com.cashbacks.common.resources.R
import com.cashbacks.common.utils.OnClick
import com.cashbacks.common.utils.mvi.IntentSender
import com.cashbacks.components.login.domain.util.REQUIRED_PASSWORD_LENGTH
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.util.Locale
import kotlin.math.roundToInt

@Composable
internal fun LoginRoot(
    navigateToHomeScreen: () -> Unit,
    viewModel: LoginViewModel = koinViewModel()
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val snackbarHostState = remember(::SnackbarHostState)

    LaunchedEffect(Unit) {
        viewModel.labelFlow.collect { label ->
            when (label) {
                is LoginLabel.DisplayMessage -> launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(label.message)
                }

                is LoginLabel.NavigateToHomeScreen -> navigateToHomeScreen()
            }
        }
    }

    LoginScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        intentSender = IntentSender(viewModel::sendIntent)
    )
}

@Composable
internal fun LoginScreen(
    state: LoginState,
    snackbarHostState: SnackbarHostState,
    intentSender: IntentSender<LoginIntent>,
) {
    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) {
                BoundedSnackbar(it)
            }
        },
        contentWindowInsets = WindowInsets(),
        containerColor = MaterialTheme.colorScheme.background.animate(),
        contentColor = MaterialTheme.colorScheme.onBackground.animate(),
        modifier = Modifier.fillMaxSize()
    ) { contentPadding ->
        Crossfade(
            targetState = state is LoginState.Loading,
            animationSpec = loadingContentAnimationSpec(),
            modifier = Modifier.padding(contentPadding)
        ) { isLoading ->
            when {
                isLoading -> LoadingInBox()
                else -> LoginContent(state, intentSender)
            }
        }
    }
}


@Composable
private fun LoginContent(
    state: LoginState,
    intentSender: IntentSender<LoginIntent>,
    modifier: Modifier = Modifier,
) {
    LoginLayout(
        pinCodeContent = {
            PinCodeField(
                state = state,
                fullLength = REQUIRED_PASSWORD_LENGTH,
                preShowPin = true
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {


            }
        },
        numberPadContent = {
            NumberPad(
                onDigitClick = { intentSender.send(LoginIntent.EnterDigit(it)) },
                onClearDigit = { intentSender.send(LoginIntent.ClearLastDigit) },
                onFingerprintClick = {}
            )
        },
        modifier = modifier.fillMaxSize()
    )
}


@Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
@Composable
private inline fun LoginLayout(
    pinCodeContent: @Composable () -> Unit,
    numberPadContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val screenConfiguration = LocalConfiguration.current
    val measurePolicy: MeasurePolicy = remember(screenConfiguration) {
        MeasurePolicy { measurables, constraints ->
            val numberPadPlaceable =
                measurables.find { it.layoutId == "NumberPad" }!!.let { measurable ->
                    val numberPadRatio = .5f
                    val newConstraints = when {
                        screenConfiguration.isOrientationPortrait() -> Constraints
                            .fixedWidth(width = constraints.maxWidth)
                            .copy(maxHeight = (numberPadRatio * constraints.maxHeight).roundToInt())

                        else -> Constraints
                            .fixedHeight(height = constraints.maxHeight)
                            .copy(maxWidth = (numberPadRatio * constraints.maxWidth).roundToInt())
                    }
                    measurable.measure(newConstraints)
                }

            val pinCodePlaceable = measurables.find { it.layoutId == "PinCode" }!!.let {
                val constraints = if (screenConfiguration.isOrientationPortrait()) {
                    Constraints.fixed(
                        width = constraints.maxWidth,
                        height = constraints.maxHeight - numberPadPlaceable.height
                    )
                } else {
                    Constraints.fixed(
                        width = constraints.maxWidth - numberPadPlaceable.width,
                        height = constraints.maxHeight
                    )
                }
                it.measure(constraints)

            }

            layout(constraints.maxWidth, constraints.maxHeight) {
                pinCodePlaceable.placeRelative(0, 0)

                if (screenConfiguration.isOrientationPortrait()) {
                    numberPadPlaceable.placeRelative(0, pinCodePlaceable.height)
                } else {
                    numberPadPlaceable.placeRelative(pinCodePlaceable.width, 0)
                }
            }
        }
    }

    Layout(
        content = {
            Box(
                modifier = Modifier.layoutId("PinCode"),
                contentAlignment = Alignment.Center,
                propagateMinConstraints = false,
                content = { pinCodeContent() }
            )

            Box(
                modifier = Modifier.layoutId("NumberPad"),
                contentAlignment = Alignment.Center,
                propagateMinConstraints = true,
                content = { numberPadContent() }
            )
        },
        measurePolicy = measurePolicy,
        modifier = modifier
    )
}


@Suppress("SameParameterValue")
@Composable
private fun PinCodeField(
    state: LoginState,
    fullLength: Int,
    preShowPin: Boolean,
    modifier: Modifier = Modifier
) {
    val cacheState = remember { mutableStateOf(state) }
    var showLastEnteredDigit by remember { mutableStateOf(false) }
    val pinCode = remember(state) { state.pinCode.orEmpty() }

    LaunchedEffect(state, fullLength, preShowPin) {
        showLastEnteredDigit = false
        val previousPinCode = cacheState.value.pinCode.orEmpty()
        cacheState.value = state

        if (!preShowPin || state.pinCode == null || previousPinCode.length >= pinCode.length) {
            return@LaunchedEffect
        }

        showLastEnteredDigit = true
        delay(1000)
        showLastEnteredDigit = false
    }


    AnimatedContent(
        targetState = pinCode,
        contentAlignment = Alignment.Center,
        contentKey = { pin -> pin.isEmpty() },
        modifier = modifier
            .padding(horizontal = 32.dp)
            .fillMaxWidth()
    ) { pinCode ->
        if (pinCode.isEmpty()) {
            Text(
                text = state.title ?: return@AnimatedContent,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.animate(),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        32.dp,
                        Alignment.CenterHorizontally
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (index in 1..fullLength) {
                        // TODO: переделать на другой тип анимации
                        Crossfade(
                            targetState = pinCode.length == index,
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = FastOutSlowInEasing
                            )
                        ) {
                            val color = when (state) {
                                is LoginState.Error -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onBackground
                            }.animate()
                            if (it && showLastEnteredDigit) {
                                Text(
                                    text = pinCode.lastOrNull()?.digitToIntOrNull()?.toString()
                                        .orEmpty(),
                                    style = MaterialTheme.typography.displayMedium,
                                    color = color
                                )
                            } else if (index <= pinCode.length) {
                                Icon(
                                    imageVector = Icons.Filled.Circle,
                                    contentDescription = "Filled Circle",
                                    tint = color
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Circle,
                                    contentDescription = "Outlined Circle",
                                    tint = color
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                AnimatedVisibility(
                    visible = !showLastEnteredDigit && state is LoginState.Error && !state.errorMessage.isNullOrBlank(),
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    state as LoginState.Error
                    Text(
                        text = state.errorMessage!!,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error.animate(),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}


private val LoginState.title: String?
    @Composable get() = when (this) {
        is LoginState.Error -> previousState.title
        is LoginState.Loading -> null
        is LoginState.SignIn -> stringResource(R.string.enter_login_code)
        is LoginState.SignUp -> when {
            repeatedPinCode.isEmpty() -> stringResource(R.string.create_login_code)
            else -> stringResource(R.string.repeat_login_code)
        }
    }


@Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
@Composable
private fun NumberPad(
    onDigitClick: (Int) -> Unit,
    onClearDigit: () -> Unit,
    onFingerprintClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val maxOverlaySizeRatio = 0.1f
    val requiredInnerPadding = 16.dp
    val keys = remember {
        buildList {
            for (digit in 1..9) {
                add(Key.Digit(digit))
            }
            add(Key.Fingerprint)
            add(Key.Digit(0))
            add(Key.Backspace)
        }.toImmutableList()
    }

    SubcomposeLayout(modifier = modifier.padding(16.dp)) { constraints ->
        val maxButtonSize = if (configuration.isOrientationPortrait()) {
            constraints.maxWidth / (3 - 2 * maxOverlaySizeRatio)
        } else {
            constraints.maxHeight / (4 - 3 * maxOverlaySizeRatio)
        }.roundToInt()

        val buttonSize = subcompose("probe") {
            keys.forEach {
                KeyboardButton(key = it, onClick = {})
            }
        }.maxOf { measurable ->
            measurable.measure(Constraints()).let { maxOf(it.width, it.height) }
        }.coerceAtMost(maxButtonSize)

        val keyboardPlaceable = subcompose("keyboard") {
            val keyboardMeasurePolicy = remember {
                keyboardMeasurePolicy(
                    buttonSize,
                    configuration.isOrientationPortrait(),
                    requiredInnerPadding.roundToPx()
                )
            }

            Layout(
                content = {
                    keys.forEach { key ->
                        KeyboardButton(
                            key = key,
                            onClick = {
                                when (key) {
                                    is Key.Digit -> onDigitClick(key.value)
                                    is Key.Backspace -> onClearDigit()
                                    is Key.Fingerprint -> onFingerprintClick()
                                }
                            },
                            modifier = Modifier.size(buttonSize.toDp())
                        )
                    }
                },
                measurePolicy = keyboardMeasurePolicy
            )
        }
            .first()
            .measure(constraints)

        layout(keyboardPlaceable.width, keyboardPlaceable.height) {
            keyboardPlaceable.place(0, 0)
        }
    }
}


private fun keyboardMeasurePolicy(
    buttonSize: Int,
    isOrientationPortrait: Boolean,
    requiredInnerPadding: Int,
    columns: Int = 3,
    rows: Int = 4,
) = MeasurePolicy { measurables, constraints ->

    val buttonConstraints = Constraints.fixed(buttonSize, buttonSize)
    val placeables = Array(rows) { i ->
        Array(columns) { j ->
            measurables[columns * i + j].measure(buttonConstraints)
        }
    }
    val buttonOccupiedWidth = buttonSize * columns
    val buttonsOccupiedHeight = buttonSize * rows

    val width: Int
    val height: Int
    val verticalSpacing: Int
    val horizontalSpacing: Int
    val topOffset: Int
    val startOffset: Int
    if (isOrientationPortrait) {
        width = constraints.maxWidth
        val fullSpaceWidth = width - buttonOccupiedWidth
        if (fullSpaceWidth / (columns + 1) >= requiredInnerPadding) {
            horizontalSpacing = fullSpaceWidth / (columns + 1)
            startOffset = horizontalSpacing
        } else if (fullSpaceWidth / (columns - 1) >= requiredInnerPadding) {
            horizontalSpacing = fullSpaceWidth / (columns - 1)
            startOffset = 0
        } else {
            horizontalSpacing = fullSpaceWidth / (columns - 1)
            startOffset = 0
        }
        verticalSpacing = ((constraints.maxHeight - buttonsOccupiedHeight) / (rows - 1))
            .coerceAtMost(requiredInnerPadding)
        height =
            (buttonsOccupiedHeight + verticalSpacing * (rows - 1)).coerceAtMost(constraints.maxHeight)
        topOffset = (height - buttonsOccupiedHeight - verticalSpacing * (rows - 1)).coerceAtLeast(0)
    } else {
        height = constraints.maxHeight
        val fullSpaceHeight = height - buttonsOccupiedHeight
        if (fullSpaceHeight / (rows + 1) >= requiredInnerPadding) {
            verticalSpacing = fullSpaceHeight / (rows + 1)
            topOffset = verticalSpacing
        } else if (fullSpaceHeight / (rows - 1) >= requiredInnerPadding) {
            verticalSpacing = fullSpaceHeight / (rows - 1)
            topOffset = 0
        } else {
            verticalSpacing = fullSpaceHeight / (rows - 1)
            topOffset = 0
        }
        horizontalSpacing = ((constraints.maxWidth - buttonOccupiedWidth) / (columns - 1))
            .coerceAtMost(requiredInnerPadding)
        width =
            (buttonOccupiedWidth + horizontalSpacing * (columns - 1)).coerceAtMost(constraints.maxHeight)
        startOffset = when (layoutDirection) {
            LayoutDirection.Ltr -> 0
            LayoutDirection.Rtl -> (width - buttonOccupiedWidth - horizontalSpacing * (columns - 1))
                .coerceAtLeast(0)
        }
    }

    layout(width, height) {
        var offsetY = topOffset
        for (i in 0..<rows) {
            var offsetX = startOffset
            for (j in 0..<columns) {
                val placeable = placeables[i][j]
                placeable.placeRelative(offsetX, offsetY)
                offsetX += placeable.width + horizontalSpacing
            }
            offsetY += buttonSize + verticalSpacing
        }
    }
}


@Stable
private sealed interface Key {
    data class Digit(val value: Int) : Key
    data object Backspace : Key
    data object Fingerprint : Key
}


@Composable
private fun KeyboardButton(
    key: Key,
    onClick: OnClick,
    modifier: Modifier = Modifier,
    contentSize: TextUnit = MaterialTheme.typography.displaySmall.fontSize,
    shape: RoundedCornerShape = CircleShape
) {
    val density = LocalDensity.current
    val contentPadding = remember(density, contentSize) {
        with(density) {
            val vertical = (contentSize.toDp() * 8) / 30
            val horizontal = (contentSize.toDp() * 24) / 30
            PaddingValues(vertical = vertical, horizontal = horizontal)
        }
    }

    Button(
        onClick = onClick,
        shape = shape,
        modifier = modifier,
        contentPadding = contentPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground.animate()
        ),
    ) {
        when (key) {
            is Key.Digit -> {
                Text(
                    text = key.value.toString(),
                    style = MaterialTheme.typography.displaySmall,
                )
            }

            is Key.Fingerprint -> {
                Icon(
                    imageVector = Icons.Rounded.Fingerprint,
                    contentDescription = "Fingerprint",
                    modifier = Modifier.size(
                        with(density) { contentSize.toDp() }
                    )
                )
            }

            is Key.Backspace -> {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Backspace,
                    contentDescription = "Backspace",
                    modifier = Modifier.size(
                        with(density) { contentSize.toDp() }
                    )
                )
            }
        }
    }
}


private fun Configuration.isOrientationPortrait(): Boolean {
    return orientation == Configuration.ORIENTATION_PORTRAIT
}


@Preview(
    name = "LoginScreen Vertical",
    showBackground = false,
)
@Composable
private fun LoginScreenPreview() {
    CashbacksTheme(isDarkTheme = isSystemInDarkTheme()) {
        var state by remember { mutableStateOf(LoginState.SignIn("")) }
        val snackbarHostState = remember { SnackbarHostState() }

        LoginScreen(
            state = state,
            snackbarHostState = snackbarHostState,
            intentSender = IntentSender { intents, _ ->
                intents.forEach {
                    when (it) {
                        is LoginIntent.ClearLastDigit -> state = state.copy(
                            pinCode = with(state) {
                                pinCode.slice(0..<pinCode.lastIndex)
                            }
                        )

                        is LoginIntent.EnterDigit if (state.pinCode.length < 4) -> state =
                            state.copy(
                                pinCode = state.pinCode + it.digit
                            )

                        else -> {}
                    }
                }
            }
        )
    }
}


@RequiresApi(Build.VERSION_CODES.BAKLAVA)
@Preview(
    name = "LoginScreen RTL Horizontal",
    device = "spec:width=411dp,height=891dp,orientation=landscape,cutout=corner",
    locale = "ar", showBackground = false
)
@Composable
private fun LoginScreenPreview_RtlDirection() {
    CompositionLocalProvider(
        LocalConfiguration provides LocalConfiguration.current.apply {
            setLayoutDirection(Locale.of("ar"))
        }
    ) {
        CashbacksTheme(isDarkTheme = isSystemInDarkTheme()) {
            LoginScreen(
                state = LoginState.Error(
                    exception = Exception("Some error message"),
                    previousState = LoginState.SignUp(pinCode = "1")
                ),
                snackbarHostState = remember { SnackbarHostState() },
                intentSender = IntentSender()
            )
        }
    }
}