package com.github.gradle.node.yarn.task

import com.github.gradle.node.task.AbstractTaskTest
import org.gradle.process.ExecSpec

class YarnSetupTaskTest extends AbstractTaskTest {
    def "exec yarnSetup task without any yarn version specified"() {
        given:
        this.execSpec = Mock(ExecSpec)

        def task = this.project.tasks.create('simple', YarnSetupTask)

        when:
        this.project.evaluate()
        task.exec()

        then:
        1 * this.execSpec.setArgs({ args ->
            def expectedYarnInstallPath = projectDir.toPath().resolve('.gradle').resolve('yarn')
                    .resolve('yarn-latest').toAbsolutePath().toString()
            def expectedArgs = ['install', '--global', '--no-save', '--prefix', expectedYarnInstallPath, 'yarn']
            // Workaround a strange issue on Github actions macOS hosts
            return args.collect { it.replace("^/private/", "/") } == expectedArgs
        })
    }

    def "exec yarnSetup task with yarn version specified"() {
        given:
        this.ext.yarnVersion = '1.22.4'
        this.execSpec = Mock(ExecSpec)

        def task = this.project.tasks.create('simple', YarnSetupTask)

        when:
        this.project.evaluate()
        task.exec()

        then:
        1 * this.execSpec.setArgs({ args ->
            def expectedYarnInstallPath = projectDir.toPath().resolve('.gradle').resolve('yarn')
                    .resolve('yarn-v1.22.4').toAbsolutePath().toString()
            def expectedArgs = ['install', '--global', '--no-save', '--prefix', expectedYarnInstallPath, 'yarn@1.22.4']
            // Workaround a strange issue on Github actions macOS hosts
            return args.collect { it.replace("^/private/", "/") } == expectedArgs
        })
    }
}
