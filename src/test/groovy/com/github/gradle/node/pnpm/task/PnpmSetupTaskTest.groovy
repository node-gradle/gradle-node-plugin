package com.github.gradle.node.pnpm.task

import com.github.gradle.node.task.AbstractTaskTest
import org.gradle.process.ExecSpec

class PnpmSetupTaskTest
        extends AbstractTaskTest {
    def "exec pnpmSetup task without any pnpm version specified"() {
        given:
        def task = project.tasks.create('simple', PnpmSetupTask)
        mockProjectApiHelperExec(task)

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
        nodeExtension.pnpmVersion = '4.12.4'

        def task = project.tasks.create('simple', PnpmSetupTask)
        mockProjectApiHelperExec(task)

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setArgs({ args ->
            def expectedPnpmInstallPath = projectDir.toPath().resolve('.gradle').resolve('pnpm')
                    .resolve('pnpm-v4.12.4').toAbsolutePath().toString()
            def expectedArgs = ['install', '--global', '--no-save', '--prefix', expectedPnpmInstallPath, 'pnpm@4.12.4']
            // Workaround a strange issue on Github actions macOS hosts
            return args.collect { it.replace("^/private/", "/") } == expectedArgs
        })
    }
}
