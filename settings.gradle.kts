import org.gradle.api.initialization.resolve.RepositoriesMode.FAIL_ON_PROJECT_REPOS

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

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
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
    }
}
