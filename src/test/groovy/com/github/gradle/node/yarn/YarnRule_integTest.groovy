package com.github.gradle.node.yarn

import com.github.gradle.AbstractIntegTest
import org.gradle.testkit.runner.TaskOutcome

class YarnRule_integTest
    extends AbstractIntegTest
{
    def 'execute yarn_install rule'()
    {
        given:
        writeBuild( '''
            plugins {
                id 'com.github.node-gradle.node'
            }

            node {
                download = true
                workDir = file('build/node')
                yarnWorkDir = file('build/yarn')
            }
        ''' )
        writeEmptyPackageJson()

        when:
        def result = buildTask( 'yarn_install' )

        then:
        result.outcome == TaskOutcome.SUCCESS
    }

    def 'can execute an yarn module using yarn_run_'()
    {
        given:
        writeBuild( '''
            plugins {
                id 'com.github.node-gradle.node'
            }

            node {
                download = true
            }
        ''' )

        copyResources( 'fixtures/yarn-rule/package.json', 'package.json' )

        when:
        def result = buildTask( 'yarn_run_parent' )

        then:
        result.outcome == TaskOutcome.SUCCESS
        fileExists( 'child1.txt' )
        fileExists( 'child2.txt' )
        fileExists( 'parent1.txt' )
        fileExists( 'parent2.txt' )
    }

}
