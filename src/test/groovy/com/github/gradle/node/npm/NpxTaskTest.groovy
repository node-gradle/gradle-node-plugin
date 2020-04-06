package com.github.gradle.node.npm

import com.github.gradle.node.task.AbstractTaskTest
import com.github.gradle.node.variant.VariantBuilder
import org.gradle.process.ExecSpec

class NpxTaskTest extends AbstractTaskTest {
    def "exec npx task"() {
        given:
        this.props.setProperty('os.name', 'Linux')
        this.execSpec = Mock(ExecSpec)

        def task = this.project.tasks.create('simple', NpxTask)
        task.command = 'command'
        task.args = ['a', 'b']
        task.environment = ['a': '1']
        task.ignoreExitValue = true
        task.workingDir = this.projectDir
        task.execOverrides = {}

        when:
        this.project.evaluate()
        task.exec()

        then:
        task.command == 'command'
        task.args == ['a', 'b']
        task.result.exitValue == 0
        1 * this.execSpec.setIgnoreExitValue(true)
        1 * this.execSpec.setEnvironment({ it['a'] == '1' && containsPath(it) })
        1 * this.execSpec.setExecutable('npx')
        1 * this.execSpec.setArgs(['command', 'a', 'b'])
        1 * this.execSpec.setWorkingDir(this.projectDir)
    }

    def "exec npx task (windows)"() {
        given:
        this.props.setProperty('os.name', 'Windows')
        this.execSpec = Mock(ExecSpec)

        def task = this.project.tasks.create('simple', NpxTask)
        task.command = 'command'
        task.args = ['a', 'b']
        task.environment = ['a': '1']
        task.ignoreExitValue = true
        task.workingDir = this.projectDir
        task.execOverrides = {}

        when:
        this.project.evaluate()
        task.exec()

        then:
        task.command == 'command'
        task.args == ['a', 'b']
        task.result.exitValue == 0
        1 * this.execSpec.setIgnoreExitValue(true)
        1 * this.execSpec.setEnvironment({ it['a'] == '1' && containsPath(it) })
        1 * this.execSpec.setExecutable('npx.cmd')
        1 * this.execSpec.setArgs(['command', 'a', 'b'])
        1 * this.execSpec.setWorkingDir(this.projectDir)
    }

    def "exec npx task (download)"() {
        given:
        this.props.setProperty('os.name', 'Linux')
        this.ext.download = true
        this.execSpec = Mock(ExecSpec)
        def variant = new VariantBuilder(this.ext).build()

        def task = this.project.tasks.create('simple', NpxTask)

        when:
        this.project.evaluate()
        task.exec()

        then:
        task.result.exitValue == 0
        1 * this.execSpec.setIgnoreExitValue(false)
        1 * this.execSpec.setExecutable(new File(variant.nodeBinDir, "node").toString())
        1 * this.execSpec.setArgs([variant.npxScriptFile])
    }
}
