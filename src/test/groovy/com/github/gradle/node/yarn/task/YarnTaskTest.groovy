package com.github.gradle.node.yarn.task

import com.github.gradle.node.npm.proxy.GradleProxyHelper
import com.github.gradle.node.task.AbstractTaskTest
import org.gradle.process.ExecSpec

class YarnTaskTest extends AbstractTaskTest {
    def cleanup() {
        GradleProxyHelper.resetProxy()
    }

    def "exec yarn task"() {
        given:
        execSpec = Mock(ExecSpec)

        def task = project.tasks.create('simple', YarnTask)
        task.args.set(['a', 'b'])
        task.environment.set(['a': '1'])
        task.ignoreExitValue.set(true)
        task.workingDir.set(projectDir)

        when:
        project.evaluate()
        task.exec()

        then:
        task.args.get() == ['a', 'b']
        1 * execSpec.setIgnoreExitValue(true)
        1 * execSpec.setEnvironment({ it['a'] == '1' && containsPath(it) })
        1 * execSpec.setArgs(['a', 'b'])
    }

    def "exec yarn task (download)"() {
        given:
        props.setProperty('os.name', 'Linux')
        nodeExtension.download.set(true)
        execSpec = Mock(ExecSpec)

        def task = project.tasks.create('simple', YarnTask)
        task.args.set(["run", "command"])

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setExecutable({ executable ->
            def yarnExecutable = projectDir.toPath().resolve(".gradle")
                    .resolve("yarn").resolve("yarn-latest").resolve("bin").resolve("yarn")
            return fixAbsolutePath(executable) == yarnExecutable.toAbsolutePath().toString()
        })
        1 * execSpec.setArgs(["run", "command"])
    }

    def "exec yarn task (download) with configured proxy"() {
        given:
        props.setProperty('os.name', 'Linux')
        nodeExtension.download.set(true)
        execSpec = Mock(ExecSpec)
        GradleProxyHelper.setHttpsProxyHost("1.2.3.4")
        GradleProxyHelper.setHttpsProxyPort(443)
        GradleProxyHelper.setHttpsProxyUser("me")
        GradleProxyHelper.setHttpsProxyPassword("password")

        def task = project.tasks.create('simple', YarnTask)
        task.args.set(["run", "command"])

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setExecutable({ executable ->
            def yarnExecutable = projectDir.toPath().resolve(".gradle")
                    .resolve("yarn").resolve("yarn-latest").resolve("bin").resolve("yarn")
            return fixAbsolutePath(executable) == yarnExecutable.toAbsolutePath().toString()
        })
        1 * execSpec.setArgs(["run", "command"])
        1 * execSpec.setEnvironment({ environment -> environment["HTTPS_PROXY"] == "http://me:password@1.2.3.4:443" })
    }

    def "exec yarn task (download) with configured proxy but disabled"() {
        given:
        props.setProperty('os.name', 'Linux')
        nodeExtension.download.set(true)
        execSpec = Mock(ExecSpec)
        GradleProxyHelper.setHttpsProxyHost("1.2.3.4")
        GradleProxyHelper.setHttpsProxyPort(443)
        GradleProxyHelper.setHttpsProxyUser("me")
        GradleProxyHelper.setHttpsProxyPassword("password")
        nodeExtension.useGradleProxySettings.set(false)

        def task = project.tasks.create('simple', YarnTask)
        task.args.set(["run", "command"])

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setExecutable({ executable ->
            def yarnExecutable = projectDir.toPath().resolve(".gradle")
                    .resolve("yarn").resolve("yarn-latest").resolve("bin").resolve("yarn")
            return fixAbsolutePath(executable) == yarnExecutable.toAbsolutePath().toString()
        })
        1 * execSpec.setArgs(["run", "command"])
    }
}
