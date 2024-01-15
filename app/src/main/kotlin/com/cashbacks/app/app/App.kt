package com.cashbacks.app.app

import android.app.Application
import com.cashbacks.app.di.DependencyFactory
import com.cashbacks.app.viewmodel.ViewModelFactory

class App : Application() {
    val dependencyFactory by lazy { DependencyFactory(this) }
    val viewModelFactory by lazy { ViewModelFactory(this) }
}