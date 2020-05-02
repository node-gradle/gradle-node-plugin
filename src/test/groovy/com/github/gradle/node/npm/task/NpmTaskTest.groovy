package com.github.gradle.node.npm.task

import com.github.gradle.node.npm.proxy.GradleProxyHelper
import com.github.gradle.node.task.AbstractTaskTest
import org.gradle.process.ExecSpec

class NpmTaskTest extends AbstractTaskTest {
    def cleanup() {
        GradleProxyHelper.resetProxy()
    }

    def "exec npm task"() {
        given:
        props.setProperty('os.name', 'Linux')
        execSpec = Mock(ExecSpec)

        def task = project.tasks.create('simple', NpmTask)
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
        execSpec = Mock(ExecSpec)

        def task = project.tasks.create('simple', NpmTask)
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
        nodeExtension.download.set(true)
        execSpec = Mock(ExecSpec)
        def nodeDir = projectDir.toPath().resolve(".gradle").resolve("nodejs")
                .resolve("node-v12.16.2-linux-x64")

        def task = project.tasks.create('simple', NpmTask)
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
        nodeExtension.download.set(true)
        execSpec = Mock(ExecSpec)
        def nodeDir = projectDir.toPath().resolve(".gradle").resolve("nodejs")
                .resolve("node-v12.16.2-linux-x64")
        GradleProxyHelper.setHttpProxyHost("host")
        GradleProxyHelper.setHttpProxyPort(123)

        def task = project.tasks.create('simple', NpmTask)
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
            return fixAbsolutePaths(args) == [npmScript, "--proxy", "http://host:1234", "run", "command"]
        })
    }

    def "exec npm task with configured proxy"() {
        given:
        props.setProperty('os.name', 'Linux')
        execSpec = Mock(ExecSpec)
        GradleProxyHelper.setHttpsProxyHost("my-super-proxy.net")
        GradleProxyHelper.setHttpsProxyPort(11235)

        def task = project.tasks.create('simple', NpmTask)
        task.args.set(['a', 'b'])

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setExecutable('npm')
        1 * execSpec.setArgs(['--https-proxy', 'https://my-super-proxy.net:11235', 'a', 'b'])
    }
}
