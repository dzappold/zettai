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

rootProject.name = "buildSrc"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }

    val kotlinVersion: String by settings
    val benManesVersion: String by settings
    val detektVersion: String by settings
    val dependencyCheckVersion: String by settings
    val foojayVersion: String by settings

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "org.jetbrains.kotlin.jvm" -> useVersion(kotlinVersion)
                "com.github.ben-manes.versions" -> useVersion(benManesVersion)
                "io.gitlab.arturbosch.detekt" -> useVersion(detektVersion)
                "org.owasp.dependencycheck" -> useVersion(dependencyCheckVersion)
                "org.gradle.toolchains.foojay-resolver-convention" -> useVersion(foojayVersion)
            }
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention")
}

