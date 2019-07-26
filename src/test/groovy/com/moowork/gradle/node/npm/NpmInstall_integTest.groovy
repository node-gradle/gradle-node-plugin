package com.moowork.gradle.node.npm

import com.moowork.gradle.AbstractIntegTest
import org.gradle.testkit.runner.TaskOutcome

class NpmInstall_integTest
    extends AbstractIntegTest
{
    def 'install packages with npm'()
    {
        given:
        writeBuild( '''
            plugins {
                id 'com.github.node-gradle.node'
            }

            node {
                version = "10.14.0"
                npmVersion = "6.4.1"
                download = true
                workDir = file('build/node')
            }
        ''' )
        writeEmptyPackageJson()

        when:
        def result = buildTask( 'npmInstall' )

        then:
        result.outcome == TaskOutcome.SUCCESS

        when:
        result = buildTask( 'npmInstall' )

        then:
        result.outcome == TaskOutcome.SUCCESS

        when:
        result = buildTask( 'npmInstall' )

        then:
        result.outcome == TaskOutcome.UP_TO_DATE
    }

    def 'install packages with npm and postinstall task requiring npm and node'()
    {
        given:
        writeBuild( '''
            plugins {
                id 'com.github.node-gradle.node'
            }
            node {
                version = "10.14.0"
                npmVersion = "6.4.1"
                download = true
                workDir = file('build/node')
            }
        ''' )
        writePackageJson(""" {
            "name": "example",
            "dependencies": {},
            "versionOutput" : "node --version",
            "postinstall" : "npm run versionOutput"
        }
        """)

        when:
        def result = buildTask( 'npmInstall' )

        then:
        result.outcome == TaskOutcome.SUCCESS

        when:
        result = buildTask( 'npmInstall' )

        then:
        result.outcome == TaskOutcome.SUCCESS

        when:
        result = buildTask( 'npmInstall' )

        then:
        result.outcome == TaskOutcome.UP_TO_DATE
    }

    def 'install packages with npm in different directory'()
    {
        given:
        writeBuild( '''
            plugins {
                id 'com.github.node-gradle.node'
            }

            node {
                version = "10.14.0"
                npmVersion = "6.4.1"
                download = true
                workDir = file('build/node')
                nodeModulesDir = file('subdirectory')
            }
        ''' )
        writeFile( 'subdirectory/package.json', """{
            "name": "example",
            "dependencies": {
            }
        }""" )

        when:
        def result = build( 'npmInstall' )

        then:
        result.task( ':npmInstall' ).outcome == TaskOutcome.SUCCESS
    }

    def 'configure npm install through extension'()
    {
        given:
        writeBuild( '''
            plugins {
                id 'com.github.node-gradle.node'
            }

            node {
                version = "10.14.0"
                npmVersion = "6.4.1"
                download = true
                workDir = file('build/node')
                npmInstallCommand = 'ci'
            }
        ''' )
        writeEmptyPackageJson()

        when:
        def result = buildAndFail( 'npmInstall' )

        then:
        result.output.contains('can only install packages with an existing package-lock.json')
        result.task(':npmInstall').outcome == TaskOutcome.FAILED

        when:
        writeEmptyLockFile()
        result = buildTask( 'npmInstall' )

        then:
        result.outcome == TaskOutcome.SUCCESS
    }

    def 'verify npm install inputs/outputs'()
    {
        given:
        writeBuild( '''
            plugins {
                id 'com.github.node-gradle.node'
            }

            node {
                version = "10.14.0"
                npmVersion = "6.4.1"
                download = true
                workDir = file('build/node')
                npmInstallCommand = 'install'
            }

            task verifyIO {
                doLast {
                    if (!tasks.named("npmInstall").get().outputs.files.contains(project.file('package-lock.json'))) {
                        throw new RuntimeException("package-lock.json is not in INSTALL'S outputs!")
                    }
                    if (tasks.named("npmInstall").get().inputs.files.contains(project.file('package-lock.json'))) {
                        throw new RuntimeException("package-lock.json is in INSTALL'S inputs!")
                    }
                }
            }
        ''' )
        writeEmptyPackageJson()
        writeFile('package-lock.json', '')

        when:
        def result = buildTask( 'verifyIO' )

        then:
        result.outcome == TaskOutcome.SUCCESS
    }
    def 'verify npm ci inputs/outputs'()
    {
        given:
        writeBuild( '''
            plugins {
                id 'com.github.node-gradle.node'
            }

            node {
                version = "10.14.0"
                npmVersion = "6.4.1"
                download = true
                workDir = file('build/node')
                npmInstallCommand = 'ci'
            }

            task verifyIO {
                doLast {
                    if (tasks.named("npmInstall").get().outputs.files.contains(project.file('package-lock.json'))) {
                        throw new RuntimeException("package-lock.json is in CI'S outputs!")
                    }
                    if (!tasks.named("npmInstall").get().inputs.files.contains(project.file('package-lock.json'))) {
                        throw new RuntimeException("package-lock.json is not in CI'S inputs!")
                    }
                }
            }
        ''' )
        writeEmptyPackageJson()
        writeEmptyLockFile()

        when:
        def result = buildTask( 'verifyIO' )

        then:
        result.outcome == TaskOutcome.SUCCESS
    }

    protected final void writeEmptyLockFile()
    {
        writeFile('package-lock.json', '''
            {
              "name": "example",
              "lockfileVersion": 1,
              "requires": true,
              "dependencies": {}
            }
        ''')
    }
}
