package com.cashbacks.app.ui.composables

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.tappableElement
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cashbacks.app.R
import com.cashbacks.app.ui.composables.ModalSheetItems.ScreenTypeItem
import com.cashbacks.app.ui.screens.navigation.AppScreens
import com.cashbacks.app.ui.theme.CashbacksTheme
import com.cashbacks.app.ui.theme.DarkerGray
import com.cashbacks.app.util.animate

@Composable
internal fun ModalNavigationDrawerContent(items: @Composable ColumnScope.() -> Unit) {
    val configuration = LocalConfiguration.current

    ModalDrawerSheet(
        drawerShape = ModalSheetDefaults.NavigationDrawerShape,
        drawerContainerColor = MaterialTheme.colorScheme.background.animate(),
        drawerContentColor = MaterialTheme.colorScheme.onBackground.animate(),
        windowInsets = WindowInsets.tappableElement.only(WindowInsetsSides.Bottom),
        modifier = Modifier
            .padding(end = 80.dp)
            .fillMaxHeight()
            .width(
                width = minOf(
                    configuration.screenWidthDp,
                    configuration.screenHeightDp
                ).dp
            )
            .verticalScroll(rememberScrollState())
    ) {
        NavHeader(
            modifier = Modifier.windowInsetsPadding(
                WindowInsets.statusBars.only(WindowInsetsSides.Top)
            )
        )
        Spacer(modifier = Modifier.size(16.dp))
        items()
    }
}


