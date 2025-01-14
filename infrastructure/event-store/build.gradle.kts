plugins {
    id("kotlin-adapter-conventions")
}

dependencies {
    implementation(project(":domain"))

    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.json)
    implementation(libs.exposed.java.time)
    implementation(libs.postgresql)
    implementation(libs.klaxon)

    testImplementation(testFixtures(project(":domain")))
}
