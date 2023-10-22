package com.github.gradle.node.bun.task

import com.github.gradle.AbstractIntegTest
import com.github.gradle.node.bun.BunUtils
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Ignore
import spock.lang.IgnoreIf

@IgnoreIf({ os.windows })
class BunTask_integTest extends AbstractIntegTest {
    def 'execute bun command with a package.json file and check inputs up-to-date detection (#gv.version)'() {
        given:
        gradleVersion = gv
        copyResources('fixtures/bun/', '')
        copyResources('fixtures/javascript-project/', '')

        when:
        def result1 = build(":test")

        then:
        result1.task(":bunSetup").outcome == TaskOutcome.SUCCESS
        result1.task(":bunInstall").outcome == TaskOutcome.SUCCESS
        result1.task(":npmInstall") == null
        result1.task(":test").outcome == TaskOutcome.SUCCESS
        result1.output.contains("1 passing")

        when:
        def result2 = build(":test")

        then:
        result2.task(":bunSetup").outcome == TaskOutcome.UP_TO_DATE
        result2.task(":bunInstall").outcome == TaskOutcome.SUCCESS
        result2.task(":npmInstall") == null
        result2.task(":test").outcome == TaskOutcome.UP_TO_DATE

        when:
        def result3 = build(":test", "-DchangeInputs=true")

        then:
        result3.task(":bunSetup").outcome == TaskOutcome.UP_TO_DATE
        result3.task(":bunInstall").outcome == TaskOutcome.UP_TO_DATE
        result3.task(":npmInstall") == null
        result3.task(":test").outcome == TaskOutcome.SUCCESS

        when:
        def result4 = build(":version")

        then:
        result4.task(":version").outcome == TaskOutcome.SUCCESS
        result4.output.contains("> Task :version${System.lineSeparator()}${BunUtils.VERSION}")

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

    def 'execute bun command with custom execution configuration and check up-to-date-detection (#gv.version)'() {
        given:
        gradleVersion = gv
        copyResources('fixtures/bun-env/', '')

        when:
        def result1 = build(":env")

        then:
        result1.task(":bunSetup").outcome == TaskOutcome.SUCCESS
        result1.task(":bunInstall").outcome == TaskOutcome.SUCCESS
        result1.task(":env").outcome == TaskOutcome.SUCCESS
        environmentDumpContainsPathVariable(result1.output)

        when:
        def result2 = build(":env", "-DcustomEnv=true")

        then:
        result2.task(":bunSetup").outcome == TaskOutcome.UP_TO_DATE
        result2.task(":bunInstall").outcome == TaskOutcome.SUCCESS
        result2.task(":env").outcome == TaskOutcome.SUCCESS
        result2.output.contains("CUSTOM=custom value")

        when:
        System.setProperty("NEW_ENV_VARIABLE", "Let's make the whole environment change")
        def result3 = build(":env", "-DcustomEnv=true")

        then:
        result3.task(":bunSetup").outcome == TaskOutcome.UP_TO_DATE
        result3.task(":bunInstall").outcome == TaskOutcome.UP_TO_DATE
        result3.task(":env").outcome == TaskOutcome.UP_TO_DATE

        when:
        def result4 = build(":env", "-DignoreExitValue=true", "-DnotExistingCommand=true")

        then:
        result4.task(":bunSetup").outcome == TaskOutcome.UP_TO_DATE
        result4.task(":bunInstall").outcome == TaskOutcome.UP_TO_DATE
        result4.task(":env").outcome == TaskOutcome.SUCCESS
        result4.output.contains("script not found \"notExistingCommand\"")

        when:
        def result5 = buildAndFail(":env", "-DnotExistingCommand=true")

        then:
        result5.task(":bunSetup").outcome == TaskOutcome.UP_TO_DATE
        result5.task(":bunInstall").outcome == TaskOutcome.UP_TO_DATE
        result5.task(":env").outcome == TaskOutcome.FAILED
        result5.output.contains("script not found \"notExistingCommand\"")

        when:
        def result6 = build(":pwd")

        then:
        result6.task(":bunSetup").outcome == TaskOutcome.UP_TO_DATE
        result6.task(":bunInstall").outcome == TaskOutcome.UP_TO_DATE
        result6.task(":pwd").outcome == TaskOutcome.SUCCESS
        result6.output.contains("Working directory is '${projectDir}'")

        when:
        def result7 = build(":pwd", "-DcustomWorkingDir=true")

        then:
        result7.task(":bunSetup").outcome == TaskOutcome.UP_TO_DATE
        result7.task(":bunInstall").outcome == TaskOutcome.UP_TO_DATE
        result7.task(":pwd").outcome == TaskOutcome.UP_TO_DATE

        when:
        def result8 = build(":pwd", "-DcustomWorkingDir=true", "--rerun-tasks")

        then:
        result8.task(":bunSetup").outcome == TaskOutcome.SUCCESS
        result8.task(":bunInstall").outcome == TaskOutcome.SUCCESS
        result8.task(":pwd").outcome == TaskOutcome.SUCCESS
        def expectedWorkingDirectory = "${projectDir}${File.separator}build${File.separator}customWorkingDirectory"
        result8.output.contains("Working directory is '${expectedWorkingDirectory}'")
        new File(expectedWorkingDirectory).isDirectory()

        when:
        def result9 = build(":version")

        then:
        result9.task(":version").outcome == TaskOutcome.SUCCESS
        result9.output.contains("> Task :version${System.lineSeparator()}${BunUtils.VERSION}")

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

    @Ignore("Should it even work that way?")
    def 'execute bun command using the bun version specified in the package.json file (#gv.version)'() {
        given:
        gradleVersion = gv
        copyResources('fixtures/bun/', '')
        copyResources('fixtures/bun-present/', '')

        when:
        def result = build(":version")

        then:
        result.task(":version").outcome == TaskOutcome.SUCCESS
        result.output.contains("> Task :version${System.lineSeparator()}${BunUtils.VERSION}")

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }
}
