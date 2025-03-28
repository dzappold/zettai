#!/usr/bin/env kotlin

@file:Repository("https://repo.maven.apache.org/maven2/")
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:3.3.0")

@file:Repository("https://bindings.krzeminski.it")
@file:DependsOn("actions:checkout:v4")
@file:DependsOn("actions:setup-java:v4")
@file:DependsOn("gradle:gradle-build-action:v3")
@file:DependsOn("dorny:test-reporter:v1")

import io.github.typesafegithub.workflows.actions.actions.Checkout
import io.github.typesafegithub.workflows.actions.actions.SetupJava
import io.github.typesafegithub.workflows.actions.actions.SetupJava.Distribution.Temurin
import io.github.typesafegithub.workflows.domain.RunnerType.UbuntuLatest
import io.github.typesafegithub.workflows.domain.triggers.PullRequest
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.dsl.workflow

val javaVersion = "21"
val testResultsPath = "**/build/test-results/test/TEST-*.xml" // Standard JUnit XML path

workflow(
    name = "Build and Test",
    on = listOf(
        Push(branches = listOf("main")),
        PullRequest()
    ),
    sourceFile = __FILE__,
    env = mapOf(
        "GRADLE_OPTS" to "-Dorg.gradle.daemon=false"
    )
) {
    job(
        id = "build",
        name = "Build, Test and Report",
        runsOn = UbuntuLatest
    ) {
        uses(
            name = "Checkout code",
            action = Checkout()
        )

        uses(
            name = "Set up JDK $javaVersion",
            action = SetupJava(
                distribution = Temurin,
                javaVersion = javaVersion,
                cache = SetupJava.BuildPlatform.Gradle
            )
        )

        run(
            name = "Run Gradle build",
            command = "./gradlew build"
        )
    }
}
