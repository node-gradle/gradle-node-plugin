package com.github.gradle.node.yarn.task

import com.github.gradle.node.npm.proxy.GradleProxyHelper
import com.github.gradle.node.task.AbstractTaskTest
import org.gradle.process.ExecSpec

class YarnSetupTaskTest extends AbstractTaskTest {
    def cleanup() {
        GradleProxyHelper.resetProxy()
    }

    def "exec yarnSetup task without any yarn version specified and proxy configured"() {
        given:
        execSpec = Mock(ExecSpec)
        GradleProxyHelper.setHttpProxyHost("my-proxy")
        GradleProxyHelper.setHttpProxyPort(80)

        def task = project.tasks.create('simple', YarnSetupTask)

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
        execSpec = Mock(ExecSpec)
        GradleProxyHelper.setHttpProxyHost("my-proxy")
        GradleProxyHelper.setHttpProxyPort(80)
        nodeExtension.useGradleProxySettings.set(false)

        def task = project.tasks.create('simple', YarnSetupTask)

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
        execSpec = Mock(ExecSpec)

        def task = project.tasks.create('simple', YarnSetupTask)

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
