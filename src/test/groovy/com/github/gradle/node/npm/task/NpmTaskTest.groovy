package com.github.gradle.node.npm.task


import com.github.gradle.node.npm.proxy.GradleProxyHelper
import com.github.gradle.node.npm.proxy.ProxySettings
import com.github.gradle.node.task.AbstractTaskTest
import com.github.gradle.node.util.PlatformHelperKt

import java.nio.file.Files

import static com.github.gradle.node.NodeExtension.DEFAULT_NODE_VERSION

class NpmTaskTest extends AbstractTaskTest {
    def cleanup() {
        GradleProxyHelper.resetProxy()
    }

    /**
     * Creates a temporary directory with a dummy executable file.
     *
     * @param fileName The name of the dummy executable (e.g., "npm").
     * @param scriptContent (Optional) The content inside the executable. Defaults to a simple echo script.
     * @return File object pointing to the dummy executable.
     */
    File createTempDummyExecutable(String fileName, String scriptContent = "#!/bin/bash\necho 'Dummy executable ran with args: \$@'") {
        // Create a temporary directory
        File tempDir = Files.createTempDirectory("dummy-bin").toFile()

        // Create the dummy executable inside the temp directory
        File executableFile = new File(tempDir, fileName)

        // Write the script content
        executableFile.text = scriptContent

        // Set executable permissions
        executableFile.setExecutable(true)

        return executableFile
    }

    def "exec npm task with environment variable"() {
        given:
        def npmFile = createTempDummyExecutable("npm")

        nodeExtension.resolvedPlatform.set(PlatformHelperKt.parsePlatform("Linux", "x86_64", {}))
        nodeExtension.environment.set(["PATH": npmFile.parent])

        def task = project.tasks.create('simple', NpmTask)
        mockProjectApiHelperExec(task)
        task.args.set(['a', 'b'])
        task.environment.set(['a': '1'])
        task.ignoreExitValue.set(true)
        task.workingDir.set(projectDir)

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setIgnoreExitValue(true)
        1 * execSpec.setEnvironment({ it['a'] == '1' && containsPath(it) })
        1 * execSpec.setExecutable(npmFile.absolutePath)
        1 * execSpec.setArgs(['a', 'b'])
    }

    def "exec npm task with environment variable (windows)"() {
        given:
        def npmFile = createTempDummyExecutable("npm.cmd")

        nodeExtension.resolvedPlatform.set(PlatformHelperKt.parsePlatform("Windows", "x86_64", {}))
        nodeExtension.environment.set(["Path": npmFile.parent])

        def task = project.tasks.create('simple', NpmTask)
        mockProjectApiHelperExec(task)
        task.args.set(['a', 'b'])
        task.environment.set(['a': '1'])
        task.ignoreExitValue.set(true)
        task.workingDir.set(projectDir)

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setIgnoreExitValue(true)
        1 * execSpec.setEnvironment({ it['a'] == '1' && containsPath(it) })
        1 * execSpec.setExecutable(npmFile.absolutePath)
        1 * execSpec.setArgs(['a', 'b'])
    }

    def "exec npm task"() {
        given:
        nodeExtension.resolvedPlatform.set(PlatformHelperKt.parsePlatform("Linux", "x86_64", {}))
        nodeExtension.environment.set([:])

        def task = project.tasks.create('simple', NpmTask)
        mockProjectApiHelperExec(task)
        task.args.set(['a', 'b'])
        task.environment.set(['a': '1'])
        task.ignoreExitValue.set(true)
        task.workingDir.set(projectDir)

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setIgnoreExitValue(true)
        1 * execSpec.setEnvironment({ it['a'] == '1' && containsPath(it) })
        1 * execSpec.setExecutable('npm')
        1 * execSpec.setArgs(['a', 'b'])
    }

    def "exec npm task (windows)"() {
        given:
        nodeExtension.resolvedPlatform.set(PlatformHelperKt.parsePlatform("Windows", "x86_64", {}))

        def task = project.tasks.create('simple', NpmTask)
        mockProjectApiHelperExec(task)
        task.args.set(['a', 'b'])
        task.environment.set(['a': '1'])
        task.ignoreExitValue.set(true)
        task.workingDir.set(projectDir)

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setIgnoreExitValue(true)
        1 * execSpec.setEnvironment({ it['a'] == '1' && containsPath(it) })
        1 * execSpec.setExecutable('npm.cmd')
        1 * execSpec.setArgs(['a', 'b'])
    }

