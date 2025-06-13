plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.cashbacks.features.shop.presentation.impl"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":common:composables"))
    implementation(project(":common:resources"))
    implementation(project(":common:utils"))
    implementation(project(":common:navigation"))

    implementation(project(":features:category:domain"))
    implementation(project(":features:category:presentation:api"))

    implementation(project(":features:shop:domain"))
    implementation(project(":features:shop:presentation:api"))

    implementation(project(":features:cashback:domain"))
    implementation(project(":features:cashback:presentation:api"))

    implementation(libs.androidx.core)
    implementation(libs.kotlinx.serialization.json)

    // DI
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.navigation)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.activity)
    implementation(libs.compose.navigation)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.icons.core)
    implementation(libs.compose.icons.extended)
    implementation(libs.compose.material3)

    // MVI
    implementation(libs.mvikotlin.main)
    implementation(libs.mvikotlin.android)
    implementation(libs.mvikotlin.coroutines)
    implementation(libs.mvikotlin.logging)
    implementation(libs.mvikotlin.timetravel)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.junit4)
    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.espresso)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}