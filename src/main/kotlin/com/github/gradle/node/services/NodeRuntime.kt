package com.github.gradle.node.services

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.NodePlugin
import com.github.gradle.node.services.NodeProvisioner.Companion.findInstalledNode
import com.github.gradle.node.services.VersionManager.Companion.checkNpmVersion
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

    private val nodeProvider = NodeProvisioner(archiveOperations, fileSystemOperations)

    private val download = providerFactory.gradleProperty(NodePlugin.DOWNLOAD_PROP)
        .forUseAtConfigurationTime()
        .orElse("true")
        .map { it.toBoolean() }

    private val baseUrl = providerFactory.gradleProperty(NodePlugin.URL_PROP)
        .forUseAtConfigurationTime()
        .orElse(NodePlugin.URL_DEFAULT)

    fun getNode(extension: NodeExtension): File {
        return getNode(extension, download.get(), baseUrl.get())
    }
    fun getNode(extension: NodeExtension, download: Boolean, baseUrl: String): File {
        val version = extension.version.get()
        val installed = findInstalledNode(version)
        if (installed.isPresent) {
            return installed.get()
        } else {
            if (!download) {
                throw NodeNotFoundException("No node installation matching requested version: $version found " +
                        "and download is set to false.")
            }
            val dir = getNodeDir(extension)
            nodeProvider.install(client, dir, baseUrl,
                "${dir.name}.${PlatformHelper.INSTANCE.getNodeUrlExtension()}", version)
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

    //TODO: this is broken, returns the wrong path
    fun getNpm(extension: NodeExtension): File {
        val version = extension.npmVersion.get()
        val installed = findInstalledNpm(version)
        if (installed.isPresent) {
            return installed.get()
        } else {
            if (!download.get()) {
                throw NpmNotFoundException("No npm installation matching requested version: $version found " +
                        "and download is set to false.")
            }
            val variant = VariantComputer()
            val npmDir = variant.computeNpmDir(zip(extension.npmVersion, parameters.gradleUserHome.dir("nodejs"),
                parameters.gradleUserHome.dir("nodejs"))) // TODO: temporary fix to allow compilation
            return npmDir.get().asFile
        }
    }

    private fun getNpms(): MutableList<File> {
        val npm = if (PlatformHelper.INSTANCE.isWindows) "npm.cmd" else "npm"
        return PathDetector.findOnPath(npm)
    }

    private fun findInstalledNpm(version: String): Optional<File> {
        return if (version.isBlank()) {
            getNpms().stream()
                .map { file -> Pair(file, checkNpmVersion(file)) }
                .map { pair -> pair.first }
                .findAny()
        } else {
            getNpms().stream()
                .map { file -> Pair(file, checkNpmVersion(file)) }
                .filter { t -> t.second.startsWith(version) }
                .map { pair -> pair.first }
                .findAny()
        }
    }
}