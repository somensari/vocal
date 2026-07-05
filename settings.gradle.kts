pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
    // KSP and Hilt must resolve from the same classloader (google/dagger#3965).
    // Versions must stay aligned with gradle/libs.versions.toml.
    plugins {
        id("com.google.devtools.ksp") version "2.3.4"
        id("com.google.dagger.hilt.android") version "2.59.2"
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Vocal"

include(":app")
include(":core:ui")
include(":core:domain")
include(":core:data")
include(":feature:board")
include(":feature:settings")
