package com.github.gradle.node

import com.github.gradle.AbstractProjectTest
import com.github.gradle.node.util.PlatformHelper

class NodePluginTest extends AbstractProjectTest {
    private Properties props

    def setup() {
        props = new Properties()
        PlatformHelper.INSTANCE = new PlatformHelper(props)
    }

    def 'check default tasks'() {
        when:
        project.apply plugin: 'com.github.node-gradle.node'
        project.evaluate()

        then:
        project.extensions.getByName('node')
        project.tasks.getByName('nodeSetup')
        project.tasks.getByName('npmInstall')
        project.tasks.getByName('npmSetup')
    }

    def 'check repository and dependencies (no download)'() {
        when:
        project.apply plugin: 'com.github.node-gradle.node'
        project.evaluate()

        then:
        project.repositories.size() == 0
        !project.configurations.contains('nodeDist')
    }
}
