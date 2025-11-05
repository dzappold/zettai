import org.gradle.api.initialization.resolve.RepositoriesMode.FAIL_ON_PROJECT_REPOS

rootProject.name = "zettai"

include("domain")
"presentation".apply {
    includeModule("web")
//    includeModule("acceptance-test")
}

"infrastructure".apply {
    includeModule("event-store")
}

fun String.includeModule(name: String) {
    val projectName = "$this:$name"
    include(":$projectName")
    project(":$projectName").projectDir = File("$this/${name.replace(':', '/')}")
}

pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }

    val kotlinVersion: String by settings
    val detektVersion: String by settings
    val foojayVersion: String by settings

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "org.jetbrains.kotlin.jvm" -> useVersion(kotlinVersion)
                "io.gitlab.arturbosch.detekt" -> useVersion(detektVersion)
                "org.gradle.toolchains.foojay-resolver-convention" -> useVersion(foojayVersion)
            }
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention")
}

dependencyResolutionManagement {
    repositoriesMode.set(FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
    }
}
