package com.github.gradle.node.npm.task

import com.github.gradle.node.npm.proxy.GradleProxyHelper
import com.github.gradle.node.npm.proxy.ProxySettings
import com.github.gradle.node.task.AbstractTaskTest

class NpmInstallTaskTest extends AbstractTaskTest {
    def cleanup() {
        GradleProxyHelper.resetProxy()
    }

    def "exec npm install task with configured proxy"() {
        given:
        props.setProperty('os.name', 'Linux')
        GradleProxyHelper.setHttpsProxyHost("my-super-proxy.net")
        GradleProxyHelper.setHttpsProxyPort(11235)

        def task = project.tasks.getByName("npmInstall")
        mockProjectApiHelperExec(task)

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setExecutable("npm")
        1 * execSpec.setArgs(["install"])
        1 * execSpec.setEnvironment({ environment -> environment["HTTPS_PROXY"] == "http://my-super-proxy.net:11235" })
    }

    def "exec npm install task with configured proxy but disabled"() {
        given:
        props.setProperty('os.name', 'Linux')
        GradleProxyHelper.setHttpsProxyHost("my-super-proxy.net")
        GradleProxyHelper.setHttpsProxyPort(11235)
        nodeExtension.nodeProxySettings.set(ProxySettings.OFF)

        def task = project.tasks.getByName("npmInstall")
        mockProjectApiHelperExec(task)

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setExecutable("npm")
        1 * execSpec.setArgs(["install"])
    }
}
