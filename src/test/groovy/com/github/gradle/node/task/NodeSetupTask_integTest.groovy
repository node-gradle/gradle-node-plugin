package com.github.gradle.node.task

import com.github.gradle.AbstractIntegTest
import com.github.gradle.node.PackageJsonExtension
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.util.GradleVersion
import org.junit.Assume
import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables
import spock.lang.IgnoreIf

import static com.github.gradle.node.NodeExtension.DEFAULT_NODE_VERSION

class NodeSetupTask_integTest extends AbstractIntegTest {
    @Rule
    EnvironmentVariables environmentVariables = new EnvironmentVariables()

    @IgnoreIf({ System.getProperty("os.name").toLowerCase().contains("windows") })
    def 'ensure configuration-cache works with nodeSetup (#gv.version)'() {
        given:
        gradleVersion = gv

        copyResources("fixtures/node")
        createFile("gradle.properties") << """
systemProp.os.arch=aarch64
systemProp.os.name="Mac OS X"
"""
        createFile("build.gradle") << '''
node {
    download = false
}
'''

        when:
        def result1 = build("nodeSetup")

        then:
        result1.task(":nodeSetup").outcome == TaskOutcome.SKIPPED

        when:
        def result2 = build("nodeSetup")

        then:
        result2.task(":nodeSetup").outcome == TaskOutcome.SKIPPED

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

    def 'nodeSetup should only delete old node versions (#gv.version)'() {
        given:
        gradleVersion = gv

        def badVersion = "node-v18.17.0"
        def goodVersion = "18.17.1"
        createFile("build.gradle") << """plugins {
            id "com.github.node-gradle.node"
        }

        node {
            version = "${goodVersion}"
            download = true
            workDir = file("build/node")
        }"""
        def badFile = writeFile("build/node/$badVersion/bin/node.js", "console.log(\"bad\");")
        def goodFile = writeFile("build/node/important.txt", "should not be deleted by nodeSetup")

        when:
        def result1 = build("nodeSetup")

        then:
        result1.task(":nodeSetup").outcome == TaskOutcome.SUCCESS
        !badFile.exists()
        goodFile.exists()

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

}
