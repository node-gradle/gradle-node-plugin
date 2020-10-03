package com.github.gradle.node.yarn.task

import com.github.gradle.AbstractIntegTest
import org.gradle.testkit.runner.TaskOutcome

class YarnRule_integTest extends AbstractIntegTest {
    def 'execute yarn_install rule'() {
        given:
        writeBuild("""
            plugins {
                id 'com.github.node-gradle.node'
            }

            node {
                yarnWorkDir = file('build/yarn')
                disableTaskRules = false
            }
        """)
        writeEmptyPackageJson()

        when:
        def result = buildTask("yarn_install")

        then:
        result.outcome == TaskOutcome.SUCCESS
    }

    def 'can execute an yarn module using yarn_run_'() {
        given:
        writeBuild("""
            plugins {
                id 'com.github.node-gradle.node'
            }
            
            node {
                disableTaskRules = false
            }
        """)

        copyResources("fixtures/yarn-rule/package.json", "package.json")

        when:
        def result = build("yarn_run_hello")

        then:
        result.task(":yarn").outcome == TaskOutcome.SUCCESS
        result.task(":yarn_run_hello").outcome == TaskOutcome.SUCCESS
        result.output.contains("Hello world!")
    }
}
