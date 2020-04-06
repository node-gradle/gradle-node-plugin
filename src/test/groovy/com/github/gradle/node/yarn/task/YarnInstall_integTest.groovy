package com.github.gradle.node.yarn.task

import com.github.gradle.AbstractIntegTest
import org.gradle.testkit.runner.TaskOutcome

class YarnInstall_integTest extends AbstractIntegTest {
    def 'install packages with yarn'() {
        given:
        writeBuild('''
            plugins {
                id 'com.github.node-gradle.node'
            }

            node {
                yarnWorkDir = file('build/yarn')
            }
        ''')
        writeEmptyPackageJson()

        when:
        def result = buildTask('yarn')

        then:
        result.outcome == TaskOutcome.SUCCESS

        when:
        result = buildTask('yarn')

        then:
        result.outcome == TaskOutcome.SUCCESS

        when:
        result = buildTask('yarn')

        then:
        result.outcome == TaskOutcome.UP_TO_DATE
    }

    def 'install packages with yarn and and postinstall task requiring node and yarn'() {
        given:
        writeBuild('''
            plugins {
                id 'com.github.node-gradle.node'
            }
            node {
                yarnWorkDir = file('build/yarn')
            }
        ''')
        writePackageJson(""" {
            "name": "example",
            "dependencies": {},
            "versionOutput" : "node --version",
            "postinstall" : "yarn run versionOutput"
        }
        """)

        when:
        def result = buildTask('yarn')

        then:
        result.outcome == TaskOutcome.SUCCESS

        when:
        result = buildTask('yarn')

        then:
        result.outcome == TaskOutcome.SUCCESS

        when:
        result = buildTask('yarn')

        then:
        result.outcome == TaskOutcome.UP_TO_DATE
    }

    def 'install packages with yarn in different directory'() {
        given:
        writeBuild('''
            plugins {
                id 'com.github.node-gradle.node'
            }

            node {
                yarnWorkDir = file('build/yarn')
                nodeModulesDir = file('subdirectory')
            }
        ''')
        writeFile('subdirectory/package.json', """{
            "name": "example",
            "dependencies": {
            }
        }""")

        when:
        def result = buildTask('yarn')

        then:
        result.outcome == TaskOutcome.SUCCESS
    }

    def 'verity output configuration'() {
        given:
        writeBuild('''
            plugins {
                id 'com.github.node-gradle.node'
            }

            def changeOutput = System.properties["changeOutput"] ? System.properties["changeOutput"] == "true" : false
            if (changeOutput) {
                yarn {
                    nodeModulesOutputFilter = { it.exclude("mocha/package.json") }
                }
            }
        ''')
        writePackageJson("""
            {
              "name": "hello",
              "dependencies": {
                "mocha": "6.2.0"
              }
            }
        """)

        when:
        def result1 = build("yarn")

        then:
        result1.task(":yarn").outcome == TaskOutcome.SUCCESS

        when:
        def result2 = build("yarn")

        then:
        // Because package-lock.json was created
        result2.task(":yarn").outcome == TaskOutcome.SUCCESS

        when:
        // Let's add a file in the node_modules directory
        writeFile("node_modules/mocha/newFile.txt", "hello")
        def result3 = build("yarn")

        then:
        // It should not make the build out-of-date
        result3.task(":yarn").outcome == TaskOutcome.UP_TO_DATE

        when:
        // Let's update a file in the node_modules directory
        writeFile("node_modules/mocha/package.json", "modified package.json")
        def result4 = build("yarn")

        then:
        // This time the build should not be up-to-date and the file should be reset
        result4.task(":yarn").outcome == TaskOutcome.SUCCESS
        createFile("node_modules/mocha/package.json").text != "modified package.json"

        when:
        // Let's delete a file in the node_modules directory
        createFile("node_modules/mocha/package.json").delete()
        def result5 = build("yarn")

        then:
        // This time the build should be up-to-date
        result5.task(":yarn").outcome == TaskOutcome.SUCCESS
    }

    def 'verity output configuration when filtering node_modules output'() {
        given:
        writeBuild('''
            plugins {
                id 'com.github.node-gradle.node'
            }

            yarn {
                nodeModulesOutputFilter = { it.exclude("mocha/package.json") }
            }
        ''')
        writePackageJson("""
            {
              "name": "hello",
              "dependencies": {
                "mocha": "6.2.0"
              }
            }
        """)

        when:
        createFile("node_modules").deleteDir()
        def result1 = build("yarn")

        then:
        result1.task(":yarn").outcome == TaskOutcome.SUCCESS

        when:
        // Let's add a file in the node_modules directory
        writeFile("node_modules/mocha/newFile.txt", "hello")
        def result2 = build("yarn")

        then:
        // It should make the build out-of-date
        result2.task(":yarn").outcome == TaskOutcome.SUCCESS

        when:
        createFile("node_modules").delete()
        def result3 = build("yarn", "--rerun-tasks")

        then:
        result3.task(":yarn").outcome == TaskOutcome.SUCCESS

        when:
        // Let's update a file in the node_modules directory
        writeFile("node_modules/mocha/package.json", "modified package.json")
        def result4 = build("yarn")

        then:
        // The build should still be up-to-date
        result4.task(":yarn").outcome == TaskOutcome.UP_TO_DATE

        when:
        // Let's delete an excluded file in the node_modules directory
        createFile("node_modules/mocha/package.json").delete()
        def result5 = build("yarn")

        then:
        // The build should still be up-to-date
        result5.task(":yarn").outcome == TaskOutcome.UP_TO_DATE

        when:
        // Let's delete a not excluded file in the node_modules directory
        createFile("node_modules/mocha/mocha.js").delete()
        def result6 = build("yarn")

        then:
        // This time the build should not be up-to-date since not the whole node_modules directory is excluded
        result6.task(":yarn").outcome == TaskOutcome.SUCCESS
    }

}
