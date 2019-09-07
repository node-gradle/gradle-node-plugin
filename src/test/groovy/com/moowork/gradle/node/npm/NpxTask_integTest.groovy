package com.moowork.gradle.node.npm

import com.moowork.gradle.AbstractIntegTest
import org.gradle.testkit.runner.TaskOutcome

class NpxTask_integTest
        extends AbstractIntegTest {
    def 'execute npx command with no package.json file'() {
        given:
        writeBuild('''
            plugins {
                id 'com.github.node-gradle.node'
            }

            node {
                version = "10.14.0"
                npmVersion = "6.4.1"
                download = true
                workDir = file('build/node')
            }
            
            task camelCase(type: NpxTask) {
                command = 'chcase-cli'
                args = ['--help']
            }
        ''')

        when:
        def result = build(":camelCase")

        then:
        result.task(":camelCase").outcome == TaskOutcome.SUCCESS
        result.output.contains("--case, -C  Which case to convert to")
    }

    def 'execute npx command with a package.json file and check inputs up-to-date detection'() {
        given:
        copyResources('fixtures/npx/', '')
        copyResources('fixtures/javascript-project/', '')

        when:
        def result = build(":test")

        then:
        result.task(":lint").outcome == TaskOutcome.SUCCESS
        result.task(":test").outcome == TaskOutcome.SUCCESS
        result.output.contains("3 problems (0 errors, 3 warnings)")
        result.output.contains("1 passing")

        when:
        def result2 = build(":test")

        then:
        result2.task(":lint").outcome == TaskOutcome.UP_TO_DATE
        result2.task(":test").outcome == TaskOutcome.UP_TO_DATE

        when:
        def result3 = build(":test", "-DchangeInputs=true")

        then:
        result3.task(":lint").outcome == TaskOutcome.SUCCESS
        result3.task(":test").outcome == TaskOutcome.SUCCESS
    }
}
