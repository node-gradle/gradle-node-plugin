package com.moowork.gradle.node.task

import com.moowork.gradle.AbstractIntegTest
import org.gradle.testkit.runner.TaskOutcome

class Setup_integTest
    extends AbstractIntegTest
{
    private final static OS_NAME = System.getProperty( 'os.name' )

    def cleanup()
    {
        System.setProperty( 'os.name', OS_NAME )
    }

    def 'setup node'()
    {
        given:
        writeBuild( '''
            plugins {
                id 'com.github.node-gradle.node'
            }

            node {
                version = "10.14.0"
                download = false
            }
        ''' )

        when:
        def result = buildTask( 'nodeSetup' )

        then:
        result.outcome == TaskOutcome.SKIPPED
    }

    def 'setup node (download)'()
    {
        given:
        writeBuild( '''
            plugins {
                id 'com.github.node-gradle.node'
            }

            node {
                version = "10.14.0"
                download = true
            }
        ''' )

        when:
        def result = buildTask( 'nodeSetup' )

        then:
        result.outcome == TaskOutcome.SUCCESS

        when:
        result = buildTask( 'nodeSetup' )

        then:
        result.outcome == TaskOutcome.UP_TO_DATE
    }

    def 'setup node (windows)'()
    {
        System.setProperty( 'os.name', 'Windows' )

        given:
        writeBuild( '''
            plugins {
                id 'com.github.node-gradle.node'
            }

            node {
                version = "10.14.0"
                download = false
            }
        ''' )

        when:
        def result = buildTask( 'nodeSetup' )

        then:
        result.outcome == TaskOutcome.SKIPPED
    }

    def 'setup node (windows download)'()
    {
        System.setProperty( 'os.name', 'Windows' )

        given:
        writeBuild( '''
            plugins {
                id 'com.github.node-gradle.node'
            }

            node {
                version = "10.14.0"
                download = true
            }
        ''' )

        when:
        def result = buildTask( 'nodeSetup' )

        then:
        result.outcome == TaskOutcome.SUCCESS
    }

    def 'setup node (windows download separate exe)'()
    {
        System.setProperty( 'os.name', 'Windows' )

        given:
        writeBuild( '''
            plugins {
                id 'com.github.node-gradle.node'
            }

            node {
                version = "10.14.0"
                download = true
            }
        ''' )

        when:
        def result = buildTask( 'nodeSetup' )

        then:
        result.outcome == TaskOutcome.SUCCESS
    }

    def 'add repository by default'()
    {
        given:
        writeBuild( '''
            plugins {
                id 'com.github.node-gradle.node'
            }

            repositories {
                mavenLocal()
            }

            node {
                version = "10.14.0"
                download = true
            }
            
            task verifyRepo {
                dependsOn nodeSetup
                doLast {
                    if (project.repositories.size() != 2) {
                        throw new RuntimeException("Expected exactly 2 repositories, found ${project.repositories.size()}.")
                    }
                }
            }
        ''' )

        when:
        def result = buildTask( 'verifyRepo' )

        then:
        result.outcome == TaskOutcome.SUCCESS
    }

    def "don't add repository if url is null"()
    {
        given:
        writeBuild( '''
            plugins {
                id 'com.github.node-gradle.node'
            }
            
            project.repositories.ivy {
                url 'https://nodejs.org/dist'
                patternLayout {
                    artifact 'v[revision]/[artifact](-v[revision]-[classifier]).[ext]'
                    ivy 'v[revision]/ivy.xml'
                }
                metadataSources {
                    artifact()
                }
            }

            node {
                version = "10.14.0"
                download = true
                distBaseUrl = null
            }
            
            task verifyRepo {
                dependsOn nodeSetup
                doLast {
                    if (project.repositories.size() != 1) {
                        throw new RuntimeException("Expected exactly 1 repositories, found ${project.repositories.size()}.")
                    }
                }
            }
        ''' )

        when:
        def result = buildTask( 'verifyRepo' )

        then:
        result.outcome == TaskOutcome.SUCCESS
    }
}
