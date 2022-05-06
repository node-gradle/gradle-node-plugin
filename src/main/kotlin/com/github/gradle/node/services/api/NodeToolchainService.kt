package com.github.gradle.node.services.api

import org.gradle.api.provider.Provider

interface NodeToolchainService {
    fun forVersion(version: String): Provider<Node>
}