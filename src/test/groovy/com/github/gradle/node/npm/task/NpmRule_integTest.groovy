package com.github.gradle.node.npm.task

import com.github.gradle.AbstractIntegTest
import com.github.gradle.node.NodeExtension
import org.gradle.testkit.runner.TaskOutcome

import java.util.regex.Pattern

class NpmRule_integTest extends AbstractIntegTest {
    def 'execute npm_install rule (#gv.version)'() {
        given:
        gradleVersion = gv
        writeBuild('''
            plugins {
                id 'com.github.node-gradle.node'
            }
        ''')
        writeEmptyPackageJson()

        when:
        def result = buildTask('npm_install')

        then:
        result.outcome == TaskOutcome.SUCCESS

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

   def 'can configure npm_ rule task (#gv.version)'() {
       given:
       gradleVersion = gv

       writeBuild('''
           plugins {
               id 'com.github.node-gradle.node'
           }

           npm_run_build {
               doFirst { project.logger.info('configured') }
           }
       ''')
       writeEmptyPackageJson()

       when:
       def result = buildTask('help')

       then:
       result.outcome == TaskOutcome.SUCCESS

       where:
       gv << GRADLE_VERSIONS_UNDER_TEST
   }

    def 'can execute an npm module using npm_run_ (#gv.version)'() {
        given:
        gradleVersion = gv

        writeBuild('''
            plugins {
                id 'com.github.node-gradle.node'
            }
        ''')

        copyResources("fixtures/npm-missing/package.json", "package.json")

        when:
        def result = buildTask('npm_run_echoTest')

        then:
        result.outcome == TaskOutcome.SUCCESS
        fileExists('test.txt')

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

    def 'succeeds to run npm module using npm_run_ when the package.json file contains local npm (#gv.version)'() {
        given:
        gradleVersion = gv

        writeBuild('''
            plugins {
                id 'com.github.node-gradle.node'
            }
        ''')

        copyResources("fixtures/npm-present/")

        when:
        def result = build('npm_run_npmVersion')

        then:
        result.task(":npmInstall").outcome == TaskOutcome.SUCCESS
        result.task(":npm_run_npmVersion").outcome == TaskOutcome.SUCCESS
        def versionPattern = Pattern.compile(".*Version\\s+${NodeExtension.DEFAULT_NPM_VERSION}.*", Pattern.DOTALL)
        versionPattern.matcher(result.output).find()

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }
}
