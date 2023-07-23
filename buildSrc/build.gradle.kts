plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

val kotlinVersion: String by project
val detektVersion: String by project
val jvmVersion: String by project

java.toolchain.languageVersion.set(JavaLanguageVersion.of(jvmVersion))

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:$kotlinVersion"))

    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:$detektVersion")
}
