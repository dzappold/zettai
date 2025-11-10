plugins {
    id("kotlin-adapter-conventions")
    application
}

dependencies {
    implementation(projects.infrastructure.eventStore)
    implementation(projects.domain)

    implementation(libs.bundles.http4k)
    implementation(libs.http4k.connect.storage.redis)

    constraints {
        implementation(libs.exposed.json)
    }

    testImplementation(libs.pesticide)

    testImplementation(testFixtures(projects.domain))

    testImplementation(libs.jsoup)
    testImplementation(libs.bundles.http4k.testing)
    testImplementation(libs.http4k.client.helidon)
    testImplementation(libs.http4k.client.apache)
}

application {
    applicationName = "Zettai"
    mainClass.set("MainKt")
}
