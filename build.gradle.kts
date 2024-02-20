import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

// TODO: test and coverage aggregation
// TODO: license check
plugins {
    alias(libs.plugins.ben.manes)
    alias(libs.plugins.dependency.check)
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
