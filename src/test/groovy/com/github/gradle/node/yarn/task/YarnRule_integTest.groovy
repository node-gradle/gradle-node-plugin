package com.github.gradle.node.yarn.task

import com.github.gradle.AbstractIntegTest
import org.gradle.testkit.runner.TaskOutcome

class YarnRule_integTest extends AbstractIntegTest {
    def 'execute yarn_install rule (#gv.version)'() {
        given:
        gradleVersion = gv

        writeBuild("""
            plugins {
                id 'com.github.node-gradle.node'
            }

            node {
                yarnWorkDir = file('build/yarn')
            }
        """)
        writeEmptyPackageJson()

        when:
        def result = buildTask("yarn_install")

        then:
        result.outcome == TaskOutcome.SUCCESS

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

    def 'can execute an yarn module using yarn_run_ (#gv.version)'() {
        given:
        gradleVersion = gv

        writeBuild("""
            plugins {
                id 'com.github.node-gradle.node'
            }
        """)

        copyResources("fixtures/yarn-rule/package.json", "package.json")

        when:
        def result = build("yarn_run_hello")

        then:
        result.task(":yarn").outcome == TaskOutcome.SUCCESS
        result.task(":yarn_run_hello").outcome == TaskOutcome.SUCCESS
        result.output.contains("Hello world!")

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

    def 'can configure yarn_ rule task (#gv.version)'() {
        given:
        gradleVersion = gv

        writeBuild("""
            plugins {
                id 'com.github.node-gradle.node'
            }

            yarn_run_build {
                doFirst { project.logger.info('configured') }
            }
        """)

        copyResources("fixtures/yarn-rule/package.json", "package.json")

        when:
        def result = buildTask("help")

        then:
        result.outcome == TaskOutcome.SUCCESS

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }
}
