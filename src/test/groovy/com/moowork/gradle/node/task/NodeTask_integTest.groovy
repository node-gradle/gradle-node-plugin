package com.moowork.gradle.node.task

import com.moowork.gradle.AbstractIntegTest
import org.gradle.testkit.runner.TaskOutcome

class NodeTask_integTest
    extends AbstractIntegTest
{
    def 'exec simple node program and check up-to-date detection'()
    {
        given:
        copyResources("fixtures/node")

        when:
        def result = build("hello")

        then:
        result.task(":hello").outcome == TaskOutcome.SUCCESS
        result.output.contains("Hello World")

        when:
        def result2 = build("hello")

        then:
        result2.task(":hello").outcome == TaskOutcome.UP_TO_DATE
        !result2.output.contains("Hello World")

        when:
        def result3 = build("hello", "-DchangeScript=true")

        then:
        result3.task(":hello").outcome == TaskOutcome.SUCCESS
        !result3.output.contains("Hello")

        when:
        def result4 = build("hello", "-DchangeScript=true", "-DchangeArgs=true")

        then:
        result4.task(":hello").outcome == TaskOutcome.SUCCESS
        result4.output.contains("Hello Bob")
        result4.output.contains("Hello Alice")

        when:
        def result5 = build("hello", "-DchangeScript=true", "-DchangeArgs=true")

        then:
        result5.task(":hello").outcome == TaskOutcome.UP_TO_DATE
    }

    def 'exec node program with custom settings and check up-to-date detection'()
    {
        given:
        copyResources("fixtures/node-env")

        when:
        def result = build("env")

        then:
        result.task(":env").outcome == TaskOutcome.SUCCESS
        result.output.contains("No custom environment")

        when:
        def result2 = build("env")

        then:
        result2.task(":env").outcome == TaskOutcome.UP_TO_DATE

        when:
        def result3 = build("env", "-DchangeOptions=true")

        then:
        result3.task(":env").outcome == TaskOutcome.SUCCESS
        result3.output.contains("1000000")

        when:
        def result4 = build("env", "-DchangeOptions=true")

        then:
        result4.task(":env").outcome == TaskOutcome.UP_TO_DATE

        when:
        // Reset build arguments to ensure the next change is not up-to-date
        def result5 = build("env")

        then:
        result5.task(":env").outcome == TaskOutcome.SUCCESS

        when:
        def result6 = build("env", "-DchangeEnv=true")

        then:
        result6.task(":env").outcome == TaskOutcome.SUCCESS
        result6.output.contains("Detected custom environment: custom environment value")

        when:
        def result7 = build("env", "-DchangeEnv=true")

        then:
        result7.task(":env").outcome == TaskOutcome.UP_TO_DATE

        when:
        // Reset build arguments to ensure the next change is not up-to-date
        def result8 = build("env")

        then:
        result8.task(":env").outcome == TaskOutcome.SUCCESS

        when:
        def result9 = buildAndFail("env", "-DchangeWorkingDir=true")

        then:
        result9.task(":env").outcome == TaskOutcome.FAILED
        result9.output.contains("A problem occurred starting process")

        when:
        // Reset build arguments to ensure the next change is not up-to-date
        def result10 = build("env")

        then:
        result10.task(":env").outcome == TaskOutcome.UP_TO_DATE

        when:
        def result11 = build("env", "-DignoreExitValue=true")

        then:
        result11.task(":env").outcome == TaskOutcome.SUCCESS
        result11.output.contains("No custom environment")

        when:
        def result12 = build("env", "-Dfail=true", "-DignoreExitValue=true")

        then:
        result12.task(":env").outcome == TaskOutcome.SUCCESS
        result12.output.contains("I had to fail")

        when:
        def result13 = build("env", "-Dfail=true", "-DignoreExitValue=true")

        then:
        result13.task(":env").outcome == TaskOutcome.UP_TO_DATE

        when:
        def result14 = buildAndFail("env", "-Dfail=true")

        then:
        result14.task(":env").outcome == TaskOutcome.FAILED
        result14.output.contains("I had to fail")
    }
}
