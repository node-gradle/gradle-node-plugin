package com.github.gradle.node.pnpm.task

import com.github.gradle.AbstractIntegTest
import org.gradle.testkit.runner.TaskOutcome

import java.util.regex.Pattern

class PnpmRule_integTest extends AbstractIntegTest {
    def 'execute pnpm_install rule'() {
        given:
        writeBuild('''
            plugins {
                id 'com.github.node-gradle.node'
            }
        ''')
        writeEmptyPackageJson()

        when:
        def result = buildTask('pnpm_install')

        then:
        result.outcome == TaskOutcome.SUCCESS
    }

    def 'Use downloaded pnpm version'() {
        given:
        writeBuild('''
            plugins {
                id 'com.github.node-gradle.node'
            }
            node {
                download = true
                pnpmVersion = '4.12.4'
            }
        ''')
        writeEmptyPackageJson()

        when:
        def result = build('pnpm_--version')

        then:
        result.task(':pnpm_--version').outcome == TaskOutcome.SUCCESS
        result.output =~ /\n4\.12\.4\n/
    }

    def 'Use local pnpm installation'() {
        given:
        writeBuild('''
            plugins {
                id 'com.github.node-gradle.node'
            }
            node {
                download = true
            }
        ''' )
        writeEmptyPackageJson()

        when:
        build('pnpm_install_pnpm@4.12.1')
        def result = build('pnpm_--version')

        then:
        result.output =~ /\n4\.12\.1\n/
        result.task(':pnpm_--version').outcome == TaskOutcome.SUCCESS
    }

    def 'can execute an pnpm module using pnpm_run_'() {
        given:
        writeBuild('''
            plugins {
                id 'com.github.node-gradle.node'
            }
        ''')

        copyResources('fixtures/npm-missing/package.json', 'package.json')

        when:
        def result = buildTask('pnpm_run_echoTest')

        then:
        result.outcome == TaskOutcome.SUCCESS
        fileExists('test.txt')
    }

    def 'succeeds to run pnpm module using pnpm_run_ when the package.json file contains local pnpm'() {
        given:
        writeBuild('''
            plugins {
                id 'com.github.node-gradle.node'
            }
        ''')

        copyResources('fixtures/pnpm-present/')

        when:
        def result = build('pnpm_run_pnpmVersion')

        then:
        result.task(":pnpmInstall").outcome == TaskOutcome.SUCCESS
        result.task(":pnpm_run_pnpmVersion").outcome == TaskOutcome.SUCCESS
        def versionPattern = Pattern.compile(".*Version\\s+4.12.1.*", Pattern.DOTALL)
        versionPattern.matcher(result.output).find()
    }

    def 'can execute subtasks using pnpm'() {
        given:
        writeBuild('''
            plugins {
                id 'com.github.node-gradle.node'
            }
        ''')
        writePackageJson(""" {
            "name": "example",
            "dependencies": {},
            "scripts": {
                "parent" : "echo 'parent1' > parent1.txt && pnpm run child1 && pnpm run child2 && echo 'parent2' > parent2.txt",
                "child1": "echo 'child1' > child1.txt",
                "child2": "echo 'child2' > child2.txt"
            }
        }
        """)

        when:
        def result = buildTask('pnpm_run_parent')

        then:
        result.outcome == TaskOutcome.SUCCESS
        fileExists('parent1.txt')
        fileExists('child1.txt')
        fileExists('child2.txt')
        fileExists('parent2.txt')
    }

    def 'Custom workingDir'() {
        given:
        writeBuild('''
            plugins {
                id 'com.github.node-gradle.node'
            }
            node {
                pnpmVersion = '4.12.4'
                nodeModulesDir = file("frontend")
            }
        ''')
        writeFile('frontend/package.json', """{
            "name": "example",
            "dependencies": {},
            "scripts": {
                "whatVersion": "pnpm --version"
            }
        }""")

        when:
        def result = build('pnpm_run_whatVersion')

        then:
        result.task(':pnpm_run_whatVersion').outcome == TaskOutcome.SUCCESS
        result.output =~ /\n4\.12\.4\n/
    }
}
