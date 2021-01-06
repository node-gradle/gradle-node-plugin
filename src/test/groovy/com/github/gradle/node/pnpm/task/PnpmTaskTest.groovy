package com.github.gradle.node.pnpm.task

import com.github.gradle.node.task.AbstractTaskTest
import org.gradle.process.ExecSpec

class PnpmTaskTest extends AbstractTaskTest {
    def "exec pnpm task"() {
        given:
        props.setProperty('os.name', 'Linux')
        execSpec = Mock(ExecSpec)

        def task = project.tasks.create('simple', PnpmTask)
        task.args.set(['a', 'b'])
        task.environment.set(['a': '1'])
        task.ignoreExitValue.set(true)
        task.workingDir.set(projectDir)

        when:
        project.evaluate()
        task.exec()

        then:
        task.args.set(['a', 'b'])
        1 * execSpec.setIgnoreExitValue(true)
        1 * execSpec.setEnvironment({ it['a'] == '1' && containsPath(it) })
        1 * execSpec.setExecutable('pnpm')
        1 * execSpec.setArgs(['a', 'b'])
    }

    def "exec pnpm task (windows)"() {
        given:
        props.setProperty('os.name', 'Windows')
        execSpec = Mock(ExecSpec)

        def task = project.tasks.create('simple', PnpmTask)
        task.args.set(['a', 'b'])
        task.environment.set(['a': '1'])
        task.ignoreExitValue.set(true)
        task.workingDir.set(projectDir)

        when:
        project.evaluate()
        task.exec()

        then:
        task.args.get() == ['a', 'b']
        1 * execSpec.setIgnoreExitValue(true)
        1 * execSpec.setEnvironment({ it['a'] == '1' && containsPath(it) })
        1 * execSpec.setExecutable('pnpm.cmd')
        1 * execSpec.setArgs(['a', 'b'])
    }

    def "exec pnpm task (download)"() {
        given:
        props.setProperty('os.name', 'Linux')
        nodeExtension.download.set(true)
        execSpec = Mock(ExecSpec)

        def task = project.tasks.create('simple', PnpmTask)

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setIgnoreExitValue(false)
    }
}
