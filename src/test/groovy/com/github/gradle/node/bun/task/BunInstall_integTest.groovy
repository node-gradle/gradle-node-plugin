package com.github.gradle.node.bun.task

import com.github.gradle.AbstractIntegTest
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.IgnoreIf

import static com.github.gradle.node.NodeExtension.DEFAULT_NODE_VERSION

@IgnoreIf({ os.windows })
class BunInstall_integTest extends AbstractIntegTest {
    def 'install packages with bun (#gv.version)'() {
        given:
        gradleVersion = gv

        writeBuild('''
            plugins {
                id 'com.github.node-gradle.node'
            }
        ''')
        writeEmptyPackageJson()

        when:
        def result = build('bunInstall')

        then:
        result.task(":nodeSetup").outcome == TaskOutcome.SKIPPED
        result.task(":bunSetup").outcome == TaskOutcome.SUCCESS
        result.task(":bunInstall").outcome == TaskOutcome.SUCCESS

        when:
        result = build('bunInstall')

        then:
        result.task(":nodeSetup").outcome == TaskOutcome.SKIPPED
        result.task(":bunSetup").outcome == TaskOutcome.UP_TO_DATE
        // because bun.lockb is generated only when needed
        result.task(":bunInstall").outcome == TaskOutcome.UP_TO_DATE

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

    def 'install packages with bun in different directory (#gv.version)'() {
        given:
        gradleVersion = gv
        writeBuild( '''
            plugins {
                id 'com.github.node-gradle.node'
            }

            node {
                download = true
                workDir = file('build/node')
                bunWorkDir = file('build/bundir')
                nodeProjectDir = file('subdirectory')
            }
        ''' )
        writeFile( 'subdirectory/package.json', """{
            "name": "example",
            "dependencies": {}
        }""" )

        when:
        def result = build( 'bunInstall' )

        then:
        result.task( ':bunInstall' ).outcome == TaskOutcome.SUCCESS

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

    def 'verify bun install inputs/outputs (#gv.version)'() {
        given:
        gradleVersion = gv
        writeBuild( '''
            plugins {
                id 'com.github.node-gradle.node'
            }

            node {
                download = true
                workDir = file('build/node')
                npmInstallCommand = 'install'
            }

            def lock = file('bun.lockb')
            def installTask = tasks.named("bunInstall").get()
            def outputs = installTask.outputs.files
            def inputs = installTask.inputs.files
            task verifyIO {
                doLast {
                    if (!outputs.contains(lock)) {
                        throw new RuntimeException("bun.lockb is not in INSTALL'S outputs!")
                    }
                    if (inputs.contains(lock)) {
                        throw new RuntimeException("bun.lockb is in INSTALL'S inputs!")
                    }
                }
            }
        ''' )
        writeEmptyPackageJson()
        writeFile('bun.lockb', '')

        when:
        def result = buildTask( 'verifyIO' )

        then:
        result.outcome == TaskOutcome.SUCCESS

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

    def 'verify output configuration (#gv.version)'() {
        given:
        gradleVersion = gv
        writeBuild('''
            plugins {
                id 'com.github.node-gradle.node'
            }

            node {
                download = true
                workDir = file('build/node')
                bunWorkDir = file('build/bundir')
            }
        ''')
        writePackageJson("""
            {
              "name": "hello",
              "dependencies": {
                "is-number": "7.0.0"
              }
            }
        """)

        when:
        def result1 = build("bunInstall")

        then:
        result1.task(":bunInstall").outcome == TaskOutcome.SUCCESS

        when:
        def result2 = build("bunInstall")

        then:
        // Because bun.lockb was created
        result2.task(":bunInstall").outcome == TaskOutcome.SUCCESS

        when:
        // Let's add a file in the node_modules directory
        writeFile("node_modules/is-number/newFile.txt", "hello")
        def result3 = build("bunInstall")

        then:
        // It should not make the build out-of-date
        result3.task(":bunInstall").outcome == TaskOutcome.UP_TO_DATE

        when:
        // Let's update a file in the node_modules directory
        createFile("node_modules/is-number/README.md").delete()
        writeFile("node_modules/is-number/README.md", "modified README")
        def result4 = build("bunInstall")

        then:
        // This time the build should not be up-to-date and the file could (but it's not) reset
        result4.task(":bunInstall").outcome == TaskOutcome.SUCCESS
        createFile("node_modules/is-number/README.md").text == "modified README"

        when:
        // Let's delete a file in the node_modules directory
        createFile("node_modules/is-number/README.md").delete()
        def result5 = build("bunInstall")

        then:
        // This time the build should not be up-to-date and the file should be reset
        result5.task(":bunInstall").outcome == TaskOutcome.SUCCESS
        createFile("node_modules/is-number/package.json").exists()

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

    def 'verify output configuration when filtering node_modules output (#gv.version)'() {
        given:
        gradleVersion = gv
        writeBuild( '''
            plugins {
                id 'com.github.node-gradle.node'
            }

            node {
                download = true
                workDir = file('build/node')
            }

            bunInstall {
                nodeModulesOutputFilter { 
                    exclude("is-number/package.json")
                }
            }
        ''' )
        writePackageJson("""
            {
              "name": "hello",
              "dependencies": {
                "is-number": "7.0.0"
              }
            }
        """)

        when:
        createFile("node_modules").deleteDir()
        def result1 = build("bunInstall")

        then:
        result1.task(":bunInstall").outcome == TaskOutcome.SUCCESS

        when:
        // Let's add a file in the node_modules directory
        writeFile("node_modules/is-number/newFile.txt", "hello")
        def result2 = build("bunInstall")

        then:
        // It should make the build out-of-date
        result2.task(":bunInstall").outcome == TaskOutcome.SUCCESS

        when:
        // Let's update a file in the node_modules directory
        createFile("node_modules/is-number/README.md").delete()
        writeFile("node_modules/is-number/README.md", "modified README")
        def result3 = build("bunInstall")

        then:
        // It should make the build out-of-date
        result3.task(":bunInstall").outcome == TaskOutcome.SUCCESS

        when:
        // Let's delete an excluded file in the node_modules directory
        createFile("node_modules/is-number/package.json").delete()
        def result4 = build("bunInstall")

        then:
        // The build should still be up-to-date
        result4.task(":bunInstall").outcome == TaskOutcome.UP_TO_DATE

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }
}
