package com.github.gradle.node.pnpm.task

import com.github.gradle.node.task.AbstractTaskTest

class PnpxTaskTest extends AbstractTaskTest {
    def "exec pnpx task"() {
        given:
        props.setProperty('os.name', 'Linux')

        def task = project.tasks.create('simple', PnpxTask)
        mockProjectApiHelperExec(task)
        task.command.set('command')
        task.args.set(['a', 'b'])
        task.environment.set(['a': '1'])
        task.ignoreExitValue.set(true)
        task.workingDir.set(projectDir)

        when:
        project.evaluate()
        task.exec()

        then:
        task.command.get() == 'command'
        task.args.get() == ['a', 'b']
        1 * execSpec.setIgnoreExitValue(true)
        1 * execSpec.setEnvironment({ it['a'] == '1' && containsPath(it) })
        1 * execSpec.setExecutable('pnpx')
        1 * execSpec.setArgs(['command', 'a', 'b'])
        1 * execSpec.setWorkingDir(projectDir)
    }

    def "exec pnpx task (windows)"() {
        given:
        props.setProperty('os.name', 'Windows')

        def task = project.tasks.create('simple', PnpxTask)
        mockProjectApiHelperExec(task)
        task.command.set('command')
        task.args.set(['a', 'b'])
        task.environment.set(['a': '1'])
        task.ignoreExitValue.set(true)
        task.workingDir.set(projectDir)

        when:
        project.evaluate()
        task.exec()

        then:
        task.command.get() == 'command'
        task.args.get() == ['a', 'b']
        1 * execSpec.setIgnoreExitValue(true)
        1 * execSpec.setEnvironment({ it['a'] == '1' && containsPath(it) })
        1 * execSpec.setExecutable('pnpx.cmd')
        1 * execSpec.setArgs(['command', 'a', 'b'])
        1 * execSpec.setWorkingDir(projectDir)
    }

    def "exec pnpx task (download)"() {
        given:
        props.setProperty('os.name', 'Linux')
        nodeExtension.download.set(true)

        def task = project.tasks.create('simple', PnpxTask)
        mockProjectApiHelperExec(task)
        task.command.set('command')

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setIgnoreExitValue(false)
        1 * execSpec.setExecutable(nodeExtension.pnpmWorkDir.get().dir("pnpm-latest/bin/pnpx").toString())
        1 * execSpec.setArgs(['command'])
    }
}
