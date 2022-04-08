package com.github.gradle.node.services

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.NodePlugin
import com.github.gradle.node.services.NodeProvider.Companion.findInstalledNode
import com.github.gradle.node.services.VersionManager.Companion.checkNodeVersion
import com.github.gradle.node.util.PlatformHelper
import com.github.gradle.node.util.zip
import com.github.gradle.node.variant.VariantComputer
import okhttp3.OkHttpClient
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import java.io.File
import java.nio.file.Paths
import java.util.*
import javax.inject.Inject

@Suppress("UnstableApiUsage")
abstract class NodeRuntime
    @Inject
    constructor(providerFactory: ProviderFactory,
                archiveOperations: ArchiveOperations,
                fileSystemOperations: FileSystemOperations) : BuildService<NodeRuntime.Params> {

    interface Params : BuildServiceParameters {
        val gradleUserHome: DirectoryProperty
    }

    private val client = OkHttpClient()

    private val nodeProvider = NodeProvider(archiveOperations, fileSystemOperations)

    private val download = providerFactory.gradleProperty(NodePlugin.DOWNLOAD_PROP)
        .forUseAtConfigurationTime()
        .orElse("true")
        .map { it.toBoolean() }

    private val baseUrl = providerFactory.gradleProperty(NodePlugin.URL_PROP)
        .forUseAtConfigurationTime()
        .orElse(NodePlugin.URL_DEFAULT)

    fun getNode(extension: NodeExtension): File {
        val version = extension.version.get()
        val installed = findInstalledNode(version)
        if (installed.isPresent) {
            return installed.get()
        } else {
            if (!download.get()) {
                throw NodeNotFoundException("No node installation matching requested version: $version found " +
                        "and download is set to false.")
            }
            val dir = getNodeDir(extension)
            nodeProvider.install(client, dir, baseUrl.get(),
                "${dir.name}.${PlatformHelper.INSTANCE.getNodeUrlExtension()}", extension.version.get())
            return if (PlatformHelper.INSTANCE.isWindows)
                File(dir, "node.exe")
            else
                Paths.get(dir.path, "bin", "node").toFile()
        }
    }

    internal fun getNodeDir(extension: NodeExtension): File {
        val variant = VariantComputer()
        return variant.computeNodeDir(zip(parameters.gradleUserHome.dir("nodejs"), extension.version))
            .get().asFile
    }

    fun getNpm(extension: NodeExtension): File {
        val version = "" //extension.npmVersion.get()
        val installed = findInstalledNpm(version)
        if (installed.isPresent) {
            return installed.get()
        } else {
            if (!download.get()) {
                throw NodeNotFoundException("No npm installation matching requested version: $version found " +
                        "and download is set to false.")
            }
            val variant = VariantComputer()
            val nodeDir = variant.computeNodeDir(zip(parameters.gradleUserHome.dir("nodejs"), extension.version))
            return nodeDir.get().asFile
        }
    }

    private fun getNpms(): MutableList<File> {
        val npm = if (PlatformHelper.INSTANCE.isWindows) "npm.cmd" else "npm"
        return PathDetector.findOnPath(npm)
    }

    private fun findInstalledNpm(version: String): Optional<File> {
        return if (version.isBlank()) {
            getNpms().stream()
                .map { file -> Pair(file, checkNodeVersion(file)) }
                .map { pair -> pair.first }
                .findAny()
        } else {
            getNpms().stream()
                .map { file -> Pair(file, checkNodeVersion(file)) }
                .filter { t -> t.second == "v$version" }
                .map { pair -> pair.first }
                .findAny()
        }
    }
}