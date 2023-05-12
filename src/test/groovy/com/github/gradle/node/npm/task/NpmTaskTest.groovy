package com.github.gradle.node.npm.task


import com.github.gradle.node.npm.proxy.GradleProxyHelper
import com.github.gradle.node.npm.proxy.ProxySettings
import com.github.gradle.node.task.AbstractTaskTest
import com.github.gradle.node.util.PlatformHelperKt

import static com.github.gradle.node.NodeExtension.DEFAULT_NODE_VERSION

class NpmTaskTest extends AbstractTaskTest {
    def cleanup() {
        GradleProxyHelper.resetProxy()
    }

    def "exec npm task"() {
        given:
        props.setProperty('os.name', 'Linux')
        nodeExtension.computedPlatform.set(PlatformHelperKt.parsePlatform("Linux", "x86_64", {}))

        def task = project.tasks.create('simple', NpmTask)
        mockPlatformHelper(task)
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
        props.setProperty('os.name', 'Windows')
        nodeExtension.computedPlatform.set(PlatformHelperKt.parsePlatform("Windows", "x86_64", {}))

        def task = project.tasks.create('simple', NpmTask)
        mockPlatformHelper(task)
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

    def "exec npm task (download)"() {
        given:
        props.setProperty('os.name', 'Linux')
        nodeExtension.computedPlatform.set(PlatformHelperKt.parsePlatform("Linux", "x86_64", {}))
        nodeExtension.download.set(true)
        def nodeDir = projectDir.toPath().resolve(".gradle").resolve("nodejs")
                .resolve("node-v${DEFAULT_NODE_VERSION}-linux-x64")

        def task = project.tasks.create('simple', NpmTask)
        mockPlatformHelper(task)
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
        props.setProperty('os.name', 'Linux')
        nodeExtension.computedPlatform.set(PlatformHelperKt.parsePlatform("Linux", "x86_64", {}))
        nodeExtension.download.set(true)
        def nodeDir = projectDir.toPath().resolve(".gradle").resolve("nodejs")
                .resolve("node-v${DEFAULT_NODE_VERSION}-linux-x64")
        GradleProxyHelper.setHttpProxyHost("host")
        GradleProxyHelper.setHttpProxyPort(123)

        def task = project.tasks.create('simple', NpmTask)
        mockPlatformHelper(task)
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
        props.setProperty('os.name', 'Linux')
        nodeExtension.computedPlatform.set(PlatformHelperKt.parsePlatform("Linux", "x86_64", {}))
        GradleProxyHelper.setHttpsProxyHost("my-super-proxy.net")
        GradleProxyHelper.setHttpsProxyPort(11235)

        def task = project.tasks.create('simple', NpmTask)
        mockPlatformHelper(task)
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
        props.setProperty('os.name', 'Linux')
        nodeExtension.computedPlatform.set(PlatformHelperKt.parsePlatform("Linux", "x86_64", {}))
        GradleProxyHelper.setHttpsProxyHost("my-super-proxy.net")
        GradleProxyHelper.setHttpsProxyPort(11235)
        nodeExtension.nodeProxySettings.set(ProxySettings.OFF)

        def task = project.tasks.create('simple', NpmTask)
        mockPlatformHelper(task)
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
