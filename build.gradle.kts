import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.github.jk1.license.filter.DependencyFilter
import com.github.jk1.license.filter.LicenseBundleNormalizer
import com.github.jk1.license.render.InventoryHtmlReportRenderer
import com.github.jk1.license.render.ReportRenderer

// TODO: test and coverage aggregation
// TODO: license check
plugins {
    `test-report-aggregation`
    `jacoco-report-aggregation`
    alias(libs.plugins.ben.manes)
    alias(libs.plugins.dependency.check)

    id("com.github.jk1.dependency-license-report") version "2.9"
    id("com.autonomousapps.dependency-analysis") version "2.1.4"
}

dependencies {
    testReportAggregation(project(":domain"))
    jacocoAggregation(project(":domain"))
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

licenseReport {
    renderers = arrayOf<ReportRenderer>(InventoryHtmlReportRenderer("report.html", "Backend"))
    filters = arrayOf<DependencyFilter>(LicenseBundleNormalizer())
    allowedLicensesFile = file("$projectDir/config/allowed-licenses.json")
}
