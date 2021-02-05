package com.github.gradle.node

import com.github.gradle.AbstractProjectTest

class NodePluginTest extends AbstractProjectTest {
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

    def 'check disabled task rules'()
    {
        when:
        this.project.apply plugin: 'com.github.node-gradle.node'
        this.project.node.disableTaskRules = true
        this.project.evaluate()

        then:
        this.project.tasks.getRules().size() == 0
        //test for the lack of rule driven task
        this.project.tasks.findByName("npm_run_install") == null

    }
}
