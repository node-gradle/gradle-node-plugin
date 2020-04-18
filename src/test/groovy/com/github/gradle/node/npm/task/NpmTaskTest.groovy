package com.github.gradle.node.npm.task

import com.github.gradle.node.task.AbstractTaskTest
import org.gradle.process.ExecSpec

class NpmTaskTest extends AbstractTaskTest {
    def "exec npm task"() {
        given:
        props.setProperty('os.name', 'Linux')
        execSpec = Mock(ExecSpec)

        def task = project.tasks.create('simple', NpmTask)
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
        1 * execSpec.setExecutable('npm')
        1 * execSpec.setArgs(['a', 'b'])
    }

    def "exec npm task (windows)"() {
        given:
        props.setProperty('os.name', 'Windows')
        execSpec = Mock(ExecSpec)

        def task = project.tasks.create('simple', NpmTask)
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
        1 * execSpec.setExecutable('npm.cmd')
        1 * execSpec.setArgs(['a', 'b'])
    }

    def "exec npm task (download)"() {
        given:
        props.setProperty('os.name', 'Linux')
        nodeExtension.download.set(true)
        execSpec = Mock(ExecSpec)

        def task = project.tasks.create('simple', NpmTask)

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setIgnoreExitValue(false)
    }
}
