package com.cashbacks.features.home.impl.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.tappableElement
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cashbacks.common.composables.ModalSheetDefaults
import com.cashbacks.common.composables.utils.animate
import com.cashbacks.common.resources.AppInfo
import com.cashbacks.features.home.impl.R

@Composable
internal fun ModalNavigationDrawerContent(
    appInfo: AppInfo,
    items: @Composable ColumnScope.() -> Unit
) {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current

    ModalDrawerSheet(
        drawerShape = ModalSheetDefaults.NavigationDrawerShape,
        drawerContainerColor = MaterialTheme.colorScheme.background.animate(),
        drawerContentColor = MaterialTheme.colorScheme.onBackground.animate(),
        windowInsets = WindowInsets.tappableElement.only(WindowInsetsSides.Bottom),
        modifier = Modifier
            .padding(end = 80.dp)
            .fillMaxHeight()
            .width(
                width = with(density) {
                    minOf(
                        windowInfo.containerSize.width.toDp(),
                        windowInfo.containerSize.height.toDp()
                    )
                }
            )
            .verticalScroll(rememberScrollState())
    ) {
        NavHeader(
            appInfo = appInfo,
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
    appInfo: AppInfo,
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
            text = appInfo.name,
            color = MaterialTheme.colorScheme.onPrimary.animate(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = appInfo.version,
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