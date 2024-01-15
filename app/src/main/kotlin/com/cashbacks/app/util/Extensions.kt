package com.cashbacks.app.util

import android.os.Bundle

internal fun Bundle?.getString(key: String, defaultValue: String = "") =
    this?.getString(key)?.takeIf { it != "null" } ?: defaultValue