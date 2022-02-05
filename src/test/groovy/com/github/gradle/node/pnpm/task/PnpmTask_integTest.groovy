package com.github.gradle.node.pnpm.task

import com.github.gradle.AbstractIntegTest
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables

class PnpmTask_integTest extends AbstractIntegTest {
    @Rule
    EnvironmentVariables environmentVariables = new EnvironmentVariables()

    def 'execute pnpm command with a package.json file and check inputs up-to-date detection (#gv.version)'() {
        given:
        gradleVersion = gv
        copyResources('fixtures/pnpm/', '')
        copyResources('fixtures/javascript-project/', '')

        when:
        def result1 = build(":test")

        then:
        result1.task(":nodeSetup").outcome == TaskOutcome.SUCCESS
        result1.task(":pnpmSetup").outcome == TaskOutcome.SUCCESS
        result1.task(":pnpmInstall").outcome == TaskOutcome.SUCCESS
        result1.task(":test").outcome == TaskOutcome.SUCCESS
        result1.output.contains("1 passing")

        when:
        def result2 = build(":test")

        then:
        result2.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result2.task(":pnpmSetup").outcome == TaskOutcome.UP_TO_DATE
        result2.task(":pnpmInstall").outcome == TaskOutcome.SUCCESS
        result2.task(":test").outcome == TaskOutcome.UP_TO_DATE

        when:
        def result3 = build(":test", "-DchangeInputs=true")

        then:
        result3.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result3.task(":pnpmSetup").outcome == TaskOutcome.UP_TO_DATE
        result3.task(":pnpmInstall").outcome == TaskOutcome.UP_TO_DATE
        result3.task(":test").outcome == TaskOutcome.SUCCESS

        when:
        def result4 = build(":version")

        then:
        result4.task(":version").outcome == TaskOutcome.SUCCESS
        result4.output.contains("> Task :version${System.lineSeparator()}4.12.4")

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

    def 'execute pnpm command with custom execution configuration and check up-to-date-detection (#gv.version)'() {
        given:
        gradleVersion = gv
        copyResources('fixtures/pnpm-env/', '')
        copyResources('fixtures/env/', '')

        when:
        def result1 = build(":env")

        then:
        result1.task(":nodeSetup").outcome == TaskOutcome.SUCCESS
        result1.task(":pnpmSetup").outcome == TaskOutcome.SUCCESS
        result1.task(":pnpmInstall").outcome == TaskOutcome.SUCCESS
        result1.task(":env").outcome == TaskOutcome.SUCCESS
        environmentDumpContainsPathVariable(result1.output)

        when:
        def result2 = build(":env", "-DcustomEnv=true")

        then:
        result2.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result2.task(":pnpmSetup").outcome == TaskOutcome.UP_TO_DATE
        result2.task(":pnpmInstall").outcome == TaskOutcome.SUCCESS
        result2.task(":env").outcome == TaskOutcome.SUCCESS
        result2.output.contains("CUSTOM=custom value")

        when:
        environmentVariables.set("NEW_ENV_VARIABLE", "Let's make the whole environment change")
        def result3 = build(":env", "-DcustomEnv=true")

        then:
        result3.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result3.task(":pnpmSetup").outcome == TaskOutcome.UP_TO_DATE
        result3.task(":pnpmInstall").outcome == TaskOutcome.UP_TO_DATE
        result3.task(":env").outcome == (isConfigurationCacheEnabled() ? TaskOutcome.SUCCESS : TaskOutcome.UP_TO_DATE)

        when:
        def result4 = build(":env", "-DignoreExitValue=true", "-DnotExistingCommand=true")

        then:
        result4.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result4.task(":pnpmSetup").outcome == TaskOutcome.UP_TO_DATE
        result4.task(":pnpmInstall").outcome == TaskOutcome.UP_TO_DATE
        result4.task(":env").outcome == TaskOutcome.SUCCESS
        result4.output.contains("Unknown command 'notExistingCommand'")

        when:
        def result5 = buildAndFail(":env", "-DnotExistingCommand=true")

        then:
        result5.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result5.task(":pnpmSetup").outcome == TaskOutcome.UP_TO_DATE
        result5.task(":pnpmInstall").outcome == TaskOutcome.UP_TO_DATE
        result5.task(":env").outcome == TaskOutcome.FAILED
        result5.output.contains("Unknown command 'notExistingCommand'")

        when:
        def result6 = build(":pwd")

        then:
        result6.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result6.task(":pnpmSetup").outcome == TaskOutcome.UP_TO_DATE
        result6.task(":pnpmInstall").outcome == TaskOutcome.UP_TO_DATE
        result6.task(":pwd").outcome == TaskOutcome.SUCCESS
        result6.output.contains("Working directory is '${projectDir}'")

        when:
        def result7 = build(":pwd", "-DcustomWorkingDir=true")

        then:
        result7.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result7.task(":pnpmSetup").outcome == TaskOutcome.UP_TO_DATE
        result7.task(":pnpmInstall").outcome == TaskOutcome.UP_TO_DATE
        result7.task(":pwd").outcome == TaskOutcome.UP_TO_DATE

        when:
        def result8 = build(":pwd", "-DcustomWorkingDir=true", "--rerun-tasks")

        then:
        result8.task(":nodeSetup").outcome == TaskOutcome.SUCCESS
        result8.task(":pnpmSetup").outcome == TaskOutcome.SUCCESS
        result8.task(":pnpmInstall").outcome == TaskOutcome.SUCCESS
        result8.task(":pwd").outcome == TaskOutcome.SUCCESS
        def expectedWorkingDirectory = "${projectDir}${File.separator}build${File.separator}customWorkingDirectory"
        result8.output.contains("Working directory is '${expectedWorkingDirectory}'")
        new File(expectedWorkingDirectory).isDirectory()

        when:
        def result9 = build(":version")

        then:
        result9.task(":version").outcome == TaskOutcome.SUCCESS
        result9.output.contains("> Task :version${System.lineSeparator()}4.12.4")

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

    def 'execute pnpm command using the pnpm version specified in the package.json file (#gv.version)'() {
        given:
        gradleVersion = gv
        copyResources('fixtures/pnpm/', '')
        copyResources('fixtures/pnpm-present/', '')

        when:
        def result = build(":version")

        then:
        result.task(":version").outcome == TaskOutcome.SUCCESS
        result.output.contains("> Task :version${System.lineSeparator()}4.12.1")

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }
}
