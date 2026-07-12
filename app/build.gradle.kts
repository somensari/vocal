import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.newrelic.android)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

/**
 * New Relic enablement is build-config only (no in-app UI).
 *
 * Precedence (first non-blank wins for each key):
 * 1. Gradle property (`-Pnewrelic.enabled` / `-Pnewrelic.token`)
 * 2. Environment (`NEW_RELIC_ENABLED` / `NEW_RELIC_TOKEN`) — CI secrets
 * 3. `local.properties` (`newrelic.enabled` / `newrelic.token`)
 *
 * Agent starts only when enabled=true **and** token is non-blank.
 * Default CI / local builds leave both unset → agent stays off (no token required).
 */
fun localProperty(key: String): String? {
    val file = rootProject.file("local.properties")
    if (!file.exists()) return null
    val props = Properties()
    file.inputStream().use { props.load(it) }
    return props.getProperty(key)?.trim()?.takeIf { it.isNotEmpty() }
}

fun configValue(gradleKey: String, envKey: String, localKey: String): String? =
    providers.gradleProperty(gradleKey).orNull?.trim()?.takeIf { it.isNotEmpty() }
        ?: System.getenv(envKey)?.trim()?.takeIf { it.isNotEmpty() }
        ?: localProperty(localKey)

val newRelicEnabledFlag = configValue("newrelic.enabled", "NEW_RELIC_ENABLED", "newrelic.enabled")
    ?.equals("true", ignoreCase = true) == true
val newRelicToken = configValue("newrelic.token", "NEW_RELIC_TOKEN", "newrelic.token").orEmpty()
val newRelicActive = newRelicEnabledFlag && newRelicToken.isNotBlank()

fun String.asBuildConfigString(): String =
    "\"${replace("\\", "\\\\").replace("\"", "\\\"")}\""

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

        buildConfigField("boolean", "NEW_RELIC_ENABLED", newRelicActive.toString())
        buildConfigField("String", "NEW_RELIC_TOKEN", newRelicToken.asBuildConfigString())
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
        compose = true
        buildConfig = true
    }
}

// Never upload ProGuard maps unless a token is configured for this build.
newrelic {
    uploadMapsForVariant(if (newRelicActive) "Release" else "")
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

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.newrelic.android.agent)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
