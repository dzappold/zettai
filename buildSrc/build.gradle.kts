plugins {
    `kotlin-dsl`
}

val kotlinVersion: String by project
val detektVersion: String by project
val jvmVersion: String by project

java.toolchain.languageVersion.set(JavaLanguageVersion.of(jvmVersion))

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:$kotlinVersion"))

    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:$detektVersion")

    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
//        because("https://stackoverflow.com/questions/67795324/gradle7-version-catalog-how-to-use-it-with-buildsrc")
}
