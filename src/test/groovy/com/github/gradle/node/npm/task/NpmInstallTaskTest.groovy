package com.github.gradle.node.npm.task

import com.github.gradle.node.npm.proxy.GradleProxyHelper
import com.github.gradle.node.task.AbstractTaskTest
import org.gradle.process.ExecSpec

class NpmInstallTaskTest extends AbstractTaskTest {
    def cleanup() {
        GradleProxyHelper.resetProxy()
    }

    def "exec npm install task with configured proxy"() {
        given:
        props.setProperty('os.name', 'Linux')
        execSpec = Mock(ExecSpec)
        GradleProxyHelper.setHttpsProxyHost("my-super-proxy.net")
        GradleProxyHelper.setHttpsProxyPort(11235)

        def task = project.tasks.getByName("npmInstall")

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setExecutable("npm")
        1 * execSpec.setArgs(["--https-proxy", "https://my-super-proxy.net:11235", "install"])
    }

    def "exec npm install task with configured proxy but disabled"() {
        given:
        props.setProperty('os.name', 'Linux')
        execSpec = Mock(ExecSpec)
        GradleProxyHelper.setHttpsProxyHost("my-super-proxy.net")
        GradleProxyHelper.setHttpsProxyPort(11235)
        nodeExtension.useGradleProxySettings.set(false)

        def task = project.tasks.getByName("npmInstall")

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setExecutable("npm")
        1 * execSpec.setArgs(["install"])
    }
}
