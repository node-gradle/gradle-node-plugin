package com.github.gradle.node.npm.task

import com.github.gradle.node.task.AbstractTaskTest
import org.gradle.process.ExecSpec

class NpmSetupTaskTest extends AbstractTaskTest {
    def "disable npmSetup task when no npm version is specified"() {
        given:
        props.setProperty('os.name', 'Linux')
        execSpec = Mock(ExecSpec)

        def task = project.tasks.create('simple', NpmSetupTask)

        when:
        project.evaluate()

        then:
        !task.isEnabled()
    }

    def "exec npmSetup task (version specified)"() {
        given:
        props.setProperty('os.name', 'Linux')
        nodeExtension.npmVersion = '6.4.1'
        execSpec = Mock(ExecSpec)

        def task = project.tasks.create('simple', NpmSetupTask)

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setArgs({ args ->
            def expectedNpmInstallPath = projectDir.toPath().resolve('.gradle').resolve('npm')
                    .resolve('npm-v6.4.1').toAbsolutePath().toString()
            def expectedArgs = ['install', '--global', '--no-save', '--prefix', expectedNpmInstallPath, 'npm@6.4.1']
            // Workaround a strange issue on Github actions macOS hosts
            return args.collect { it.replace('^/private/', '/') } == expectedArgs
        })
    }
}
