package com.github.gradle.node.pnpm.task

import com.github.gradle.AbstractIntegTest
import com.github.gradle.node.Versions
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Ignore

import java.util.regex.Pattern

class PnpmRule_integTest extends AbstractIntegTest {
    def 'execute pnpm_install rule (#gv.version)'()
    {
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
        ''' )
        writeEmptyPackageJson()

        when:
        def result = buildTask( 'pnpm_install' )

        then:
        result.outcome == TaskOutcome.SUCCESS

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

    def 'Use downloaded pnpm version (#gv.version)'()
    {
        given:
        gradleVersion = gv
        writeBuild( """
            plugins {
                id 'com.github.node-gradle.node'
            }
            node {
                download = true
                pnpmVersion = '${Versions.TEST_PNPM_DOWNLOAD_VERSION}'
            }
        """ )
        writeEmptyPackageJson()

        when:
        def result = build( 'pnpm_--version' )

        then:
        result.output =~ Versions.TEST_PNPM_DOWNLOAD_REGEX
        result.task( ':pnpm_--version' ).outcome == TaskOutcome.SUCCESS

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

    @Ignore("https://github.com/node-gradle/gradle-node-plugin/issues/270")
    def 'Use local pnpm installation (#gv.version)'()
    {
        given:
        gradleVersion = gv
        writeBuild( """
            plugins {
                id 'com.github.node-gradle.node'
            }
            node {
                download = true
                pnpmVersion = '${Versions.TEST_PNPM_DOWNLOAD_VERSION}'
            }
        """ )
        writeEmptyPackageJson()

        when:
        build( "pnpm_install_pnpm@${Versions.TEST_PNPM_LOCAL_VERSION}" )
        def result = build( 'pnpm_--version' )

        then:
        result.output =~ Versions.TEST_PNPM_LOCAL_REGEX
        result.task( ':pnpm_--version' ).outcome == TaskOutcome.SUCCESS

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

    def 'can execute an pnpm module using pnpm_run_ (#gv.version)'()
    {
        given:
        gradleVersion = gv
        writeBuild( '''
            plugins {
                id 'com.github.node-gradle.node'
            }

            node {
                download = true
            }
        ''' )

        copyResources( 'fixtures/npm-missing/package.json', 'package.json' )

        when:
        def result = buildTask( 'pnpm_run_echoTest' )

        then:
        result.outcome == TaskOutcome.SUCCESS
        fileExists( 'test.txt' )

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

    def 'succeeds to run pnpm module using pnpm_run_ when the package.json file contains local pnpm (#gv.version)'()
    {
        given:
        gradleVersion = gv
        writeBuild( '''
            plugins {
                id 'com.github.node-gradle.node'
            }

            node {
                download = true
            }
        ''' )

        copyResources( 'fixtures/pnpm-present/' )

        when:
        def result = build( 'pnpm_run_pnpmVersion' )

        then:
        result.task(":pnpmInstall").outcome == TaskOutcome.SUCCESS
        result.task(":pnpm_run_pnpmVersion").outcome == TaskOutcome.SUCCESS
        def versionPattern = Pattern.compile(".*Version\\s+${Versions.TEST_PNPM_LOCAL_VERSION}.*", Pattern.DOTALL)
        versionPattern.matcher(result.output).find()

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

    def 'can execute subtasks using pnpm (#gv.version)'()
    {
        given:
        gradleVersion = gv
        writeBuild( '''
            plugins {
                id 'com.github.node-gradle.node'
            }
            node {
                download = true
            }
        ''' )
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
        def result = buildTask( 'pnpm_run_parent' )

        then:
        result.outcome == TaskOutcome.SUCCESS
        fileExists( 'parent1.txt' )
        fileExists( 'child1.txt' )
        fileExists( 'child2.txt' )
        fileExists( 'parent2.txt' )

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

    def 'Custom workingDir (#gv.version)'()
    {
        given:
        gradleVersion = gv
        writeBuild( """
            plugins {
                id 'com.github.node-gradle.node'
            }
            node {
                download = true
                pnpmVersion = '${Versions.TEST_PNPM_DOWNLOAD_VERSION}'
                nodeProjectDir = file("frontend")
            }
        """ )
        writeFile( 'frontend/package.json', """{
            "name": "example",
            "dependencies": {},
            "scripts": {
                "whatVersion": "pnpm --version"
            }
        }""" )

        when:
        def result = build( 'pnpm_run_whatVersion' )

        then:
        result.output =~ Versions.TEST_PNPM_DOWNLOAD_REGEX
        result.task( ':pnpm_run_whatVersion' ).outcome == TaskOutcome.SUCCESS

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }
}
