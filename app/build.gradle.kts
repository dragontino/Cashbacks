import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.detekt)
}



val versionMajor: Int by rootProject.extra
val versionMinor: Int by rootProject.extra
val versionPatch: Int by rootProject.extra
val minSdkVersion: Int by rootProject.extra

val localPropsPath by lazy { rootDir.resolve("local.properties") }

val properties: Properties by lazy {
    Properties().apply {
        localPropsPath.inputStream().use { load(it) }
    }
}

val ciBuildNumber: Int? by lazy {
    properties.getProperty("ci.build.number")?.toIntOrNull()
}
val buildNumber: Int by lazy {
    ciBuildNumber ?: (properties.getProperty("build.number").toInt() + 1)
}

fun generateVersionCode(): Int {
    val versionCode = minSdkVersion * 1_000_000 + versionMajor * 10_000 + versionMinor * 100 + versionPatch
    return versionCode
}
fun generateVersionName(): String {
    return "$versionMajor.$versionMinor.$versionPatch"
}



android {
    val compileSdkVersion: Int by rootProject.extra

    namespace = "com.cashbacks.app"
    compileSdk = compileSdkVersion

    defaultConfig {
        applicationId = "com.cashbacks.app"
        minSdk = minSdkVersion
        targetSdk = rootProject.extra["targetSdkVersion"] as Int

        versionName = generateVersionName()
        versionCode = generateVersionCode()

        project.base.archivesName = "Cashbacks $versionName"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField(
            type = "String",
            name = "BUILD_NUMBER",
            value = buildNumber.toString().padQuotes()
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            buildConfigField(
                type = "String",
                name = "VERSION_DATE",
                value = properties.getProperty("app.version.date").padQuotes()
            )
        }

        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = " build $buildNumber"

            val versionDate = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            buildConfigField(
                type = "String",
                name = "VERSION_DATE",
                value = versionDate.format(formatter).padQuotes()
            )
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

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}


detekt {
    buildUponDefaultConfig = true
    allRules = false
    baseline = file("$projectDir/config/detekt/baseline.xml")
}


fun setLocalProperty(name: String, value: Any) {
    properties.setProperty(name, value.toString())
    localPropsPath.outputStream().use {
        properties.store(it, null)
    }
}

fun String.padQuotes(): String = "\"$this\""


tasks.register("incrementLocalBuildNumber") {
    if (ciBuildNumber == null) {
        val currentBuildNumber = properties.getProperty("build.number").toInt()
        val nextBuildNumber = currentBuildNumber + 1
        setLocalProperty("build.number", nextBuildNumber)
        println("build.number has been updated: $currentBuildNumber -> $nextBuildNumber")
    }
}

tasks.whenTaskAdded {
    if (name == "assembleDebug") {
        dependsOn("incrementLocalBuildNumber")
    }
}


tasks.register("resetLocalBuildNumber") {
    doLast {
        setLocalProperty("build.number", 0)
        println("build.number reset to 0")
    }
}


tasks.withType<Detekt>().configureEach {
    reports {
        html.required = true
        md.required = true
    }
}


tasks.withType<Detekt>().configureEach {
    jvmTarget = "17"
}

tasks.withType<DetektCreateBaselineTask>().configureEach {
    jvmTarget = "17"
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
    api(project(":core:network"))

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
    androidTestImplementation(libs.compose.junit4)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
    debugImplementation(libs.squareup.leakcanary)


    // Tests
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.espresso)
    androidTestImplementation(libs.espresso.intents)
    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.androidx.uiautomator)


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

    detektPlugins(libs.detekt.formatting)
}