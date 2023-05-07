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

    // Windows lacks uname
    @IgnoreIf({ System.getProperty("os.name").toLowerCase().contains("windows") })
    def 'ensure configuration-cache works with uname (#gv.version)'() {
        given:
        gradleVersion = gv

        copyResources("fixtures/node")
        createFile("gradle.properties") << """
systemProp.os.arch=aarch64
systemProp.os.name=Mac OS X
"""
        createFile("build.gradle") << '''
def nodeExtension = com.github.gradle.node.NodeExtension.get(project)
def versionSource = project.providers.of(com.github.gradle.node.util.NodeVersionSource) {
            parameters.nodeVersion.set(nodeExtension.version)
        }
tasks.register('getVersionFromSource') {
    def version = versionSource.get()
    doLast {
        println "Got version: '${version.get()}'"
    }
}
'''

        when:
        def result1 = build("getVersionFromSource")

        then:
        result1.task(":getVersionFromSource").outcome == TaskOutcome.SUCCESS
        result1.output.contains("Got version: 'org.nodejs:node:16.14.2:")

        when:
        def result2 = build("getVersionFromSource")

        then:
        result2.task(":getVersionFromSource").outcome == TaskOutcome.SUCCESS
        result2.output.contains("Got version: 'org.nodejs:node:16.14.2:")

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

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

}
