plugins {
    id("kotlin-adapter-conventions")
}

dependencies {
    implementation(projects.domain)

    implementation(libs.bundles.exposed)
    implementation(libs.postgresql)
    implementation(libs.klaxon)

    testImplementation(testFixtures(projects.domain))
}
