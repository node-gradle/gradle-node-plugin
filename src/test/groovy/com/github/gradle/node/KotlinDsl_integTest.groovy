package com.github.gradle.node

import com.github.gradle.AbstractIntegTest
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.util.GradleVersion
import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables

import java.util.zip.ZipFile

class KotlinDsl_integTest extends AbstractIntegTest {
    @Rule
    EnvironmentVariables environmentVariables = new EnvironmentVariables()

    def 'build project using Kotlin DSL'() {
        given:
        copyResources("fixtures/kotlin/")
        copyResources("fixtures/javascript-project/")

        when:
        def result1 = buildWithConfigurationCacheIfAvailable("run")

        then:
        result1.task(":nodeSetup").outcome == TaskOutcome.SKIPPED
        result1.task(":npmSetup").outcome == TaskOutcome.SKIPPED
        result1.task(":yarnSetup").outcome == TaskOutcome.SUCCESS
        result1.task(":npmInstall").outcome == TaskOutcome.SUCCESS
        result1.task(":testNpx").outcome == TaskOutcome.SUCCESS
        result1.task(":testNpm").outcome == TaskOutcome.SUCCESS
        result1.task(":testYarn").outcome == TaskOutcome.SUCCESS
        result1.task(":run").outcome == TaskOutcome.SUCCESS
        result1.output.contains("Hello Bobby!")
        // Ensure tests were executed 3 times
        result1.output.split("1 passing").length == 4

        when:
        def result2 = buildWithConfigurationCacheIfAvailable("package")

        then:
        result2.task(":nodeSetup").outcome == TaskOutcome.SKIPPED
        result2.task(":npmSetup").outcome == TaskOutcome.SKIPPED
        result2.task(":npmInstall").outcome == TaskOutcome.SUCCESS
        result2.task(":buildNpx").outcome == TaskOutcome.SUCCESS
        result2.task(":buildNpm").outcome == TaskOutcome.SUCCESS
        result2.task(":buildYarn").outcome == TaskOutcome.SUCCESS
        result2.task(":package").outcome == TaskOutcome.SUCCESS
        def outputFile = createFile("build/app.zip")
        outputFile.exists()
        def zipFile = new ZipFile(outputFile)
        def zipFileEntries = Collections.list(zipFile.entries())
        zipFileEntries.findAll { it.name.endsWith("/index.js") }.size() == 3
        zipFileEntries.findAll { it.name.endsWith("/main.js") }.size() == 3
        zipFileEntries.size() == 9
    }

    private BuildResult buildWithConfigurationCacheIfAvailable(String... args) {
        if (gradleVersion >= GradleVersion.version("6.6")) {
            return build(*[*args, "--configuration-cache"])
        }
        return build(args)
    }
}
