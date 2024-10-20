package com.cashbacks.app.ui.navigation

abstract class Feature<Args : FeatureArguments> {
    abstract val baseRoute: String
    open val arguments: Args? = null

    val destinationRoute: String get() = buildString {
        append(baseRoute)
        arguments?.toStringArray()
            ?.joinToString("/") { "{$it}" }
            ?.let { append("/", it) }
    }

    inline fun createUrl(
        argumentValues: Args.() -> Map<String, Any?> = { emptyMap() }
    ) = buildString {
        append(baseRoute)
        val values = arguments?.let(argumentValues)
        arguments?.toStringArray()
            ?.joinToString("/") { values?.get(it).toString() }
            ?.let { append("/", it) }
    }
}

interface FeatureArguments {
    fun toStringArray(): Array<String>
}