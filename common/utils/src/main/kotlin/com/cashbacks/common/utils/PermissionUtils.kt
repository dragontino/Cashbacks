package com.cashbacks.common.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

inline fun usePermissions(
    vararg permissions: String,
    context: Context,
    grantPermission: (permission: String) -> Unit,
    onAllPermissionsGranted: () -> Unit
) {
    val isAllPermissionsGranted = permissions.all { permission ->
        val checkPermission = ContextCompat.checkSelfPermission(context, permission)
        if (checkPermission == PackageManager.PERMISSION_DENIED) {
            grantPermission(permission)
        }
        return@all checkPermission == PackageManager.PERMISSION_GRANTED
    }

    if (isAllPermissionsGranted) {
        onAllPermissionsGranted()
    }
}