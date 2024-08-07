plugins {
    id("kotlin-adapter-conventions")
    application
}

dependencies {
    implementation(project(":infrastructure:example-adapter"))
    implementation(project(":domain"))

    implementation(libs.bundles.http4k)
    implementation(libs.http4k.connect.redis)
    testImplementation(libs.pesticide)

    testImplementation(testFixtures(project(":domain")))

    testImplementation(libs.jsoup)
    testImplementation(libs.bundles.http4k.testing)
    testImplementation(libs.http4k.helidon)
    testImplementation(libs.http4k.apache)
}

application {
    applicationName = "Web"
    mainClass.set("MainKt")
}
