package com.cashbacks.app.ui.features.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsIgnoringVisibility
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.cashbacks.app.ui.navigation.enterScreenTransition
import com.cashbacks.app.ui.navigation.exitScreenTransition
import com.cashbacks.app.util.AnimationDefaults
import com.cashbacks.app.util.animate
import com.cashbacks.app.util.keyboardAsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


internal enum class HomeTopAppBarState {
    Search,
    TopBar
}


private object AppBarDefaults {
    val EnterFloatSpec: FiniteAnimationSpec<Float> = tween(
        durationMillis = 600,
        delayMillis = 100,
        easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    )

    val ExitFloatSpec: FiniteAnimationSpec<Float> = tween(
        durationMillis = 500,
        delayMillis = 100,
        easing = CubicBezierEasing(0.0f, 1.0f, 0.0f, 1.0f)
    )

    val SearchBarCornerRadius = 20.dp
}


@Composable
internal fun HomeTopAppBar(
    title: String,
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    state: HomeTopAppBarState = HomeTopAppBarState.TopBar,
    onStateChange: (HomeTopAppBarState) -> Unit = {},
    searchPlaceholder: String = "",
    onNavigationIconClick: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()

    AnimatedContent(
        targetState = state,
        label = "search animation",
        transitionSpec = {
            val expandFrom = when (targetState) {
                HomeTopAppBarState.Search -> Alignment.End
                HomeTopAppBarState.TopBar -> Alignment.Start
            }
            val shrinkTowards = when (initialState) {
                HomeTopAppBarState.Search -> Alignment.End
                HomeTopAppBarState.TopBar -> Alignment.Start
            }
            enterScreenTransition(expandFrom) togetherWith exitScreenTransition(shrinkTowards)
        },
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
    ) { homeTopAppBarState ->
        when (homeTopAppBarState) {
            HomeTopAppBarState.Search -> SearchBar(
                modifier = Modifier.fillMaxWidth(),
                query = query,
                placeholder = searchPlaceholder,
                onQueryChange = onQueryChange,
                onClose = {
                    scope.launch {
                        onStateChange(HomeTopAppBarState.TopBar)
                        delay(100)
                        onQueryChange("")
                    }

                },
            )

            HomeTopAppBarState.TopBar -> TopBar(
                title = title,
                onNavigationIconClick = onNavigationIconClick,
                onSearch = { onStateChange(HomeTopAppBarState.Search) }
            )
        }
    }

    BackHandler(enabled = state == HomeTopAppBarState.Search) {
        scope.launch {
            onStateChange(HomeTopAppBarState.TopBar)
            delay(100)
            onQueryChange("")
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchBar(
    modifier: Modifier,
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    onClose: () -> Unit,
) {
    val focusRequester = remember(::FocusRequester)
    val active = rememberSaveable { mutableStateOf(true) }
    val interactionSource = remember(::MutableInteractionSource)
    val focusManager = LocalFocusManager.current

    val animationProgress = animateFloatAsState(
        targetValue = if (active.value) 1f else 0f,
        label = "search field anim",
        animationSpec = when {
            active.value -> AppBarDefaults.EnterFloatSpec
            else -> AppBarDefaults.ExitFloatSpec
        }
    )
    val density = LocalDensity.current

    val animatedShape = remember {
        GenericShape { size, _ ->
            val radius = with(density) {
                (AppBarDefaults.SearchBarCornerRadius * (1 - animationProgress.value)).toPx()
            }
            addRoundRect(RoundRect(size.toRect(), CornerRadius(radius)))
        }
    }

    val activeBackgroundColor = MaterialTheme.colorScheme.surface
    val inactiveBackgroundColor = MaterialTheme.colorScheme.background.copy(alpha = .7f)
    val animatedColor = remember {
        derivedStateOf {
            mixColors(
                firstColor = activeBackgroundColor,
                secondColor = inactiveBackgroundColor,
                ratio = animationProgress.value
            )
        }
    }


    val keyboardState = keyboardAsState()
    LaunchedEffect(Unit) {
        delay(AnimationDefaults.ScreenDelayMillis + 100L)
        focusRequester.requestFocus()
        delay(700)
        snapshotFlow { keyboardState.value }.collect { isKeyboardVisible ->
            if (!isKeyboardVisible) active.value = false
        }
    }


    Surface(
        color = animatedColor.value,
        modifier = Modifier
            .then(modifier)
            .zIndex(1f)
            .windowInsetsPadding(
                WindowInsets.systemBarsIgnoringVisibility.only(WindowInsetsSides.Horizontal)
            )
    ) {
        val horizontalPadding = remember {
            derivedStateOf { 16.dp }
        }
        val textFieldPadding = AnimatedPaddingValues(animationProgress, horizontalPadding)

        TextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium,
            shape = animatedShape,
            interactionSource = interactionSource,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null
                )
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { active.value = false }),
            trailingIcon = {
                IconButton(onClick = onClose) {
                    Icon(imageVector = Icons.Rounded.Close, contentDescription = "close search bar")
                }
            },
            placeholder = {
                Text(text = placeholder, style = MaterialTheme.typography.bodyMedium)
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                errorContainerColor = MaterialTheme.colorScheme.surface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                unfocusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
                unfocusedTrailingIconColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .padding(textFieldPadding)
                .windowInsetsPadding(
                    WindowInsets.systemBarsIgnoringVisibility.only(WindowInsetsSides.Top)
                )
                .focusRequester(focusRequester)
                .onFocusChanged { if (it.isFocused) active.value = true }
                .semantics {
                    onClick {
                        focusRequester.requestFocus()
                        true
                    }
                }
        )
    }

    val isFocused = interactionSource.collectIsFocusedAsState()
    LaunchedEffect(active.value) {
        if (!active.value && isFocused.value) {
            delay(100)
            focusManager.clearFocus()
        }
    }

    BackHandler(enabled = active.value) {
        active.value = false
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    title: String,
    onNavigationIconClick: () -> Unit,
    onSearch: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onNavigationIconClick) {
                Icon(Icons.Rounded.Menu, contentDescription = "open menu")
            }
        },
        actions = {
            IconButton(onClick = onSearch) {
                Icon(imageVector = Icons.Rounded.Search, contentDescription = "search")
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary.animate(),
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary.animate(),
            titleContentColor = MaterialTheme.colorScheme.onPrimary.animate(),
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary.animate()
        )
    )
}


@Stable
private class AnimatedPaddingValues(
    val animationProgress: State<Float>,
    val horizontalPadding: State<Dp>
) : PaddingValues {
    override fun calculateTopPadding(): Dp = 8.dp * (1 - animationProgress.value)
    override fun calculateBottomPadding(): Dp = 8.dp * animationProgress.value

    override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp =
        horizontalPadding.value * (1 - animationProgress.value)
    override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp =
        horizontalPadding.value * (1 - animationProgress.value)

}


private fun mixColors(firstColor: Color, secondColor: Color, ratio: Float): Color {
    val secondColorRatio = 1 - ratio

    fun calculateComponent(getComponent: Color.() -> Float): Float {
        val first = firstColor.getComponent() * ratio
        val second = secondColor.getComponent() * secondColorRatio
        return first + second
    }

    return Color(
        red = calculateComponent { red },
        green = calculateComponent { green },
        blue = calculateComponent { blue }
    )
}