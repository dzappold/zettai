import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    `java-test-fixtures`
    jacoco

    kotlin("jvm")

    id("io.gitlab.arturbosch.detekt")
}

val jvmVersion: String by project

java.toolchain.languageVersion.set(JavaLanguageVersion.of(jvmVersion))
kotlin.jvmToolchain(jvmVersion.toInt())

val libs = the<LibrariesForLibs>()

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.result4k)

    testImplementation(libs.bundles.junit)
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.result4k.kotest)
    testImplementation(libs.mockk)
    testImplementation(libs.pesticide)
}

configurations.all {
    exclude(group = "junit", module = "junit")
}

//testing {
//    suites {
//        val test by getting(JvmTestSuite::class) {
//
//        }
//    }
//}

tasks {
    withType<Test>().configureEach {
        useJUnitPlatform()
        testLogging {
            events(
//                PASSED,
                SKIPPED,
                FAILED
            )
        }
        finalizedBy(jacocoTestReport)
    }

    withType<JacocoReport>().configureEach {
        dependsOn(test) // tests are required to run before generating the report
    }

    withType<JacocoCoverageVerification>().configureEach {
        violationRules {
            rule {
                enabled = true

                limit {
                    counter = "LINE"
                    value = "TOTALCOUNT"
                    minimum = "1.0".toBigDecimal()
                }
            }
        }
    }


    withType<KotlinCompile> {
        compilerOptions {
            freeCompilerArgs.set(
                listOf(
                    "-Xcontext-receivers",
                    "-Xconsistent-data-class-copy-visibility",
                    "-Xjsr305=strict"
                )
            )
            jvmTarget.set(JVM_21)
            languageVersion.set(KotlinVersion.KOTLIN_2_3)
        }
    }
}


detekt {
    ignoreFailures = true
}

tasks {
    withType<Detekt>().configureEach {
        // Target version of the generated JVM bytecode. It is used for type resolution.
        jvmTarget = java.toolchain.languageVersion.get().toString()
    }
}
