package com.moowork.gradle.node.yarn

import com.moowork.gradle.AbstractIntegTest
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables

class YarnTask_integTest
        extends AbstractIntegTest {
    @Rule
    EnvironmentVariables environmentVariables = new EnvironmentVariables()

    def 'execute yarn command with a package.json file and check inputs up-to-date detection'() {
        given:
        copyResources('fixtures/yarn/', '')
        copyResources('fixtures/javascript-project/', '')

        when:
        def result = build(":test")

        then:
        result.task(":test").outcome == TaskOutcome.SUCCESS
        result.output.contains("1 passing")

        when:
        def result2 = build(":test")

        then:
        result2.task(":test").outcome == TaskOutcome.UP_TO_DATE

        when:
        def result3 = build(":test", "-DchangeInputs=true")

        then:
        result3.task(":test").outcome == TaskOutcome.SUCCESS
    }

    def 'execute yarn command with custom execution configuration and check up-to-date-detection'() {
        given:
        copyResources('fixtures/npm-env/', '')
        copyResources('fixtures/env/', '')

        when:
        def result1 = build(":env")

        then:
        result1.task(":env").outcome == TaskOutcome.SUCCESS
        result1.output.contains("PATH=")

        when:
        def result2 = build(":env", "-DcustomEnv=true")

        then:
        result2.task(":env").outcome == TaskOutcome.SUCCESS
        result2.output.contains("CUSTOM=custom value")

        when:
        environmentVariables.set("NEW_ENV_VARIABLE", "Let's make the whole environment change")
        def result3 = build(":env", "-DcustomEnv=true")

        then:
        result3.task(":env").outcome == TaskOutcome.UP_TO_DATE

        when:
        def result4 = build(":env", "-DignoreExitValue=true", "-DnotExistingCommand=true")

        then:
        result4.task(":env").outcome == TaskOutcome.SUCCESS
        result4.output.contains("Usage: npm <command>")

        when:
        def result5 = buildAndFail(":env", "-DnotExistingCommand=true")

        then:
        result5.task(":env").outcome == TaskOutcome.FAILED
        result5.output.contains("Usage: npm <command>")

        when:
        def result6 = build(":pwd")

        then:
        result6.task(":pwd").outcome == TaskOutcome.SUCCESS
        result6.output.contains("Working directory is '${projectDir}'")

        when:
        def result7 = build(":pwd", "-DcustomWorkingDir=true")

        then:
        result7.task(":pwd").outcome == TaskOutcome.SUCCESS
        def expectedWorkingDirectory = "${projectDir}${File.separator}build${File.separator}customWorkingDirectory"
        result7.output.contains("Working directory is '${expectedWorkingDirectory}'")
    }
}
