import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlinx.serialization)
}

android {
    namespace = "com.cashbacks.core.network"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        getLocalProperty("app.repos.url")?.let {
            buildConfigField(
                type = "String",
                name = "APP_REPOS_URL",
                value = "\"$it\""
            )
        }
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

    buildFeatures {
        buildConfig = true
    }
}


fun getLocalProperty(name: String): String? {
    val propertiesFile = rootProject.file("local.properties")
    if (propertiesFile.exists().not()) return null

    val properties = Properties()
    propertiesFile.inputStream().use { properties.load(it) }
    return properties.getProperty(name, null)
}


dependencies {
    implementation(libs.androidx.core)

    implementation(libs.kotlinx.serialization.json)

    // Retrofit
    implementation(libs.squareup.retrofit)
    implementation(libs.squareup.retrofit.kotlinx.serialization.converter)

    // DI
    implementation(libs.koin.android)

    testImplementation(libs.junit)
    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.espresso)
}