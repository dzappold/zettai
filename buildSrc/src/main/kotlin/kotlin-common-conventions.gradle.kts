import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED

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

val forkhandlesVersion: String by project
val junitVersion: String by project
val kotestVersion: String by project
val kotlinVersion: String by project
val kurePotlinVersion: String by project
val mockkVersion: String by project
val pesticideVersion: String by project

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:$kotlinVersion"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation(platform("dev.forkhandles:forkhandles-bom:$forkhandlesVersion"))
    implementation("dev.forkhandles:result4k")
    testImplementation("dev.forkhandles:result4k-kotest")

    testImplementation(platform("org.junit:junit-bom:$junitVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    testImplementation("io.mockk:mockk-jvm:$mockkVersion") {
        exclude("junit", "junit")
    }

    testImplementation(platform("io.kotest:kotest-bom:$kotestVersion"))
    testImplementation("io.kotest:kotest-assertions-core")
    testImplementation("io.kotest:kotest-assertions-json")
    testImplementation("com.ubertob.pesticide:pesticide-core:$pesticideVersion")

    detektPlugins("pl.setblack:kure-potlin:$kurePotlinVersion")
}

tasks {
    withType<Test>().configureEach {
        useJUnitPlatform()
        testLogging {
            events(PASSED, SKIPPED, FAILED)
        }
        outputs.upToDateWhen { false }
        finalizedBy(jacocoTestReport) // report is always generated after tests run
    }

    jacocoTestReport {
        dependsOn(test) // tests are required to run before generating the report
    }

    jacocoTestCoverageVerification {
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


//    withType<KotlinCompile> {
//        compilerOptions {
//            freeCompilerArgs.set(
//                listOf(
//                    "-Xcontext-receivers",
//                    "-Xjsr305=strict"
//                )
//            )
//            jvmTarget.set(JVM_17)
//            languageVersion.set(KOTLIN_2_0)
//        }
//    }
}


detekt {
    ignoreFailures = true
}

tasks {
    withType<Detekt> {
        // Target version of the generated JVM bytecode. It is used for type resolution.
        jvmTarget = java.toolchain.languageVersion.get().toString()
    }
}
