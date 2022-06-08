package com.github.gradle.node

import com.github.gradle.AbstractIntegTest
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables

class SystemVersion_integTest extends AbstractIntegTest {
    @Rule
    EnvironmentVariables environmentVariables = new EnvironmentVariables()

    def 'use system node version and exec node, npm, npx and yarn program'() {
        given:
        copyResources("fixtures/node-system-version")

        when:
        def result1 = build(":hello")

        then:
        result1.task(":nodeSetup").outcome == TaskOutcome.SKIPPED
        result1.task(":hello").outcome == TaskOutcome.SUCCESS
        result1.output.contains("Hello world!")

        when:
        def result2 = build(":countRepositories")

        then:
        result2.task(":countRepositories").outcome == TaskOutcome.SUCCESS
        result2.output.contains("Project repositories: 0")

        when:
        def result3 = build(":npmVersion")

        then:
        result3.task(":npmVersion").outcome == TaskOutcome.SUCCESS
        result3.output.contains("  npm: '")

        when:
        def result4 = build(":npxHelp")

        then:
        result4.task(":npxHelp").outcome == TaskOutcome.SUCCESS
        result4.output.contains("Run a command from a local or remote npm package")

        when:
        def result5 = build(":yarnHelp")

        then:
        result5.task(":yarnHelp").outcome == TaskOutcome.SUCCESS
        result5.output.contains("Usage: yarn [command] [flags]")
    }
}
