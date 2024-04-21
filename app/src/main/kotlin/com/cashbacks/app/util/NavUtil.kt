package com.cashbacks.app.util

import android.os.Build
import android.os.Bundle
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.cashbacks.app.ui.navigation.FeatureApi

fun NavGraphBuilder.register(
    feature: FeatureApi,
    navController: NavHostController,
    modifier: Modifier
) {
    feature.registerGraph(
        navGraphBuilder = this,
        navController = navController,
        modifier = modifier
    )
}

@Suppress("DEPRECATION")
inline fun <reified D : Enum<*>> Bundle?.getEnum(key: String, defaultValue: D): D {
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
            this?.getSerializable(key, D::class.java) ?: defaultValue
        }

        else -> (this?.getSerializable(key) as D?) ?: defaultValue
    }
}