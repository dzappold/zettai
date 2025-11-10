plugins {
    `kotlin-dsl`
}

val jvmVersion: String by project

java.toolchain.languageVersion.set(JavaLanguageVersion.of(jvmVersion))

dependencies {
    implementation(platform(libs.kotlin.bom))

    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.detekt.gradle.plugin)

    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
//        because("https://stackoverflow.com/questions/67795324/gradle7-version-catalog-how-to-use-it-with-buildsrc")
}