@Composable
private fun NavHeader(modifier: Modifier = Modifier) {
    Column(
        modifier = Modifier
            .background(
                Brush.linearGradient(
                    0f to MaterialTheme.colorScheme.primaryContainer.animate(),
                    0.5f to MaterialTheme.colorScheme.primary.animate(),
                    1f to MaterialTheme.colorScheme.secondary.animate(),
                )
            )
            .then(modifier)
            .padding(vertical = 16.dp, horizontal = 17.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {

        Image(
            painter = painterResource(R.drawable.icon),
            contentDescription = "navigation header",
            alignment = Alignment.Center,
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.app_name),
            color = MaterialTheme.colorScheme.onPrimary.animate(),
            style = MaterialTheme.typography.titleSmall
        )
        Spacer(Modifier.height(3.dp))
        Text(
            stringResource(R.string.app_version),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimary.animate().copy(alpha = 0.8f)
        )
    }
}


@Composable
fun ColumnScope.BottomSheetContent(
    title: String = "",
    subtitle: String = "",
    beautifulDesign: Boolean = false,
    bodyContent: @Composable (ColumnScope.() -> Unit)
) = BottomSheetContent(
    header = Header(title, subtitle, beautifulDesign),
    bodyContent = bodyContent
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.BottomSheetContent(
    header: Header = Header(),
    contentWindowInsets: WindowInsets =
        WindowInsets
            .tappableElement.only(WindowInsetsSides.Bottom)
            .union(
                WindowInsets.statusBars.only(WindowInsetsSides.Top)
            ),
    bodyContent: @Composable (ColumnScope.() -> Unit),
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val initialTopPadding = contentWindowInsets
        .asPaddingValues()
        .calculateTopPadding()

    var topInsetsPadding by remember { mutableStateOf(initialTopPadding) }

    val scrollModifier = when (configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> Modifier.verticalScroll(rememberScrollState())
        else -> Modifier
    }

    Column(
        modifier = Modifier
            .then(scrollModifier)
            .align(Alignment.CenterHorizontally)
            .onGloballyPositioned {
                with(density) {
                    topInsetsPadding = when {
                        it.size.height.toDp() >= configuration.screenHeightDp.dp / 2 -> initialTopPadding
                        else -> 0.dp
                    }
                }
                it.size.height
            }
    ) {
        if (header.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(top = topInsetsPadding)
                    .align(Alignment.CenterHorizontally)
            ) {
                BottomSheetDefaults.DragHandle()
            }
        } else {
            Spacer(modifier = Modifier.padding(top = topInsetsPadding))
            BottomSheetHeader(header)
            Spacer(modifier = Modifier.size(8.dp))
        }

        bodyContent()
        Spacer(
            modifier = Modifier
                .windowInsetsPadding(
                    contentWindowInsets.only(WindowInsetsSides.Bottom)
                )
                .size(8.dp),
        )
    }
}


@Composable
private fun ColumnScope.BottomSheetHeader(header: Header) {
    Column(
        modifier = Modifier
            .padding(top = 16.dp)
            .align(Alignment.Start)
            .fillMaxWidth()
    ) {
        val style = when {
            header.beautifulDesign -> MaterialTheme.typography.headlineLarge
            else -> MaterialTheme.typography.titleMedium
        }
        Text(
            text = header.title,
            style = style,
            color = MaterialTheme.colorScheme.onBackground.animate(),
            modifier = Modifier
                .padding(start = 16.dp)
                .fillMaxWidth()
        )

        if (header.subtitle.isNotBlank()) {
            val style1 = MaterialTheme.typography.titleSmall
            Text(
                text = header.subtitle,
                color = DarkerGray,
                style = style1,
                modifier = Modifier
                    .padding(start = 16.dp, bottom = 4.dp)
                    .fillMaxWidth()
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(top = 16.dp),
            color = DarkerGray
        )
    }
}


object ModalSheetItems {
    @Composable
    fun ColumnScope.ImageTextItem(
        text: String,
        @DrawableRes image: Int,
        modifier: Modifier = Modifier,
        imageModifier: Modifier = Modifier,
        imageTintColor: Color? = null,
        selected: Boolean = false,
        imageAlignment: Alignment.Horizontal = Alignment.Start,
        imageSpace: Dp = 8.dp,
        onClick: (text: String) -> Unit
    ) = BottomSheetItem(
        text,
        imageAlignment,
        onClick,
        modifier,
        selected,
        imageSpace
    ) { horizontalPadding ->
        Image(
            painter = painterResource(image),
            contentDescription = text,
            colorFilter = if (imageTintColor != null) ColorFilter.tint(imageTintColor) else null,
            contentScale = ContentScale.Fit,
            modifier = imageModifier
                .padding(horizontal = horizontalPadding)
                .clip(RoundedCornerShape(20))
        )
    }


    @Composable
    fun ColumnScope.IconTextItem(
        text: String,
        icon: ImageVector,
        iconTintColor: Color,
        modifier: Modifier = Modifier,
        iconModifier: Modifier = Modifier,
        selected: Boolean = false,
        iconAlignment: Alignment.Horizontal = Alignment.Start,
        iconSpace: Dp = 8.dp,
        onClick: (text: String) -> Unit
    ) = BottomSheetItem(
        text,
        iconAlignment,
        onClick,
        modifier,
        selected,
        iconSpace
    ) { horizontalPadding ->
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = iconTintColor,
            modifier = iconModifier
                .padding(horizontal = horizontalPadding)
                .clip(CircleShape.copy(CornerSize(40)))
        )
    }


    @Composable
    fun ColumnScope.TextItem(
        text: String,
        modifier: Modifier = Modifier,
        selected: Boolean = false,
        onClick: (text: String) -> Unit
    ) = BottomSheetItem(
        text = text,
        imageAlignment = Alignment.Start,
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        image = {
            Spacer(modifier = Modifier.padding(horizontal = it + it))
        }
    )


    @Composable
    fun <S : AppScreens.NavigationDrawerScreens> ColumnScope.ScreenTypeItem(
        screen: S,
        modifier: Modifier = Modifier,
        selected: Boolean = false,
        iconTintColor: Color = MaterialTheme.colorScheme.primary,
        onClick: (screen: S) -> Unit
    ) {
        ScreenTypeItem(
            screen = screen,
            icon = screen.icon,
            modifier = modifier,
            selected = selected,
            iconTintColor = iconTintColor,
            onClick = onClick
        )
    }

    @Composable
    fun <S : AppScreens> ColumnScope.ScreenTypeItem(
        screen: S,
        modifier: Modifier = Modifier,
        icon: ImageVector? = null,
        iconTintColor: Color = MaterialTheme.colorScheme.secondary,
        selected: Boolean = false,
        onClick: (screen: S) -> Unit
    ) {
        when (icon) {
            null -> TextItem(
                text = screen.title(),
                selected = selected,
                modifier = modifier,
                onClick = { onClick(screen) }
            )

            else -> IconTextItem(
                text = screen.title(),
                icon = icon,
                iconTintColor = iconTintColor.animate(),
                modifier = modifier,
                iconModifier = Modifier.scale(1.17f),
                selected = selected,
                onClick = { onClick(screen) }
            )
        }
    }


    @Composable
    private fun ColumnScope.BottomSheetItem(
        text: String,
        imageAlignment: Alignment.Horizontal,
        onClick: (text: String) -> Unit,
        modifier: Modifier = Modifier,
        selected: Boolean = false,
        spaceAfterImage: Dp = 8.dp,
        image: @Composable (horizontalPadding: Dp) -> Unit
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .clip(MaterialTheme.shapes.small)
                .clickable { onClick(text) }
                .background(
                    brush = when {
                        selected -> Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondary,
                            ).map {
                                it
                                    .copy(alpha = .25f)
                                    .animate()
                            }
                        )

                        else -> SolidColor(Color.Transparent)
                    }
                )
                .then(modifier)
                .align(Alignment.Start)
                .padding(vertical = 16.dp)
                .fillMaxWidth(),
        ) {

            if (imageAlignment != Alignment.End) image(spaceAfterImage)
            else Spacer(modifier = Modifier.width(spaceAfterImage))

            Text(text = "")
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onBackground.animate(),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(2f)
            )

            if (imageAlignment == Alignment.End) image(spaceAfterImage)
        }
    }
}


object ModalSheetDefaults {
    val AnimationSpec = tween<Float>(
        durationMillis = 500,
        easing = FastOutSlowInEasing
    )

    val BottomSheetShape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
    val NavigationDrawerShape = RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp)
}


@Preview
@Composable
private fun ModalNavigationContentPreview() {
    CashbacksTheme(
        isDarkTheme = false
    ) {
        ModalNavigationDrawerContent {
            ScreenTypeItem(AppScreens.Categories, selected = true) {}
            ScreenTypeItem(AppScreens.Settings) {}
        }
    }
}