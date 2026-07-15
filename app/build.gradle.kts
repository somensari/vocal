plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

fun String.toBuildConfigStringLiteral(): String =
    "\"${replace("\\", "\\\\").replace("\"", "\\\"")}\""

val newRelicEnabled = rootProject.extra["newRelicEnabled"] as Boolean
val newRelicApplicationToken = rootProject.extra["newRelicApplicationToken"] as String

android {
    namespace = "org.openaac.vocal"
    compileSdk = 35

    defaultConfig {
        applicationId = "org.openaac.vocal"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("Boolean", "NEW_RELIC_ENABLED", newRelicEnabled.toString())
        buildConfigField(
            "String",
            "NEW_RELIC_APPLICATION_TOKEN",
            newRelicApplicationToken.toBuildConfigStringLiteral(),
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }
}

// Plugin is applied from the root project only when New Relic is enabled.
// Never upload ProGuard maps unless a token is configured for this build.
// Use the typed extension API so this compiles when the plugin is on the
// classpath via `apply false` but not applied to this module.
pluginManager.withPlugin("com.newrelic.agent.android") {
    extensions.configure<com.newrelic.agent.android.NewRelicExtension>("newrelic") {
        uploadMapsForVariant(if (newRelicEnabled) "Release" else "")
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:ui"))
    implementation(project(":core:data"))
    implementation(project(":feature:board"))
    implementation(project(":feature:settings"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.newrelic.android.agent)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
