package com.moowork.gradle.node.task

import com.moowork.gradle.AbstractIntegTest
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables

class NodeTask_integTest
        extends AbstractIntegTest {
    @Rule
    EnvironmentVariables environmentVariables = new EnvironmentVariables()

    def 'exec simple node program and check up-to-date detection'() {
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
        def result8 = build("helloFromIndex")

        then:
        result8.task(":helloFromIndex").outcome == TaskOutcome.SUCCESS
        result8.output.contains("Hello world from index.js!")
        result8.output.contains("Using the NodeTask with a script directory")

        when:
        createFile("simple.js").delete()
        def result9 = build("helloFromIndex")

        then:
        // Ensure that the helloFromIndex task input does not contain the whole directory
        result9.task(":helloFromIndex").outcome == TaskOutcome.UP_TO_DATE

        when:
        writeFile("index.js", "console.log('Hello Gradle');")
        def result10 = build("helloFromIndex")

        then:
        // Ensure that the helloFromIndex task input contains the index.js file
        result10.task(":helloFromIndex").outcome == TaskOutcome.SUCCESS
        result10.output.contains("Hello Gradle")
        result10.output.contains("Using the NodeTask with a script directory")

        when:
        def result11 = build(":version")

        then:
        result11.task(":version").outcome == TaskOutcome.SUCCESS
        result11.output.contains("Version: v12.13.0")
    }

    def 'exec node program with custom settings and check up-to-date detection'() {
        given:
        copyResources("fixtures/node-env")

        when:
        def result1 = build("env")

        then:
        result1.task(":nodeSetup").outcome == TaskOutcome.SUCCESS
        result1.task(":env").outcome == TaskOutcome.SUCCESS
        result1.output.contains("No custom environment")

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
        result6.output.contains("Detected custom environment: custom environment value")

        when:
        environmentVariables.set("NEW_ENV_VARIABLE", "Let's make the whole environment change")
        def result7 = build("env", "-DchangeEnv=true")

        then:
        result7.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result7.task(":env").outcome == TaskOutcome.UP_TO_DATE

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
        def result13 = build("env", "-Dfail=true", "-DignoreExitValue=true")

        then:
        result13.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result13.task(":env").outcome == TaskOutcome.SUCCESS
        result13.output.contains("I had to fail")

        when:
        def result14 = build("env", "-Dfail=true", "-DignoreExitValue=true")

        then:
        result14.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result14.task(":env").outcome == TaskOutcome.UP_TO_DATE

        when:
        def result15 = buildAndFail("env", "-Dfail=true")

        then:
        result15.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result15.task(":env").outcome == TaskOutcome.FAILED
        result15.output.contains("I had to fail")

        when:
        def result16 = build(":version")

        then:
        result16.task(":version").outcome == TaskOutcome.SUCCESS
        result16.output.contains("Version: v10.14.0")
    }
}
