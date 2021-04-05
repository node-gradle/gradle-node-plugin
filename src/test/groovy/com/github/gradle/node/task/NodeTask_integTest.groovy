package com.github.gradle.node.task

import com.github.gradle.AbstractIntegTest
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.util.GradleVersion
import org.junit.Assume
import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables

import static com.github.gradle.node.NodeExtension.DEFAULT_NODE_VERSION

class NodeTask_integTest extends AbstractIntegTest {
    @Rule
    EnvironmentVariables environmentVariables = new EnvironmentVariables()

    def 'download specified node version and exec simple node program and check up-to-date detection'() {
        given:
        copyResources("fixtures/node")

        when:
        def result1 = build("hello")

        then:
        result1.task(":nodeSetup").outcome == TaskOutcome.SUCCESS
        result1.task(":hello").outcome == TaskOutcome.SUCCESS
        result1.output.contains("Hello World")

        when:
        def result2 = build("hello")

        then:
        result2.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result2.task(":hello").outcome == TaskOutcome.UP_TO_DATE
        !result2.output.contains("Hello World")

        when:
        def result3 = build("hello", "-DchangeScript=true")

        then:
        result3.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result3.task(":hello").outcome == TaskOutcome.SUCCESS
        !result3.output.contains("Hello")

        when:
        def result4 = build("hello", "-DchangeScript=true", "-DchangeArgs=true")

        then:
        result4.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result4.task(":hello").outcome == TaskOutcome.SUCCESS
        result4.output.contains("Hello Bob")
        result4.output.contains("Hello Alice")

        when:
        def result5 = build("hello", "-DchangeScript=true", "-DchangeArgs=true")

        then:
        result5.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result5.task(":hello").outcome == TaskOutcome.UP_TO_DATE

        when:
        // Reset build arguments to ensure the next change is not up-to-date
        def result6 = build("hello")

        then:
        result6.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result6.task(":hello").outcome == TaskOutcome.SUCCESS

        when:
        writeFile("simple.js", "console.log('Hello Bobby');")
        def result7 = build("hello")

        then:
        result7.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result7.task(":hello").outcome == TaskOutcome.SUCCESS
        result7.output.contains("Hello Bobby")

        when:
        def result8 = buildAndFail("executeDirectoryScript")

        then:
        result8.task(":executeDirectoryScript").outcome == TaskOutcome.FAILED
        // Gradle < 7 || Gradle >= 7
        result8.output.contains("specified for property 'script' is not a file") || result8.output.contains("Reason: Expected an input to be a file but it was a directory.")

        when:
        def result9 = build(":version")

        then:
        result9.task(":version").outcome == TaskOutcome.SUCCESS
        result9.output.contains("Version: v12.13.0")
    }

