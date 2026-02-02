package com.github.gradle.node.pnpm.task

import com.github.gradle.node.Versions
import com.github.gradle.node.task.AbstractTaskTest

class PnpmSetupTaskTest
    extends AbstractTaskTest
{
    def "exec pnpmSetup task without any pnpm version specified"() {
        given:
        def task = project.tasks.create('simple', PnpmSetupTask)
        mockExecOperationsExec(task)

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setArgs({ args ->
            def expectedPnpmInstallPath = projectDir.toPath().resolve('.gradle').resolve('pnpm')
                    .resolve('pnpm-latest').toAbsolutePath().toString()
            def expectedArgs = ['install', '--global', '--no-save', '--prefix', expectedPnpmInstallPath, 'pnpm']
            // Workaround a strange issue on Github actions macOS hosts
            return args.collect { it.replace("^/private/", "/") } == expectedArgs
        })
    }

    def "exec pnpmSetup task with pnpm version specified"() {
        given:
        nodeExtension.pnpmVersion.set(Versions.TEST_PNPM_DOWNLOAD_VERSION)
        def task = project.tasks.create('simple', PnpmSetupTask)
        mockExecOperationsExec(task)

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setArgs({ args ->
            def expectedPnpmInstallPath = projectDir.toPath().resolve('.gradle').resolve('pnpm')
                    .resolve("pnpm-v${Versions.TEST_PNPM_DOWNLOAD_VERSION}").toAbsolutePath().toString()
            def expectedArgs = ['install', '--global', '--no-save', '--prefix', expectedPnpmInstallPath, "pnpm@${Versions.TEST_PNPM_DOWNLOAD_VERSION}"]
            // Workaround a strange issue on Github actions macOS hosts
            return args.collect { it.replace("^/private/", "/") } == expectedArgs
        })
    }
}
