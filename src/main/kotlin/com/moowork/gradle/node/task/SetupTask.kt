package com.moowork.gradle.node.task

import com.moowork.gradle.node.NodeExtension
import com.moowork.gradle.node.NodePlugin
import com.moowork.gradle.node.util.Alias
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

open class SetupTask : DefaultTask() {

    private val config = NodeExtension[project]
    private val variant by lazy { config.variant }

    @get:OutputDirectory
    protected val nodeDir by Alias { variant::nodeDir }

    init {
        group = NodePlugin.NODE_GROUP
        description = "Download and install a local node/npm version."
        isEnabled = false
    }

    @Input
    fun getInput(): Set<String?> {
        return setOf(config.download.toString(), variant.archiveDependency, variant.exeDependency)
    }

    @TaskAction
    fun exec() {
        addRepositoryIfNeeded()
        if (!variant.exeDependency.isNullOrBlank()) {
            copyNodeExe()
        }
        deleteExistingNode()
        unpackNodeArchive()
        setExecutableFlag()
    }

    private fun copyNodeExe() {
        project.copy {
            from(getNodeExeFile())
            into(variant.nodeBinDir)
            rename("node.*\\.exe", "node.exe")
        }
    }

    private fun deleteExistingNode() {
        project.delete(nodeDir.parent)
    }

    private fun unpackNodeArchive() {
        if (getNodeArchiveFile().name.endsWith("zip")) {
            project.copy {
                from(project.zipTree(getNodeArchiveFile()))
                into(nodeDir.parent)
            }
        } else if (!variant.exeDependency.isNullOrBlank()) {
            // Remap lib/node_modules to node_modules (the same directory as node.exe) because that's how the zip dist does it
            project.copy {
                from(project.tarTree(getNodeArchiveFile()))
                into(variant.nodeBinDir)
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
                from(project.tarTree(getNodeArchiveFile()))
                into(nodeDir.parent)
            }
            // Fix broken symlink
            val npm = Paths.get(variant.nodeBinDir.path, "npm")
            if (Files.deleteIfExists(npm)) {
                Files.createSymbolicLink(npm, Paths.get(variant.npmScriptFile))
            }
            val npx = Paths.get(variant.nodeBinDir.path, "npx")
            if (Files.deleteIfExists(npx)) {
                Files.createSymbolicLink(npx, Paths.get(variant.npxScriptFile))
            }
        }
    }

    private fun setExecutableFlag() {
        if (!variant.isWindows) {
            File(variant.nodeExec).setExecutable(true)
        }
    }

    private fun getNodeExeFile(): File? = variant.exeDependency?.let { resolveSingle(it) }

    private fun getNodeArchiveFile(): File = resolveSingle(variant.archiveDependency)

    private fun resolveSingle(name: String): File {
        val dep = project.dependencies.create(name)
        val conf = project.configurations.detachedConfiguration(dep)
        conf.isTransitive = false
        return conf.resolve().single()
    }

    private fun addRepositoryIfNeeded() {
        config.distBaseUrl?.let { addRepository(it) }
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
