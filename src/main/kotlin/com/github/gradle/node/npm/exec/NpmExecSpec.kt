package com.github.gradle.node.npm.exec

import com.github.gradle.node.npm.proxy.ProxySettings
import com.github.gradle.node.util.Platform
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property

/**
 * Configurable options for executing `npm`.
 *
 * Intended for use in buildscripts by users
 */
abstract class NpmExecSpec internal constructor() {
    abstract val arguments: ListProperty<String>
    abstract val environment: MapProperty<String, String>
    abstract val includeSystemEnvironment: Property<Boolean>
//    abstract val additionalBinPaths: ListProperty<String>

    /** @see com.github.gradle.node.NodeExtension.download */
    abstract val download: Property<Boolean>

    /** @see com.github.gradle.node.NodeExtension.resolvedNodeDir */
    abstract val resolvedNodeDir: DirectoryProperty

    /** @see com.github.gradle.node.NodeExtension.resolvedPlatform */
    abstract val resolvedPlatform: Property<Platform>

    /** @see com.github.gradle.node.NodeExtension.npmVersion */
    abstract val npmVersion: Property<String>

//    /** @see com.github.gradle.node.NodeExtension.npmCommand */
//    abstract val npmCommand: Property<String>

    /** @see com.github.gradle.node.NodeExtension.npmWorkDir */
    abstract val npmWorkDir: DirectoryProperty

    /** @see com.github.gradle.node.NodeExtension.nodeProjectDir */
    abstract val nodeProjectDir: DirectoryProperty


    /** @see com.github.gradle.node.NodeExtension.nodeProxySettings */
    abstract val nodeProxySettings: Property<ProxySettings>
}
