import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.cashbacks.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.cashbacks.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = 36
        versionCode = libs.versions.app.android.get().split(".")[0].toInt()
        versionName = libs.versions.app.android.get()

        project.base.archivesName = "Cashbacks-$versionName"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        getLocalProperty("version.date")?.let {
            buildConfigField(
                type = "String",
                name = "VERSION_DATE",
                value = "\"$it\""
            )
        }

        getLocalProperty("appversion.url")?.let {
            buildConfigField(
                type = "String",
                name = "VERSION_URL",
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

        debug {
            applicationIdSuffix = ".debug"
            getLocalProperty("debug.version.suffix")?.let {
                versionNameSuffix = "-$it"
            }
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
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
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}


fun getLocalProperty(name: String): String? {
    val propertiesFile = rootProject.file("local.properties")
    if (propertiesFile.exists().not()) return null

    val properties = Properties()
    propertiesFile.inputStream().use { properties.load(it) }
    return properties.getProperty(name, null)
}


composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
    metricsDestination = layout.buildDirectory.dir("compose_compiler")
}


dependencies {
    api(project(":common:composables"))
    api(project(":common:utils"))
    api(project(":common:resources"))
    api(project(":common:navigation"))
    api(project(":core:database"))

    api(project(":features:settings:domain"))
    api(project(":features:settings:data"))
    api(project(":features:settings:presentation"))

    api(project(":features:bankcard:domain"))
    api(project(":features:bankcard:data"))
    api(project(":features:bankcard:presentation:api"))
    api(project(":features:bankcard:presentation:impl"))

    api(project(":features:cashback:domain"))
    api(project(":features:cashback:data"))
    api(project(":features:cashback:presentation:api"))
    api(project(":features:cashback:presentation:impl"))

    api(project(":features:shop:domain"))
    api(project(":features:shop:data"))
    api(project(":features:shop:presentation:api"))
    api(project(":features:shop:presentation:impl"))

    api(project(":features:category:domain"))
    api(project(":features:category:data"))
    api(project(":features:category:presentation:api"))
    api(project(":features:category:presentation:impl"))

    api(project(":features:home:api"))
    api(project(":features:home:impl"))

    api(project(":features:share:domain"))
    api(project(":features:share:data"))


    coreLibraryDesugaring(libs.tools.desugaring)

    implementation(libs.androidx.core)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.kotlinx.datetime)

    // WorkManager
    implementation(libs.androidx.work.runtime)
    androidTestImplementation(libs.androidx.work.testing)

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
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.junit4)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
    debugImplementation(libs.squareup.leakcanary)


    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso)
    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.room.testing)


    // Koin
    implementation(libs.koin.compose)
    androidTestImplementation(libs.koin.test)
    androidTestImplementation(libs.koin.test.junit4)

    // MVI
    implementation(libs.mvikotlin.main)
    implementation(libs.mvikotlin.android)
    implementation(libs.mvikotlin.coroutines)
    implementation(libs.mvikotlin.logging)
    implementation(libs.mvikotlin.timetravel)
}