import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinx.serialization)
}

android {
    namespace = "com.cashbacks.features.settings.presentation"
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

    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
            freeCompilerArgs.addAll(listOf("-P", "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" + rootProject.projectDir.absolutePath + "/compose_metrics/"))
            freeCompilerArgs.addAll (listOf("-P", "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" + rootProject.projectDir.absolutePath + "/compose_metrics/"))
        }
    }
}

dependencies {
    implementation(project(":common:composables"))
    implementation(project(":common:utils"))
    implementation(project(":common:resources"))
    implementation(project(":common:navigation"))

    implementation(project(":features:settings:domain"))

    implementation(libs.androidx.core)

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

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.junit4)
    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.espresso)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}