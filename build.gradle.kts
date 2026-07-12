import org.gradle.api.GradleException
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.newrelic.android) apply false
    id("com.google.devtools.ksp") apply false
    id("com.google.dagger.hilt.android") apply false
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.isFile) {
        file.inputStream().use(::load)
    }
}

fun configuredValue(propertyName: String, environmentName: String): String? =
    providers.gradleProperty(propertyName).orNull
        ?: providers.environmentVariable(environmentName).orNull
        ?: localProperties.getProperty(propertyName)

fun String?.asEnabledFlag(): Boolean =
    when (this?.trim()?.lowercase()) {
        "true", "1", "yes", "y", "on" -> true
        else -> false
    }

val newRelicEnabled = configuredValue(
    propertyName = "newRelic.enabled",
    environmentName = "NEW_RELIC_ENABLED",
).asEnabledFlag()
val newRelicApplicationToken = configuredValue(
    propertyName = "newRelic.applicationToken",
    environmentName = "NEW_RELIC_APPLICATION_TOKEN",
).orEmpty()

if (newRelicEnabled && newRelicApplicationToken.isBlank()) {
    throw GradleException(
        "newRelic.enabled=true requires newRelic.applicationToken in local.properties, " +
            "-PnewRelic.applicationToken=..., or NEW_RELIC_APPLICATION_TOKEN.",
    )
}

extra["newRelicEnabled"] = newRelicEnabled
extra["newRelicApplicationToken"] = newRelicApplicationToken

subprojects {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    pluginManager.withPlugin("com.android.application") {
        configureNewRelicForAndroidModule()
    }
    pluginManager.withPlugin("com.android.library") {
        configureNewRelicForAndroidModule()
    }
}

fun Project.configureNewRelicForAndroidModule() {
    if (rootProject.extra["newRelicEnabled"] as Boolean) {
        pluginManager.apply("com.newrelic.agent.android")
    }
}
