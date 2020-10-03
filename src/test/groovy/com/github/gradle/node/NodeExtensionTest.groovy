package com.github.gradle.node

import com.github.gradle.AbstractProjectTest

import static com.github.gradle.node.NodeExtension.DEFAULT_NODE_VERSION

class NodeExtensionTest extends AbstractProjectTest {
    def "check default values for extension"() {
        when:
        project.apply plugin: 'com.github.node-gradle.node'
        def nodeExtension = NodeExtension.get(project)

        then:
        nodeExtension != null
        nodeExtension.npmCommand.get() == 'npm'
        nodeExtension.npxCommand.get() == 'npx'
        nodeExtension.distBaseUrl.get() == 'https://nodejs.org/dist'
        nodeExtension.workDir.get() != null
        nodeExtension.nodeProjectDir.get() != null
        nodeExtension.version.get() == DEFAULT_NODE_VERSION
        !nodeExtension.download.get()
        nodeExtension.disableTaskRules.get()
        nodeExtension.npmVersion.get() == ''
    }
}
