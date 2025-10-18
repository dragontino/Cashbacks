package com.cashbacks.common.composables

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat


@Composable
fun AppLaunchPermissionsDialog(
    vararg permissions: String,
    onPermissionDenied: (permission: String) -> Unit
) {
    val context = LocalContext.current
    permissions.forEach {
        if (ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED) {
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (!isGranted) onPermissionDenied(it)
            }

            SideEffect { launcher.launch(it) }
        }
    }
}