    def "exec npm task with path respects download"() {
        given:
        def npmFile = createTempDummyExecutable("npm")

        nodeExtension.resolvedPlatform.set(PlatformHelperKt.parsePlatform("Linux", "x86_64", {}))
        nodeExtension.environment.set(["PATH": npmFile.parent])
        nodeExtension.download.set(true)

        def nodeDir = projectDir.toPath().resolve(".gradle").resolve("nodejs")
                .resolve("node-v${DEFAULT_NODE_VERSION}-linux-x64")

        def task = project.tasks.create('simple', NpmTask)
        mockProjectApiHelperExec(task)
        task.args.set(["run", "command"])

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setIgnoreExitValue(false)
        1 * execSpec.setExecutable({ executable ->
            def expectedNodePath = nodeDir.resolve("bin").resolve("node")
            return fixAbsolutePath(executable) == expectedNodePath.toAbsolutePath().toString()
        })
        1 * execSpec.setArgs({ args ->
            def command = nodeDir
                    .resolve("lib").resolve("node_modules").resolve("npm").resolve("bin")
                    .resolve("npm-cli.js").toAbsolutePath().toString()
            return fixAbsolutePaths(args) == [command, "run", "command"]
        })
    }

    def "exec npm task (download)"() {
        given:
        nodeExtension.resolvedPlatform.set(PlatformHelperKt.parsePlatform("Linux", "x86_64", {}))
        nodeExtension.download.set(true)
        def nodeDir = projectDir.toPath().resolve(".gradle").resolve("nodejs")
                .resolve("node-v${DEFAULT_NODE_VERSION}-linux-x64")

        def task = project.tasks.create('simple', NpmTask)
        mockProjectApiHelperExec(task)
        task.args.set(["run", "command"])

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setIgnoreExitValue(false)
        1 * execSpec.setExecutable({ executable ->
            def expectedNodePath = nodeDir.resolve("bin").resolve("node")
            return fixAbsolutePath(executable) == expectedNodePath.toAbsolutePath().toString()
        })
        1 * execSpec.setArgs({ args ->
            def command = nodeDir
                    .resolve("lib").resolve("node_modules").resolve("npm").resolve("bin")
                    .resolve("npm-cli.js").toAbsolutePath().toString()
            return fixAbsolutePaths(args) == [command, "run", "command"]
        })
    }

    def "exec npm task (download) with configured proxy"() {
        given:
        nodeExtension.resolvedPlatform.set(PlatformHelperKt.parsePlatform("Linux", "x86_64", {}))
        nodeExtension.download.set(true)
        def nodeDir = projectDir.toPath().resolve(".gradle").resolve("nodejs")
                .resolve("node-v${DEFAULT_NODE_VERSION}-linux-x64")
        GradleProxyHelper.setHttpProxyHost("host")
        GradleProxyHelper.setHttpProxyPort(123)

        def task = project.tasks.create('simple', NpmTask)
        mockProjectApiHelperExec(task)
        task.args.set(["run", "command"])

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setIgnoreExitValue(false)
        1 * execSpec.setExecutable({ executable ->
            def nodeExecutable = nodeDir.resolve("bin").resolve("node")
            return fixAbsolutePath(executable) == nodeExecutable.toAbsolutePath().toString()
        })
        1 * execSpec.setArgs({ args ->
            def npmScript = nodeDir
                    .resolve("lib").resolve("node_modules").resolve("npm").resolve("bin")
                    .resolve("npm-cli.js").toAbsolutePath().toString()
            return fixAbsolutePaths(args) == [npmScript, "run", "command"]
        })
        1 * execSpec.setEnvironment({ environment -> environment["HTTP_PROXY"] == "http://host:123" })
    }

    def "exec npm task with configured proxy"() {
        given:
        nodeExtension.resolvedPlatform.set(PlatformHelperKt.parsePlatform("Linux", "x86_64", {}))
        nodeExtension.environment.set([:])

        GradleProxyHelper.setHttpsProxyHost("my-super-proxy.net")
        GradleProxyHelper.setHttpsProxyPort(11235)

        def task = project.tasks.create('simple', NpmTask)
        mockProjectApiHelperExec(task)
        task.args.set(['a', 'b'])

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setExecutable('npm')
        1 * execSpec.setArgs(['a', 'b'])
        1 * execSpec.setEnvironment({ environment -> environment["HTTPS_PROXY"] == "http://my-super-proxy.net:11235" })
    }

    def "exec npm task with configured proxy but disabled"() {
        given:
        nodeExtension.resolvedPlatform.set(PlatformHelperKt.parsePlatform("Linux", "x86_64", {}))
        nodeExtension.environment.set([:])

        GradleProxyHelper.setHttpsProxyHost("my-super-proxy.net")
        GradleProxyHelper.setHttpsProxyPort(11235)
        nodeExtension.nodeProxySettings.set(ProxySettings.OFF)

        def task = project.tasks.create('simple', NpmTask)
        mockProjectApiHelperExec(task)
        task.args.set(['a', 'b'])

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setExecutable('npm')
        1 * execSpec.setArgs(['a', 'b'])
    }
}
