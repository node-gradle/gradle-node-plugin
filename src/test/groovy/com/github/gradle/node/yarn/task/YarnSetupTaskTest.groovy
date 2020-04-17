package com.github.gradle.node.yarn.task

import com.github.gradle.node.task.AbstractTaskTest
import org.gradle.process.ExecSpec

class YarnSetupTaskTest extends AbstractTaskTest {
    def "exec yarnSetup task without any yarn version specified"() {
        given:
        execSpec = Mock(ExecSpec)

        def task = project.tasks.create('simple', YarnSetupTask)

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setArgs({ args ->
            def expectedYarnInstallPath = projectDir.toPath().resolve('.gradle').resolve('yarn')
                    .resolve('yarn-latest').toAbsolutePath().toString()
            def expectedArgs = ['install', '--global', '--no-save', '--prefix', expectedYarnInstallPath, 'yarn']
            // Workaround a strange issue on Github actions macOS hosts
            return args.collect { it.replace("^/private/", "/") } == expectedArgs
        })
    }

    def "exec yarnSetup task with yarn version specified"() {
        given:
        nodeExtension.yarnVersion = '1.22.4'
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
            // Workaround a strange issue on Github actions macOS hosts
            return args.collect { it.replace("^/private/", "/") } == expectedArgs
        })
    }
}
