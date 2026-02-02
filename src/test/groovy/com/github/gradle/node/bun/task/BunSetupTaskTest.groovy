package com.github.gradle.node.bun.task

import com.github.gradle.node.bun.BunUtils
import com.github.gradle.node.task.AbstractTaskTest

class BunSetupTaskTest
    extends AbstractTaskTest
{
    def "exec bunSetup task without any bun version specified"() {
        given:
        def task = project.tasks.create('simple', BunSetupTask)
        mockExecOperationsExec(task)

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setArgs({ args ->
            def expectedBunInstallPath = projectDir.toPath().resolve('.gradle').resolve('bun')
                    .resolve('bun-latest').toAbsolutePath().toString()
            def expectedArgs = ['install', '--global', '--no-save', '--prefix', expectedBunInstallPath, 'bun']
            // Workaround a strange issue on Github actions macOS hosts
            return args.collect { it.replace("^/private/", "/") } == expectedArgs
        })
    }

    def "exec bunSetup task with bun version specified"() {
        given:
        def bunVersion = BunUtils.VERSION
        nodeExtension.bunVersion.set(bunVersion)
        def task = project.tasks.create('simple', BunSetupTask)
        mockExecOperationsExec(task)

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setArgs({ args ->
            def expectedBunInstallPath = projectDir.toPath().resolve('.gradle').resolve('bun')
                    .resolve("pnpm-v${bunVersion}").toAbsolutePath().toString()
            def expectedArgs = ['install', '--global', '--no-save', '--prefix', expectedBunInstallPath, "bun@${bunVersion}"]
            // Workaround a strange issue on Github actions macOS hosts
            return args.collect { it.replace("^/private/", "/") } == expectedArgs
        })
    }
}
