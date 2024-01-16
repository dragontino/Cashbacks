package com.cashbacks.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.cashbacks.app.util.animate

@Composable
fun SingleCashbackScreen() {
    var counter by remember { mutableIntStateOf(1) }

    Text(
        text = "Здесь будет информация о кэшбеке №$counter",
        modifier = Modifier
            .clickable { counter++ }
            .clip(MaterialTheme.shapes.large)
            .windowInsetsPadding(WindowInsets.statusBars)
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.onBackground.animate()
    )
}