package com.github.gradle.node.npm.task

import com.github.gradle.node.task.AbstractTaskTest
import org.gradle.process.ExecSpec

class NpmTaskTest extends AbstractTaskTest {
    def "exec npm task"() {
        given:
        this.props.setProperty('os.name', 'Linux')
        this.execSpec = Mock(ExecSpec)

        def task = this.project.tasks.create('simple', NpmTask)
        task.args = ['a', 'b']
        task.environment = ['a': '1']
        task.ignoreExitValue = true
        task.workingDir = this.projectDir
        task.execOverrides = {}

        when:
        this.project.evaluate()
        task.exec()

        then:
        task.args == ['a', 'b']
        1 * this.execSpec.setIgnoreExitValue(true)
        1 * this.execSpec.setEnvironment({ it['a'] == '1' && containsPath(it) })
        1 * this.execSpec.setExecutable('npm')
        1 * this.execSpec.setArgs(['a', 'b'])
    }

    def "exec npm task (windows)"() {
        given:
        this.props.setProperty('os.name', 'Windows')
        this.execSpec = Mock(ExecSpec)

        def task = this.project.tasks.create('simple', NpmTask)
        task.args = ['a', 'b']
        task.environment = ['a': '1']
        task.ignoreExitValue = true
        task.workingDir = this.projectDir
        task.execOverrides = {}

        when:
        this.project.evaluate()
        task.exec()

        then:
        task.args == ['a', 'b']
        1 * this.execSpec.setIgnoreExitValue(true)
        1 * this.execSpec.setEnvironment({ it['a'] == '1' && containsPath(it) })
        1 * this.execSpec.setExecutable('npm.cmd')
        1 * this.execSpec.setArgs(['a', 'b'])
    }

    def "exec npm task (download)"() {
        given:
        this.props.setProperty('os.name', 'Linux')
        this.ext.download = true
        this.execSpec = Mock(ExecSpec)

        def task = this.project.tasks.create('simple', NpmTask)

        when:
        this.project.evaluate()
        task.exec()

        then:
        1 * this.execSpec.setIgnoreExitValue(false)
    }
}