    def 'download default node version and exec node program with custom settings and check up-to-date detection'() {
        given:
        copyResources("fixtures/node-env")

        when:
        def result1 = build("env", "-DenableHooks=true")

        then:
        result1.task(":nodeSetup").outcome == TaskOutcome.SUCCESS
        result1.task(":env").outcome == TaskOutcome.SUCCESS
        result1.output.contains("No custom environment")
        result1.output.contains("Env task success with status 0")
        !result1.output.contains("Env task failure")

        when:
        def result2 = build("env")

        then:
        result2.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result2.task(":env").outcome == TaskOutcome.UP_TO_DATE

        when:
        def result3 = build("env", "-DchangeOptions=true")

        then:
        result3.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result3.task(":env").outcome == TaskOutcome.SUCCESS
        result3.output.contains("1000000")

        when:
        def result4 = build("env", "-DchangeOptions=true")

        then:
        result4.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result4.task(":env").outcome == TaskOutcome.UP_TO_DATE

        when:
        // Reset build arguments to ensure the next change is not up-to-date
        def result5 = build("env")

        then:
        result5.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result5.task(":env").outcome == TaskOutcome.SUCCESS

        when:
        def result6 = build("env", "-DchangeEnv=true")

        then:
        result6.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result6.task(":env").outcome == TaskOutcome.SUCCESS
        result6.output.contains("Detected custom environment: custom value")

        when:
        environmentVariables.set("NEW_ENV_VARIABLE", "Let's make the whole environment change")
        def result7 = build("env", "-DchangeEnv=true")

        then:
        result7.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result7.task(":env").outcome == (isConfigurationCacheEnabled() ? TaskOutcome.SUCCESS : TaskOutcome.UP_TO_DATE)

        when:
        // Reset build arguments to ensure the next change is not up-to-date
        def result8 = build("env")

        then:
        result8.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result8.task(":env").outcome == TaskOutcome.SUCCESS

        when:
        def result9 = build("env", "-DchangeWorkingDir=true")

        then:
        result9.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result9.task(":env").outcome == TaskOutcome.UP_TO_DATE

        when:
        def result10 = build("env", "-DchangeWorkingDir=true", "--rerun-tasks")

        then:
        result10.task(":nodeSetup").outcome == TaskOutcome.SUCCESS
        result10.task(":env").outcome == TaskOutcome.SUCCESS
        def expectedWorkingDir = "${projectDir}${File.separator}build${File.separator}notExisting"
        result10.output.contains("Current working directory: ${expectedWorkingDir}")
        new File(expectedWorkingDir).isDirectory()

        when:
        // Reset build arguments to ensure the next change is not up-to-date
        def result11 = build("env")

        then:
        result11.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result11.task(":env").outcome == TaskOutcome.UP_TO_DATE

        when:
        def result12 = build("env", "-DignoreExitValue=true")

        then:
        result12.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result12.task(":env").outcome == TaskOutcome.SUCCESS
        result12.output.contains("No custom environment")

        when:
        def result13 = build("env", "-Dfail=true", "-DignoreExitValue=true", "-DenableHooks=true")

        then:
        result13.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result13.task(":env").outcome == TaskOutcome.SUCCESS
        result13.output.contains("I had to fail")
        result13.output.contains("Env task success with status 1")
        !result13.output.contains("Env task failure")

        when:
        def result14 = build("env", "-Dfail=true", "-DignoreExitValue=true")

        then:
        result14.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result14.task(":env").outcome == TaskOutcome.UP_TO_DATE

        when:
        def result15 = buildAndFail("env", "-Dfail=true", "-DenableHooks=true")

        then:
        result15.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result15.task(":env").outcome == TaskOutcome.FAILED
        result15.output.contains("I had to fail")
        result15.output.contains("Env task failure with status 1")
        !result15.output.contains("Env task success")

        when:
        def result16 = build("env", "-DoutputFile=true")

        then:
        result16.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result16.task(":env").outcome == TaskOutcome.SUCCESS
        !result16.output.contains("No custom environment")
        def outputFile = file("build/standard-output.txt")
        outputFile.exists()
        outputFile.text.contains("No custom environment")

        when:
        def result17 = build("env", "-Dfail=true", "-DignoreExitValue=true", "-DenableExpectFailureHook=true")

        then:
        result17.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result17.task(":env").outcome == TaskOutcome.SUCCESS
        result17.output.contains("Failed as expected")

        when:
        def result18 = buildAndFail("env", "-DignoreExitValue=true", "-DenableExpectFailureHook=true")

        then:
        result18.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result18.task(":env").outcome == TaskOutcome.FAILED
        result18.output.contains("Should have failed")

        when:
        def result19 = build(":version")

        then:
        result19.task(":version").outcome == TaskOutcome.SUCCESS
        result19.output.contains("Version: v${DEFAULT_NODE_VERSION}")
    }

    def 'try to use custom repositories when the download url is null'() {
        given:
        copyResources("fixtures/node-no-download-url")

        when:
        def result = buildAndFail("nodeSetup")

        then:
        result.output.contains("Cannot resolve external dependency org.nodejs:node:${DEFAULT_NODE_VERSION} because no repositories are defined.")
    }

    def 'make sure build works with FAIL_ON_PROJECT_REPOS using a custom repository'() {
        given:
        Assume.assumeFalse(gradleVersion < GradleVersion.version("6.8"))
        copyResources("fixtures/node-fail-on-project-repos-download")

        when:
        def result = build("hello")

        then:
        result.task(":nodeSetup").outcome == TaskOutcome.SUCCESS
        result.task(":hello").outcome == TaskOutcome.SUCCESS
    }

    def 'make sure build works with FAIL_ON_PROJECT_REPOS when using the global Node.js (no download)'() {
        given:
        Assume.assumeFalse(gradleVersion < GradleVersion.version("6.8"))
        copyResources("fixtures/node-fail-on-project-repos-no-download")

        when:
        def result = build("hello")

        then:
        result.task(":nodeSetup").outcome == TaskOutcome.SKIPPED
        result.task(":hello").outcome == TaskOutcome.SUCCESS
    }
}
