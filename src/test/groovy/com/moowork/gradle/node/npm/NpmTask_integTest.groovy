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
}
