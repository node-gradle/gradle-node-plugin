package com.github.gradle.node.services

import com.github.gradle.AbstractProjectTest
import com.github.gradle.node.NodeExtension
import com.github.gradle.node.NodePlugin
import org.gradle.api.provider.Provider

class NodeRuntimeTest extends AbstractProjectTest {
    def "Check nodejs paths"() {
        when:
        initializeProject()
        def prop = project.objects.directoryProperty()
        prop.value(layout.dir(layout.buildDirectory.file("nodejs").map {it.asFile }))
        Provider<NodeRuntime> runtime = runtimeProvider(prop)
        def ext = new NodeExtension(project)
        ext.version.set("10.10.10")

        then:
        runtime.get().getNodeDir$gradle_node_plugin(ext).parent == prop.file("nodejs").get().getAsFile().path
    }

    def "non-installed version with download disabled"() {
        when:
        addProperty(NodePlugin.DOWNLOAD_PROP, "false")
        initializeProject()
        Provider<NodeRuntime> runtime = runtimeProvider()
        def ext = new NodeExtension(project)
        ext.version.set("100000")
        runtime.get().getNode(ext)

        then:
        thrown NodeNotFoundException
    }

    @SuppressWarnings('GroovyAssignabilityCheck')
    Provider<NodeRuntime> runtimeProvider(Object home=project.gradle.gradleUserHomeDir) {
        return project.gradle.sharedServices
                .registerIfAbsent("nodeRuntime", NodeRuntime.class) {
                    it.parameters.gradleUserHome.set(home)
                }
    }
}