package com.github.gradle.node.npm.task

import com.github.gradle.AbstractIntegTest
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables

import static com.github.gradle.node.NodeExtension.DEFAULT_NPM_VERSION

class NpmTask_integTest extends AbstractIntegTest {
    @Rule
    EnvironmentVariables environmentVariables = new EnvironmentVariables()

    def 'execute npm command with a package.json file and check inputs up-to-date detection'() {
        given:
        copyResources("fixtures/npm/")
        copyResources("fixtures/javascript-project/")

        when:
        def result1 = build(":test")

        then:
        result1.task(":nodeSetup").outcome == TaskOutcome.SUCCESS
        result1.task(":npmSetup").outcome == TaskOutcome.SUCCESS
        result1.task(":npmInstall").outcome == TaskOutcome.SUCCESS
        result1.task(":test").outcome == TaskOutcome.SUCCESS
        result1.output.contains("1 passing")

        when:
        def result2 = build(":test")

        then:
        result2.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result2.task(":npmSetup").outcome == TaskOutcome.UP_TO_DATE
        result2.task(":npmInstall").outcome == TaskOutcome.SUCCESS
        result2.task(":test").outcome == TaskOutcome.UP_TO_DATE

        when:
        def result3 = build(":test", "-DchangeInputs=true")

        then:
        result3.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result3.task(":npmSetup").outcome == TaskOutcome.UP_TO_DATE
        result3.task(":npmInstall").outcome == TaskOutcome.UP_TO_DATE
        result3.task(":test").outcome == TaskOutcome.SUCCESS

        when:
        def result4 = build(":version")

        then:
        result4.task(":version").outcome == TaskOutcome.SUCCESS
        result4.output.contains("> Task :version${System.lineSeparator()}6.12.0")
    }

    def 'execute npm command with custom execution configuration and check up-to-date-detection'() {
        given:
        copyResources("fixtures/npm-env/")
        copyResources("fixtures/env/")

        when:
        def result1 = build(":env", "-DenableHooks=true")

        then:
        result1.task(":nodeSetup").outcome == TaskOutcome.SUCCESS
        result1.task(":npmSetup").outcome == TaskOutcome.SKIPPED
        result1.task(":npmInstall").outcome == TaskOutcome.SUCCESS
        result1.task(":env").outcome == TaskOutcome.SUCCESS
        environmentDumpContainsPathVariable(result1.output)
        result1.output.contains("Env task success with status 0")
        !result1.output.contains("Env task failure")

        when:
        def result2 = build(":env", "-DcustomEnv=true")

        then:
        result2.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result2.task(":npmSetup").outcome == TaskOutcome.SKIPPED
        result2.task(":npmInstall").outcome == TaskOutcome.SUCCESS
        result2.task(":env").outcome == TaskOutcome.SUCCESS
        result2.output.contains("CUSTOM=custom value")

        when:
        environmentVariables.set("NEW_ENV_VARIABLE", "Let's make the whole environment change")
        def result3 = build(":env", "-DcustomEnv=true")

        then:
        result3.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result3.task(":npmSetup").outcome == TaskOutcome.SKIPPED
        result3.task(":npmInstall").outcome == TaskOutcome.UP_TO_DATE
        result3.task(":env").outcome == (isConfigurationCacheEnabled() ? TaskOutcome.SUCCESS : TaskOutcome.UP_TO_DATE)

        when:
        def result4 = build(":env", "-DignoreExitValue=true", "-DnotExistingCommand=true", "-DenableHooks=true")

        then:
        result4.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result4.task(":npmSetup").outcome == TaskOutcome.SKIPPED
        result4.task(":npmInstall").outcome == TaskOutcome.UP_TO_DATE
        result4.task(":env").outcome == TaskOutcome.SUCCESS
        result4.output.contains("Usage: npm <command>")
        result4.output.contains("Env task success with status 1")
        !result4.output.contains("Env task failure")

        when:
        def result5 = buildAndFail(":env", "-DnotExistingCommand=true", "-DenableHooks=true")

        then:
        result5.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result5.task(":npmSetup").outcome == TaskOutcome.SKIPPED
        result5.task(":npmInstall").outcome == TaskOutcome.UP_TO_DATE
        result5.task(":env").outcome == TaskOutcome.FAILED
        result5.output.contains("Usage: npm <command>")
        result5.output.contains("Env task failure with status 1")
        !result5.output.contains("Env task success")

        when:
        def result6 = build(":env", "-DoutputFile=true")

        then:
        result6.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result6.task(":npmSetup").outcome == TaskOutcome.SKIPPED
        result6.task(":npmInstall").outcome == TaskOutcome.UP_TO_DATE
        result6.task(":env").outcome == TaskOutcome.SUCCESS
        !environmentDumpContainsPathVariable(result6.output)
        def outputFile = file("build/standard-output.txt")
        outputFile.exists()
        environmentDumpContainsPathVariable(outputFile.text)

        when:
        def result7 = build(":pwd")

        then:
        result7.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result7.task(":npmSetup").outcome == TaskOutcome.SKIPPED
        result7.task(":npmInstall").outcome == TaskOutcome.UP_TO_DATE
        result7.task(":pwd").outcome == TaskOutcome.SUCCESS
        result7.output.contains("Working directory is '${projectDir}'")

        when:
        def result8 = build(":pwd", "-DcustomWorkingDir=true")

        then:
        result8.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result8.task(":npmSetup").outcome == TaskOutcome.SKIPPED
        result8.task(":npmInstall").outcome == TaskOutcome.UP_TO_DATE
        result8.task(":pwd").outcome == TaskOutcome.UP_TO_DATE

        when:
        def result9 = build(":pwd", "-DcustomWorkingDir=true", "--rerun-tasks")

        then:
        result9.task(":nodeSetup").outcome == TaskOutcome.SUCCESS
        result9.task(":npmSetup").outcome == TaskOutcome.SKIPPED
        result9.task(":npmInstall").outcome == TaskOutcome.SUCCESS
        result9.task(":pwd").outcome == TaskOutcome.SUCCESS
        def expectedWorkingDirectory = "${projectDir}${File.separator}build${File.separator}customWorkingDirectory"
        result9.output.contains("Working directory is '${expectedWorkingDirectory}'")
        new File(expectedWorkingDirectory).isDirectory()

        when:
        def result10 = build(":version")

        then:
        result10.task(":version").outcome == TaskOutcome.SUCCESS
        result10.output.contains("> Task :version${System.lineSeparator()}${DEFAULT_NPM_VERSION}")
    }

    def 'execute npm command using the npm version specified in the package.json file'() {
        given:
        copyResources("fixtures/npm/")
        copyResources("fixtures/npm-present/")

        when:
        def result = build(":version")

        then:
        result.task(":version").outcome == TaskOutcome.SUCCESS
        result.output.contains("> Task :version${System.lineSeparator()}6.12.0")
    }
}
