package com.github.gradle.node.npm.task

import com.github.gradle.node.npm.proxy.GradleProxyHelper
import com.github.gradle.node.task.AbstractTaskTest
import com.github.gradle.node.util.PlatformHelperKt

class NpmSetupTaskTest extends AbstractTaskTest {
    def cleanup() {
        GradleProxyHelper.resetProxy()
    }

    def "disable npmSetup task when no npm version is specified"() {
        given:
        nodeExtension.resolvedPlatform.set(PlatformHelperKt.parsePlatform("Linux", "x86_64", {}))

        def task = project.tasks.create('simple', NpmSetupTask)

        when:
        project.evaluate()

        then:
        !task.isTaskEnabled()
    }

    def "exec npmSetup task (version specified)"() {
        given:
        nodeExtension.resolvedPlatform.set(PlatformHelperKt.parsePlatform("Linux", "x86_64", {}))
        nodeExtension.npmVersion.set('6.4.1')

        def task = project.tasks.create('simple', NpmSetupTask)
        mockProjectApiHelperExec(task)

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setArgs({ args ->
            def expectedNpmInstallPath = projectDir.toPath().resolve('.gradle').resolve('npm')
                    .resolve('npm-v6.4.1').toAbsolutePath().toString()
            def expectedArgs = ['install', '--global', '--no-save', '--prefix', expectedNpmInstallPath, 'npm@6.4.1']
            return fixAbsolutePaths(args) == expectedArgs
        })
    }

    def "exec npmSetup task with proxy configured"() {
        given:
        nodeExtension.resolvedPlatform.set(PlatformHelperKt.parsePlatform("Linux", "x86_64", {}))
        nodeExtension.npmVersion.set('6.4.1')
        GradleProxyHelper.setHttpProxyHost("my-proxy.net")
        GradleProxyHelper.setHttpProxyPort(1234)

        def task = project.tasks.create('simple', NpmSetupTask)
        mockProjectApiHelperExec(task)

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setArgs({ args ->
            def expectedNpmInstallPath = projectDir.toPath().resolve('.gradle').resolve('npm')
                    .resolve('npm-v6.4.1').toAbsolutePath().toString()
            def expectedArgs = ['--proxy', 'http://my-proxy.net:1234',
                                'install', '--global', '--no-save', '--prefix', expectedNpmInstallPath, 'npm@6.4.1']
            return fixAbsolutePaths(args) == expectedArgs
        })
    }
}
