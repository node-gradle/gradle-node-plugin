package com.moowork.gradle.node.npm

import com.moowork.gradle.AbstractIntegTest
import org.gradle.testkit.runner.TaskOutcome

class NpmTask_integTest
        extends AbstractIntegTest {

    def 'execute npm command with a package.json file and check inputs up-to-date detection'() {
        given:
        copyResources('fixtures/npm/', '')
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

    def 'execute npm command with custom execution configuration and check up-to-date-detection'() {
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
        def result3 = build(":env", "-DignoreExitValue=true", "-DnotExistingCommand=true")

        then:
        result3.task(":env").outcome == TaskOutcome.SUCCESS
        result3.output.contains("Usage: npm <command>")

        when:
        def result4 = buildAndFail(":env", "-DnotExistingCommand=true")

        then:
        result4.task(":env").outcome == TaskOutcome.FAILED
        result4.output.contains("Usage: npm <command>")

        when:
        def result5 = build(":pwd")

        then:
        result5.task(":pwd").outcome == TaskOutcome.SUCCESS
        result5.output.contains("Working directory is '${projectDir}'")

        when:
        def result6 = build(":pwd", "-DcustomWorkingDir=true")

        then:
        result6.task(":pwd").outcome == TaskOutcome.SUCCESS
        def expectedWorkingDirectory = "${projectDir}${File.separator}build${File.separator}customWorkingDirectory"
        result6.output.contains("Working directory is '${expectedWorkingDirectory}'")
    }
}
