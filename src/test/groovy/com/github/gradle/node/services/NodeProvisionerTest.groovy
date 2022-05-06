package com.github.gradle.node.services

import com.github.gradle.AbstractProjectTest
import com.github.gradle.node.NodePlugin
import okhttp3.OkHttpClient
import org.gradle.api.provider.Provider

class NodeProvisionerTest extends AbstractProjectTest {
    def "download node"() {
        when:
        initializeProject()
        def version = "16.13.1"
        def fileName = "node-v$version-win-x64"
        def out = new File(projectDir, fileName)
        NodeProvisioner provider = new NodeProvisioner(project.objects.newInstance(NodeProvisioner.Data))
        def download = provider.download$gradle_node_plugin(out,
                NodePlugin.URL_DEFAULT, fileName+".zip", version, new OkHttpClient())

        then:
        download.exists()
        download.size() == 25689871
    }

    def "install node"() {
        when:
        initializeProject()
        def version = "16.13.1"
        def fileName = "node-v$version-win-x64"
        def out = new File(projectDir, fileName)
        NodeProvisioner provider = new NodeProvisioner(project.objects.newInstance(NodeProvisioner.Data))
        provider.install$gradle_node_plugin(new OkHttpClient(), out, NodePlugin.URL_DEFAULT, fileName+".zip", version)

        then:
        def nodeExe = new File(out, "node.exe")
        nodeExe.exists()
        nodeExe.size() == 59385480
    }

    @SuppressWarnings('GroovyAssignabilityCheck')
    Provider<NodeRuntime> runtimeProvider(Object home=project.gradle.gradleUserHomeDir) {
        return project.gradle.sharedServices
                .registerIfAbsent("nodeRuntime", NodeRuntime.class) {
                    it.parameters.gradleUserHome.set(home)
                }
    }
}