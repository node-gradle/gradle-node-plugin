package com.github.gradle.node.yarn.task


import com.github.gradle.node.task.AbstractTaskTest
import org.gradle.process.ExecSpec

class YarnSetupTaskTest extends AbstractTaskTest {
    def "exec yarnSetup task without any yarn version specified"() {
        given:
        this.execSpec = Mock(ExecSpec)

        def task = this.project.tasks.create('simple', YarnSetupTask)

        def expectedYarnInstallDirectory = projectDir.toPath().resolve('.gradle').resolve('yarn')
                .resolve('yarn-latest').toFile()

        when:
        this.project.evaluate()
        task.exec()

        then:
        1 * this.execSpec.setArgs(['install', '--global', '--no-save', '--prefix',
                                   expectedYarnInstallDirectory.absolutePath, 'yarn'])
    }

    def "exec yarnSetup task with yarn version specified"() {
        given:
        this.ext.yarnVersion = '1.22.4'
        this.execSpec = Mock(ExecSpec)

        def task = this.project.tasks.create('simple', YarnSetupTask)

        def expectedYarnInstallDirectory = projectDir.toPath().resolve('.gradle').resolve('yarn')
                .resolve('yarn-v1.22.4').toFile()

        when:
        this.project.evaluate()
        task.exec()

        then:
        1 * this.execSpec.setArgs(['install', '--global', '--no-save', '--prefix',
                                   expectedYarnInstallDirectory.absolutePath, 'yarn@1.22.4'])
    }
}
