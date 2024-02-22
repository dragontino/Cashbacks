package com.cashbacks.app.ui.composables

import android.content.res.Configuration
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cashbacks.app.R
import com.cashbacks.app.ui.theme.DarkerGray
import com.cashbacks.app.util.animate


data class Header(
    val title: String = "",
    val subtitle: String = "",
    val beautifulDesign: Boolean = false
) {
    fun isEmpty(): Boolean = title.isBlank()
}


@Composable
internal fun ModalNavigationDrawerContent(
    appName: String,
    appVersion: String,
    items: @Composable ColumnScope.() -> Unit
) {
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
            appName = appName,
            appVersion = appVersion,
            modifier = Modifier.windowInsetsPadding(
                WindowInsets.statusBars.only(WindowInsetsSides.Top)
            )
        )
        Spacer(modifier = Modifier.size(16.dp))
        items()
    }
}


@Composable
private fun NavHeader(
    appName: String,
    appVersion: String,
    modifier: Modifier = Modifier
) {
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

        Spacer(modifier = Modifier.height(15.dp))
        Text(
            text = appName,
            color = MaterialTheme.colorScheme.onPrimary.animate(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = appVersion,
            style = MaterialTheme.typography.bodySmall.copy(
                shadow = Shadow(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    blurRadius = 30f
                )
            ),
            color = MaterialTheme.colorScheme.onPrimary.animate().copy(alpha = .85f),
        )
    }
}


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
            .windowInsetsPadding(contentWindowInsets.only(WindowInsetsSides.Vertical))
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
            BottomSheetHeader(
                header = header,
                modifier = Modifier.padding(top = topInsetsPadding, bottom = 8.dp)
            )
        }
        bodyContent()
    }
}


@Composable
private fun ColumnScope.BottomSheetHeader(
    header: Header,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .then(modifier)
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
    fun ColumnScope.IconTextItem(
        text: String,
        icon: ImageVector,
        iconTintColor: Color,
        modifier: Modifier = Modifier,
        iconModifier: Modifier = Modifier,
        selected: Boolean = false,
        iconAlignment: Alignment.Horizontal = Alignment.Start,
        onClick: (String) -> Unit
    ) {
        val iconContent = @Composable {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = iconTintColor,
                modifier = iconModifier.clip(RoundedCornerShape(40))
            )
        }

        BottomSheetItem(
            text = text,
            onClick = onClick,
            leadingIcon = {
                if (iconAlignment == Alignment.Start) {
                    iconContent()
                }
            },
            trailingIcon = {
                if (iconAlignment == Alignment.End) {
                    iconContent()
                }
            },
            selected = selected,
            modifier = modifier,
        )
    }


    @Composable
    private fun ColumnScope.BottomSheetItem(
        text: String,
        onClick: (text: String) -> Unit,
        modifier: Modifier = Modifier,
        selected: Boolean = false,
        leadingIcon: @Composable (() -> Unit)? = null,
        trailingIcon: @Composable (() -> Unit)? = null
    ) {
        NavigationDrawerItem(
            label = {
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.onBackground.animate(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(2f)
                )
            },
            icon = leadingIcon,
            badge = trailingIcon,
            selected = selected,
            onClick = { onClick(text) },
            shape = MaterialTheme.shapes.small,
            colors = NavigationDrawerItemDefaults.colors(
                selectedContainerColor = Color.Transparent,
                unselectedContainerColor = Color.Transparent
            ),
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .background(
                    brush = when {
                        selected -> Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondary,
                            )
                        )

                        else -> SolidColor(Color.Transparent)
                    },
                    shape = MaterialTheme.shapes.small,
                    alpha = .25f
                )
                .then(modifier)
                .align(Alignment.Start)
                .fillMaxWidth()
        )
    }
}


object ModalSheetDefaults {
    val BottomSheetShape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
    val NavigationDrawerShape = RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp)

    val drawerAnimationSpec = tween<Float>(
        durationMillis = 600,
        easing = FastOutSlowInEasing
    )
}