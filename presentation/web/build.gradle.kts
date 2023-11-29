plugins {
    id("kotlin-adapter-conventions")
    application
}

val http4kConnectVersion: String by project
val http4kVersion: String by project

dependencies {
    implementation(project(":infrastructure:example-adapter"))

    implementation(platform("org.http4k:http4k-bom:$http4kVersion"))

    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-cloudevents")
    implementation("org.http4k:http4k-cloudnative")
    implementation("org.http4k:http4k-contract")
    implementation("org.http4k:http4k-failsafe")
    implementation("org.http4k:http4k-client-okhttp")
    implementation("org.http4k:http4k-format-jackson")
    implementation("org.http4k:http4k-server-undertow")
    implementation("org.http4k:http4k-server-jetty")

    implementation(platform("org.http4k:http4k-connect-bom:$http4kConnectVersion"))
    implementation("org.http4k:http4k-connect-storage-redis")

    testImplementation("org.jsoup:jsoup:1.17.1")

    testImplementation("org.http4k:http4k-testing-chaos")
    testImplementation("org.http4k:http4k-testing-kotest")
    testImplementation("org.http4k:http4k-testing-strikt")
    testImplementation("org.http4k:http4k-testing-approval")
    testImplementation("org.http4k:http4k-testing-servirtium")
}

application {
    applicationName = "Web"
    mainClass.set("MainKt")
}
