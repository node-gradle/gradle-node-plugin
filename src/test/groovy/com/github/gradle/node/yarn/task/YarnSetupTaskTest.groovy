package com.github.gradle.node.yarn.task

import com.github.gradle.node.npm.proxy.GradleProxyHelper
import com.github.gradle.node.npm.proxy.ProxySettings
import com.github.gradle.node.task.AbstractTaskTest

class YarnSetupTaskTest extends AbstractTaskTest {
    def cleanup() {
        GradleProxyHelper.resetProxy()
    }

    def "exec yarnSetup task without any yarn version specified and proxy configured"() {
        given:
        GradleProxyHelper.setHttpProxyHost("my-proxy")
        GradleProxyHelper.setHttpProxyPort(80)

        def task = project.tasks.create('simple', YarnSetupTask)
        mockProjectApiHelperExec(task)

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setArgs({ args ->
            def expectedYarnInstallPath = projectDir.toPath().resolve('.gradle').resolve('yarn')
                    .resolve('yarn-latest').toAbsolutePath().toString()
            def expectedArgs = ['--proxy', 'http://my-proxy:80', 'install', '--global', '--no-save',
                                '--prefix', expectedYarnInstallPath, 'yarn']
            return fixAbsolutePaths(args) == expectedArgs
        })
    }

    def "exec yarnSetup task without any yarn version specified and proxy configured but disabled"() {
        given:
        GradleProxyHelper.setHttpProxyHost("my-proxy")
        GradleProxyHelper.setHttpProxyPort(80)
        nodeExtension.nodeProxySettings.set(ProxySettings.OFF)

        def task = project.tasks.create('simple', YarnSetupTask)
        mockProjectApiHelperExec(task)

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setArgs({ args ->
            def expectedYarnInstallPath = projectDir.toPath().resolve('.gradle').resolve('yarn')
                    .resolve('yarn-latest').toAbsolutePath().toString()
            def expectedArgs = ['install', '--global', '--no-save', '--prefix', expectedYarnInstallPath, 'yarn']
            return fixAbsolutePaths(args) == expectedArgs
        })
    }

    def "exec yarnSetup task with yarn version specified"() {
        given:
        nodeExtension.yarnVersion.set('1.22.4')

        def task = project.tasks.create('simple', YarnSetupTask)
        mockProjectApiHelperExec(task)

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setArgs({ args ->
            def expectedYarnInstallPath = projectDir.toPath().resolve('.gradle').resolve('yarn')
                    .resolve('yarn-v1.22.4').toAbsolutePath().toString()
            def expectedArgs = ['install', '--global', '--no-save', '--prefix', expectedYarnInstallPath, 'yarn@1.22.4']
            return fixAbsolutePaths(args) == expectedArgs
        })
    }
}
