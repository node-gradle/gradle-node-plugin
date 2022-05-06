package com.github.gradle.node.services

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.services.api.Node
import com.github.gradle.node.services.api.NodeInstallationMetadata
import com.github.gradle.node.services.api.NodeToolchainService
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import java.io.File

class NodeToolchainServiceImpl(private val runtime: Provider<NodeRuntime>,
                               private val providers: ProviderFactory,
                               private val extension: NodeExtension): NodeToolchainService {

    override fun forVersion(version: String): Provider<Node> {
        return providers.provider {
            NodeImpl(runtime.get().getNode(extension), version)
        }
    }
}

class NodeImpl(val path: File, private val version: String) : Node, NodeInstallationMetadata {
    override fun getMetadata(): NodeInstallationMetadata {
        return this
    }

    override fun getFile(): File {
        return path
    }

    override fun getVersion(): String {
        return version
    }
}