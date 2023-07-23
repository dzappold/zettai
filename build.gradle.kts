import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

// TODO: test and coverage aggregation
plugins {
    id("com.github.ben-manes.versions")
    id("org.owasp.dependencycheck")
}

tasks {
    withType<DependencyUpdatesTask> {
        rejectVersionIf {
            this.candidate.version.contains("alpha", ignoreCase = true) ||
                    this.candidate.version.contains("beta", ignoreCase = true) ||
                    this.candidate.version.contains("rc", ignoreCase = true) ||
                    this.candidate.version.contains("m", ignoreCase = true)
        }

        // optional parameters
        checkForGradleUpdate = true
        outputFormatter = "json"
        outputDir = "build/dependencyUpdates"
        reportfileName = "report"
    }
}
