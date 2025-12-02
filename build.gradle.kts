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

    id("com.github.jk1.dependency-license-report") version "3.0.1"
    id("com.autonomousapps.dependency-analysis") version "3.5.1"
}

dependencies {
    testReportAggregation(projects.domain)
    testReportAggregation(projects.infrastructure.eventStore)
    testReportAggregation(projects.presentation.web)
    jacocoAggregation(projects.domain)
    jacocoAggregation(projects.infrastructure.eventStore)
    jacocoAggregation(projects.presentation.web)
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

//val sha by providers.of("git rev-parse --short HEAD".runCommand()).map { it.trim() }
//tasks.register("printSha") { doLast { println(sha.get()) } }
