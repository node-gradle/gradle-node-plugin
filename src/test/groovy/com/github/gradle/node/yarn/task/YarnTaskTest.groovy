package com.github.gradle.node.yarn.task

import com.github.gradle.node.task.AbstractTaskTest
import org.gradle.process.ExecSpec

class YarnTaskTest extends AbstractTaskTest {
    def "exec yarn task"() {
        given:
        execSpec = Mock(ExecSpec)

        def task = project.tasks.create('simple', YarnTask)
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
        1 * execSpec.setArgs(['a', 'b'])
    }

    def "exec yarn task (download)"() {
        given:
        nodeExtension.download.set(true)
        execSpec = Mock(ExecSpec)

        def task = project.tasks.create('simple', YarnTask)

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setIgnoreExitValue(false)
    }
}
