enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    // Reuse version catalog from the main build.
    versionCatalogs {
        create("libs", { from(files("../gradle/libs.versions.toml")) })
    }
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

rootProject.name = "build-logic"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

plugins {
    val foojayVersion: String by settings

    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    //id("dev.panuszewski.typesafe-conventions") version "0.5.1"
}

