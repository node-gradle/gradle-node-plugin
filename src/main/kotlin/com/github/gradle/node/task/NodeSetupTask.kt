package com.github.gradle.node.task

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.NodePlugin
import com.github.gradle.node.util.PlatformHelper
import com.github.gradle.node.variant.VariantComputer
import com.github.gradle.node.variant.VariantComputer.Dependency
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

open class NodeSetupTask : DefaultTask() {
    private val variantComputer = VariantComputer()
    private val nodeExtension = NodeExtension[project]

    @get:OutputDirectory
    val nodeDir by lazy {
        variantComputer.computeNodeDir(nodeExtension)
    }

    init {
        group = NodePlugin.NODE_GROUP
        description = "Download and install a local node/npm version."
        isEnabled = false
        project.afterEvaluate {
            isEnabled = nodeExtension.download
        }
    }

    @Input
    fun getInput(): Set<String?> {
        val (archiveDependency, exeDependency) = variantComputer.computeDependency(nodeExtension)
        return setOf(nodeExtension.download.toString(), archiveDependency, exeDependency)
    }

    @TaskAction
    fun exec() {
        addRepositoryIfNeeded()
        val dependency = variantComputer.computeDependency(nodeExtension)
        if (!dependency.exeDependency.isNullOrBlank()) {
            copyNodeExe(dependency)
        }
        deleteExistingNode()
        unpackNodeArchive(dependency)
        setExecutableFlag()
    }

    private fun copyNodeExe(dependency: Dependency) {
        val nodeDir = variantComputer.computeNodeDir(nodeExtension)
        val nodeBinDir = variantComputer.computeNodeBinDir(nodeDir)
        project.copy {
            from(getNodeExeFile(dependency))
            into(nodeBinDir)
            rename("node.*\\.exe", "node.exe")
        }
    }

    private fun deleteExistingNode() {
        project.delete(nodeDir.parent)
    }

    private fun unpackNodeArchive(dependency: Dependency) {
        val nodeArchiveFile = getNodeArchiveFile(dependency)
        val nodeDir = variantComputer.computeNodeDir(nodeExtension)
        val nodeBinDir = variantComputer.computeNodeBinDir(nodeDir)
        if (nodeArchiveFile.name.endsWith("zip")) {
            project.copy {
                from(project.zipTree(nodeArchiveFile))
                into(nodeDir.parent)
            }
        } else if (!dependency.exeDependency.isNullOrBlank()) {
            // Remap lib/node_modules to node_modules (the same directory as node.exe) because that's how the zip dist does it
            project.copy {
                from(project.tarTree(nodeArchiveFile))
                into(nodeBinDir)
                val nodeModulesPattern = Regex("""^.*?[\\/]lib[\\/](node_modules.*$)""")
                eachFile {
                    val file = this
                    val matchResult = nodeModulesPattern.matchEntire(file.path)
                    if (matchResult != null) {
                        // Remap the file to the root
                        file.path = matchResult.groupValues[1]
                    } else {
                        file.exclude()
                    }
                }
                includeEmptyDirs = false
            }
        } else {
            project.copy {
                from(project.tarTree(nodeArchiveFile))
                into(nodeDir.parent)
            }
            // Fix broken symlink
            val npm = Paths.get(nodeBinDir.path, "npm")
            val npmScriptFile = variantComputer.computeNpmScriptFile(nodeDir, "npm")
            if (Files.deleteIfExists(npm)) {
                Files.createSymbolicLink(npm, nodeBinDir.toPath().relativize(Paths.get(npmScriptFile)))
            }
            val npx = Paths.get(nodeBinDir.path, "npx")
            val npxScriptFile = variantComputer.computeNpmScriptFile(nodeDir, "npx")
            if (Files.deleteIfExists(npx)) {
                Files.createSymbolicLink(npx, nodeBinDir.toPath().relativize(Paths.get(npxScriptFile)))
            }
        }
    }

    private fun setExecutableFlag() {
        if (!PlatformHelper.INSTANCE.isWindows) {
            val nodeDir = variantComputer.computeNodeDir(nodeExtension)
            val nodeBinDir = variantComputer.computeNodeBinDir(nodeDir)
            val nodeExec = variantComputer.computeNodeExec(nodeExtension, nodeBinDir)
            File(nodeExec).setExecutable(true)
        }
    }

    private fun getNodeExeFile(dependency: Dependency): File? =
            dependency.exeDependency?.let { resolveSingle(it) }

    private fun getNodeArchiveFile(dependency: Dependency): File =
            resolveSingle(dependency.archiveDependency)

    private fun resolveSingle(name: String): File {
        val dep = project.dependencies.create(name)
        val conf = project.configurations.detachedConfiguration(dep)
        conf.isTransitive = false
        return conf.resolve().single()
    }

    private fun addRepositoryIfNeeded() {
        nodeExtension.distBaseUrl?.let { addRepository(it) }
    }

    private fun addRepository(distUrl: String) {
        project.repositories.ivy {
            setUrl(distUrl)
            patternLayout {
                artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]")
                ivy("v[revision]/ivy.xml")
            }
            metadataSources {
                artifact()
            }
        }
    }

    companion object {
        const val NAME = "nodeSetup"
    }
}
