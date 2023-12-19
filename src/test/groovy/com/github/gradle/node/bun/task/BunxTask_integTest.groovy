package com.github.gradle.node.bun.task

import com.github.gradle.AbstractIntegTest
import com.github.gradle.node.NodeExtension
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables
import spock.lang.Ignore
import spock.lang.IgnoreIf

import java.util.regex.Pattern

import static com.github.gradle.node.NodeExtension.DEFAULT_NPM_VERSION

@IgnoreIf({ os.windows })
class BunxTask_integTest extends AbstractIntegTest {
    def 'execute bunx command with no package.json file (#gv.version)'() {
        given:
        gradleVersion = gv

        writeBuild('''
            plugins {
                id 'com.github.node-gradle.node'
            }

            task camelCase(type: BunxTask) {
                command = 'chcase-cli'
                args = ['--help']
            }
        ''')

        when:
        def result = build(":camelCase")

        then:
        result.task(":nodeSetup").outcome == TaskOutcome.SKIPPED
        result.task(":bunSetup").outcome == TaskOutcome.SUCCESS
        result.task(":camelCase").outcome == TaskOutcome.SUCCESS
        result.output.contains("--case, -C  Which case to convert to")

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

    def 'execute bunx command with a package.json file and check inputs up-to-date detection (#gv.version)'() {
        given:
        gradleVersion = gv

        copyResources("fixtures/bunx/")
        copyResources("fixtures/javascript-project/")

        when:
        def result1 = build(":test")

        then:
        result1.task(":nodeSetup").outcome == TaskOutcome.SKIPPED
        result1.task(":bunSetup").outcome == TaskOutcome.SUCCESS
        result1.task(":bunInstall").outcome == TaskOutcome.SUCCESS
        result1.task(":lint").outcome == TaskOutcome.SUCCESS
        result1.task(":test").outcome == TaskOutcome.SUCCESS
        result1.output.contains("5 problems (0 errors, 5 warnings)")
        result1.output.contains("1 passing")

        when:
        def result2 = build(":test")

        then:
        result2.task(":nodeSetup").outcome == TaskOutcome.SKIPPED
        result2.task(":bunSetup").outcome == TaskOutcome.UP_TO_DATE
        result2.task(":bunInstall").outcome == TaskOutcome.SUCCESS
        result2.task(":lint").outcome == TaskOutcome.UP_TO_DATE
        result2.task(":test").outcome == TaskOutcome.UP_TO_DATE

        when:
        def result3 = build(":test", "-DchangeInputs=true")

        then:
        result3.task(":nodeSetup").outcome == TaskOutcome.SKIPPED
        result3.task(":bunSetup").outcome == TaskOutcome.UP_TO_DATE
        result3.task(":bunInstall").outcome == TaskOutcome.UP_TO_DATE
        result3.task(":lint").outcome == TaskOutcome.SUCCESS
        result3.task(":test").outcome == TaskOutcome.SUCCESS

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

    def 'execute bun pwd command with custom execution configuration and check up-to-date-detection'() {
        given:
        gradleVersion = gv

        copyResources("fixtures/bunx-env/")
        copyResources("fixtures/env/")

        when:
        def result7 = build(":pwd")

        then:
        result7.task(":pwd").outcome == TaskOutcome.SUCCESS
        result7.output.contains("workingDirectory='${projectDir}'")

        when:
        def result8 = build(":pwd", "-DcustomWorkingDir=true")

        then:
        result8.task(":pwd").outcome == TaskOutcome.UP_TO_DATE

        when:
        def result9 = build(":pwd", "-DcustomWorkingDir=true", "--rerun-tasks")

        then:
        result9.task(":nodeSetup").outcome == TaskOutcome.SKIPPED
        result9.task(":bunSetup").outcome == TaskOutcome.SUCCESS
        result9.task(":bunInstall").outcome == TaskOutcome.SUCCESS
        result9.task(":pwd").outcome == TaskOutcome.SUCCESS
        def expectedWorkingDirectory = "${projectDir}${File.separator}build${File.separator}customWorkingDirectory"
        result9.output.contains("workingDirectory='${expectedWorkingDirectory}'")
        new File(expectedWorkingDirectory).isDirectory()

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

    def 'execute bunx env command with custom execution configuration and check up-to-date-detection'() {
        given:
        gradleVersion = gv

        copyResources("fixtures/bunx-env/")
        copyResources("fixtures/bun-env/package.json", "package.json")

        when:
        def result1 = build(":env")

        then:
        result1.task(":nodeSetup").outcome == TaskOutcome.SKIPPED
        result1.task(":bunSetup").outcome == TaskOutcome.SUCCESS
        result1.task(":bunInstall").outcome == TaskOutcome.SUCCESS
        result1.task(":env").outcome == TaskOutcome.SUCCESS
        // Sometimes the PATH variable is not defined in Windows Powershell, but the PATHEXT is
        Pattern.compile("^PATH(?:EXT)?=.+\$", Pattern.MULTILINE).matcher(result1.output).find()

        when:
        def result2 = build(":env", "-DcustomEnv=true")

        then:
        result2.task(":nodeSetup").outcome == TaskOutcome.SKIPPED
        result2.task(":bunSetup").outcome == TaskOutcome.UP_TO_DATE
        result2.task(":bunInstall").outcome == TaskOutcome.SUCCESS
        result2.task(":env").outcome == TaskOutcome.SUCCESS
        result2.output.contains("CUSTOM=custom value")

        when:
        System.setProperty("NEW_ENV_VARIABLE", "Let's make the whole environment change")
        def result3 = build(":env", "-DcustomEnv=true")

        then:
        result3.task(":nodeSetup").outcome == TaskOutcome.SKIPPED
        result3.task(":bunSetup").outcome == TaskOutcome.UP_TO_DATE
        result3.task(":bunInstall").outcome == TaskOutcome.UP_TO_DATE
        result3.task(":env").outcome == TaskOutcome.UP_TO_DATE

        when:
        def result4 = build(":env", "-DignoreExitValue=true", "-DnotExistingCommand=true")

        then:
        result4.task(":nodeSetup").outcome == TaskOutcome.SKIPPED
        result4.task(":bunSetup").outcome == TaskOutcome.UP_TO_DATE
        result4.task(":bunInstall").outcome == TaskOutcome.UP_TO_DATE
        result4.task(":env").outcome == TaskOutcome.SUCCESS
        result4.output.contains("E404")

        when:
        def result5 = buildAndFail(":env", "-DnotExistingCommand=true")

        then:
        result5.task(":nodeSetup").outcome == TaskOutcome.SKIPPED
        result5.task(":bunSetup").outcome == TaskOutcome.UP_TO_DATE
        result5.task(":bunInstall").outcome == TaskOutcome.UP_TO_DATE
        result5.task(":env").outcome == TaskOutcome.FAILED
        result5.output.contains("E404")

        when:
        def result6 = build(":env", "-DoutputFile=true", "--stacktrace")

        then:
        result6.task(":nodeSetup").outcome == TaskOutcome.SKIPPED
        result6.task(":bunSetup").outcome == TaskOutcome.UP_TO_DATE
        result6.task(":bunInstall").outcome == TaskOutcome.UP_TO_DATE
        result6.task(":env").outcome == TaskOutcome.SUCCESS
        !environmentDumpContainsPathVariable(result6.output)
        def outputFile = file("build/standard-output.txt")
        outputFile.exists()
        environmentDumpContainsPathVariable(outputFile.text)

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

    @Ignore("Should it even work that way?")
    def 'execute bunx command using the npm version specified in the package.json file (#gv.version)'() {
        given:
        gradleVersion = gv

        copyResources("fixtures/bunx/")
        copyResources("fixtures/bun-present/")

        when:
        def result = build(":version")

        then:
        result.task(":version").outcome == TaskOutcome.SUCCESS
        result.output.contains("> Task :version${System.lineSeparator()}1.0.0")

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }
}
