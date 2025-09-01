import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.cashbacks.features.share.data"
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
        }
    }
}

dependencies {
    implementation(project(":features:share:domain"))
    implementation(project(":core:database"))
    implementation(project(":common:utils"))
    implementation(libs.androidx.core)
    implementation(libs.koin.android)
    implementation(libs.opencsv)
    implementation(libs.kotlinx.datetime)

    implementation(libs.room.runtime)

    testImplementation(libs.junit)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.espresso)
}