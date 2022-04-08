package com.github.gradle.node.services

import com.github.gradle.node.services.VersionManager.Companion.checkNodeVersion
import com.github.gradle.node.util.PlatformHelper
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.FileSystemOperations
import java.io.File
import java.io.IOException
import java.nio.file.Paths
import java.util.*
import javax.inject.Inject

@Suppress("UnstableApiUsage")
class NodeProvider(private val archiveOperations: ArchiveOperations,
                   private val fileSystemOperations: FileSystemOperations
) {

    open class Data @Inject constructor(val archiveOperations: ArchiveOperations,
                                        val fileSystemOperations: FileSystemOperations)

    constructor(data: Data) : this(data.archiveOperations, data.fileSystemOperations)

    @Synchronized
    internal fun install(client: OkHttpClient, dir: File, url: String, fileName: String, version: String) {
        val download = download(dir, url, fileName, version, client)
        unpack(fileName, download, dir)
        download.deleteOnExit()
    }

    internal fun unpack(fileName: String, download: File, dir: File) {
        fileSystemOperations.copy {
            if (fileName.endsWith(".zip")) {
                from(archiveOperations.zipTree(download))
            } else {
                from(archiveOperations.tarTree(download))
            }
            into(dir.parentFile)
        }
    }

    internal fun download(
        dir: File,
        url: String,
        fileName: String,
        version: String,
        client: OkHttpClient
    ): File {
        val tmp = Paths.get(dir.parentFile.path, ".node-tmp", fileName).toFile()
        tmp.parentFile.mkdirs()

        val request = Request.Builder()
            .url("$url/v$version/$fileName")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val rsp = response.body ?: throw IOException("No response body")
            val sink = tmp.sink().buffer()
            sink.writeAll(rsp.source())
            sink.close()
        }
        return tmp
    }

    companion object {
        private fun getNodes(): MutableList<File> {
            val node = if (PlatformHelper.INSTANCE.isWindows) "node.exe" else "node"
            return PathDetector.findOnPath(node)
        }

        fun findInstalledNode(version: String): Optional<File> {
            return if (version.isBlank()) {
                getNodes().stream()
                    .map { file -> Pair(file, checkNodeVersion(file)) }
                    .map { pair -> pair.first }
                    .findAny()
            } else {
                getNodes().stream()
                    .map { file -> Pair(file, checkNodeVersion(file)) }
                    .filter { t -> t.second == "v$version" }
                    .map { pair -> pair.first }
                    .findAny()
            }
        }
    }
}