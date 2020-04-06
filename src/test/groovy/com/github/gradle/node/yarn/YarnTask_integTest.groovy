package com.github.gradle.node.yarn

import com.github.gradle.AbstractIntegTest
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.util.GradleVersion
import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables

import java.util.regex.Pattern

class YarnTask_integTest extends AbstractIntegTest {
    @Rule
    EnvironmentVariables environmentVariables = new EnvironmentVariables()

    def 'execute yarn command with a package.json file and check inputs up-to-date detection'() {
        given:
        copyResources('fixtures/yarn/', '')
        copyResources('fixtures/javascript-project/', '')

        when:
        def result1 = build(":test")

        then:
        result1.task(":nodeSetup").outcome == TaskOutcome.SUCCESS
        result1.task(":yarnSetup").outcome == TaskOutcome.SUCCESS
        result1.task(":yarn").outcome == TaskOutcome.SUCCESS
        result1.task(":test").outcome == TaskOutcome.SUCCESS
        result1.output.contains("1 passing")

        when:
        def result2 = build(":test")

        then:
        result2.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result2.task(":yarnSetup").outcome == TaskOutcome.UP_TO_DATE
        result2.task(":yarn").outcome == TaskOutcome.SUCCESS
        result2.task(":test").outcome == TaskOutcome.UP_TO_DATE

        when:
        def result3 = build(":test", "-DchangeInputs=true")

        then:
        result3.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result3.task(":yarnSetup").outcome == TaskOutcome.UP_TO_DATE
        result3.task(":yarn").outcome == TaskOutcome.UP_TO_DATE
        result3.task(":test").outcome == TaskOutcome.SUCCESS

        when:
        def result4 = build(":version")

        then:
        result4.task(":version").outcome == TaskOutcome.SUCCESS
        result4.output.contains("> Task :version${System.lineSeparator()}1.18.0")
    }

    def 'execute yarn command with custom execution configuration and check up-to-date-detection'() {
        given:
        copyResources('fixtures/yarn-env/', '')
        copyResources('fixtures/env/', '')

        when:
        def result1 = build(":env")

        then:
        result1.task(":nodeSetup").outcome == TaskOutcome.SUCCESS
        result1.task(":yarnSetup").outcome == TaskOutcome.SUCCESS
        result1.task(":yarn").outcome == TaskOutcome.SUCCESS
        result1.task(":env").outcome == TaskOutcome.SUCCESS
        result1.output.contains("PATH=")

        when:
        def result2 = build(":env", "-DcustomEnv=true")

        then:
        result2.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result2.task(":yarnSetup").outcome == TaskOutcome.UP_TO_DATE
        result2.task(":yarn").outcome == TaskOutcome.SUCCESS
        result2.task(":env").outcome == TaskOutcome.SUCCESS
        result2.output.contains("CUSTOM=custom value")

        when:
        environmentVariables.set("NEW_ENV_VARIABLE", "Let's make the whole environment change")
        def result3 = build(":env", "-DcustomEnv=true")

        then:
        result3.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result3.task(":yarnSetup").outcome == TaskOutcome.UP_TO_DATE
        result3.task(":yarn").outcome == TaskOutcome.UP_TO_DATE
        result3.task(":env").outcome == TaskOutcome.UP_TO_DATE

        when:
        def result4 = build(":env", "-DignoreExitValue=true", "-DnotExistingCommand=true")

        then:
        result4.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result4.task(":yarnSetup").outcome == TaskOutcome.UP_TO_DATE
        result4.task(":yarn").outcome == TaskOutcome.UP_TO_DATE
        result4.task(":env").outcome == TaskOutcome.SUCCESS
        result4.output.contains("error Command \"notExistingCommand\" not found.")

        when:
        def result5 = buildAndFail(":env", "-DnotExistingCommand=true")

        then:
        result5.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result5.task(":yarnSetup").outcome == TaskOutcome.UP_TO_DATE
        result5.task(":yarn").outcome == TaskOutcome.UP_TO_DATE
        result5.task(":env").outcome == TaskOutcome.FAILED
        result5.output.contains("error Command \"notExistingCommand\" not found.")

        when:
        def result6 = build(":pwd")

        then:
        result6.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result6.task(":yarnSetup").outcome == TaskOutcome.UP_TO_DATE
        result6.task(":yarn").outcome == TaskOutcome.UP_TO_DATE
        result6.task(":pwd").outcome == TaskOutcome.SUCCESS
        result6.output.contains("Working directory is '${projectDir}'")

        when:
        def result7 = build(":pwd", "-DcustomWorkingDir=true")

        then:
        result7.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result7.task(":yarnSetup").outcome == TaskOutcome.UP_TO_DATE
        result7.task(":yarn").outcome == TaskOutcome.UP_TO_DATE
        result7.task(":pwd").outcome == TaskOutcome.UP_TO_DATE

        when:
        def result8 = build(":pwd", "-DcustomWorkingDir=true", "--rerun-tasks")

        then:
        result8.task(":nodeSetup").outcome == TaskOutcome.SUCCESS
        result8.task(":yarnSetup").outcome == TaskOutcome.SUCCESS
        result8.task(":yarn").outcome == TaskOutcome.SUCCESS
        result8.task(":pwd").outcome == TaskOutcome.SUCCESS
        def expectedWorkingDirectory = "${projectDir}${File.separator}build${File.separator}customWorkingDirectory"
        result8.output.contains("Working directory is '${expectedWorkingDirectory}'")
        new File(expectedWorkingDirectory).isDirectory()

        when:
        def result9 = build(":version")

        then:
        result9.task(":version").outcome == TaskOutcome.SUCCESS
        def versionPattern = Pattern.compile("> Task :version\\s+([0-9]+\\.[0-9]+\\.[0-9]+)")
        def versionMatch = versionPattern.matcher(result9.output)
        versionMatch.find()
        GradleVersion.version(versionMatch.group(1)) > GradleVersion.version("1.19.0")
    }
}
