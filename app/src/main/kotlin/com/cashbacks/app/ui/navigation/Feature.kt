package com.cashbacks.app.ui.navigation

interface Feature {
    interface Args {
        fun toStringArray(): Array<String>
    }

    val baseRoute: String
    val args: Args? get() = null

    val destinationRoute: String get() = buildString {
        append(baseRoute)
        args?.toStringArray()
            ?.joinToString("/") { "{$it}" }
            ?.let { append("/", it) }
    }
}

