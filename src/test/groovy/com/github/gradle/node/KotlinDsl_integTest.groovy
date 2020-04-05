package com.github.gradle.node

import com.github.gradle.AbstractIntegTest
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables

class KotlinDsl_integTest extends AbstractIntegTest {
    @Rule
    EnvironmentVariables environmentVariables = new EnvironmentVariables()

    def 'build project using Kotlin DSL'() {
        given:
        copyResources('fixtures/kotlin/', '')
        copyResources('fixtures/javascript-project/', '')

        when:
        def result = build("run")

        then:
        result.task(":nodeSetup").outcome == TaskOutcome.SKIPPED
        result.task(":npmSetup").outcome == TaskOutcome.SKIPPED
        result.task(":npmInstall").outcome == TaskOutcome.SUCCESS
        result.task(":testNpx").outcome == TaskOutcome.SUCCESS
        result.task(":testNpm").outcome == TaskOutcome.SUCCESS
        result.task(":testYarn").outcome == TaskOutcome.SUCCESS
        result.task(":run").outcome == TaskOutcome.SUCCESS
        result.output.contains("Hello Bobby!")
    }
}
