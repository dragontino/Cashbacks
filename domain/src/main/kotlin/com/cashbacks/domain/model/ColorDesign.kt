package com.cashbacks.domain.model

import android.content.res.Resources
import com.cashbacks.domain.R

enum class ColorDesign {
    Light {
        override fun getTitle(resources: Resources) = resources.getString(R.string.light_scheme)
    },
    Dark {
        override fun getTitle(resources: Resources) = resources.getString(R.string.dark_scheme)
    },
    System {
        override fun getTitle(resources: Resources) = resources.getString(R.string.system_theme)
    };

    abstract fun getTitle(resources: Resources): String
}