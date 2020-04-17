package com.github.gradle.node.npm.task

import com.github.gradle.node.task.AbstractTaskTest
import com.github.gradle.node.variant.VariantComputer
import org.gradle.process.ExecSpec

class NpxTaskTest extends AbstractTaskTest {
    def "exec npx task"() {
        given:
        props.setProperty('os.name', 'Linux')
        execSpec = Mock(ExecSpec)

        def task = project.tasks.create('simple', NpxTask)
        task.command = 'command'
        task.args = ['a', 'b']
        task.environment = ['a': '1']
        task.ignoreExitValue = true
        task.workingDir = projectDir
        task.execOverrides = {}

        when:
        project.evaluate()
        task.exec()

        then:
        task.command == 'command'
        task.args == ['a', 'b']
        1 * execSpec.setIgnoreExitValue(true)
        1 * execSpec.setEnvironment({ it['a'] == '1' && containsPath(it) })
        1 * execSpec.setExecutable('npx')
        1 * execSpec.setArgs(['command', 'a', 'b'])
        1 * execSpec.setWorkingDir(projectDir)
    }

    def "exec npx task (windows)"() {
        given:
        props.setProperty('os.name', 'Windows')
        execSpec = Mock(ExecSpec)

        def task = project.tasks.create('simple', NpxTask)
        task.command = 'command'
        task.args = ['a', 'b']
        task.environment = ['a': '1']
        task.ignoreExitValue = true
        task.workingDir = projectDir
        task.execOverrides = {}

        when:
        project.evaluate()
        task.exec()

        then:
        task.command == 'command'
        task.args == ['a', 'b']
        1 * execSpec.setIgnoreExitValue(true)
        1 * execSpec.setEnvironment({ it['a'] == '1' && containsPath(it) })
        1 * execSpec.setExecutable('npx.cmd')
        1 * execSpec.setArgs(['command', 'a', 'b'])
        1 * execSpec.setWorkingDir(projectDir)
    }

    def "exec npx task (download)"() {
        given:
        props.setProperty('os.name', 'Linux')
        nodeExtension.download = true
        execSpec = Mock(ExecSpec)
        def variantComputer = new VariantComputer()
        def nodeDir = variantComputer.computeNodeDir(nodeExtension)
        def nodeBinDir = variantComputer.computeNodeBinDir(nodeDir)
        def npxScriptFile = variantComputer.computeNpmScriptFile(nodeDir, "npx")

        def task = project.tasks.create('simple', NpxTask)

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setIgnoreExitValue(false)
        1 * execSpec.setExecutable(new File(nodeBinDir, "node").toString())
        1 * execSpec.setArgs([npxScriptFile])
    }
}
