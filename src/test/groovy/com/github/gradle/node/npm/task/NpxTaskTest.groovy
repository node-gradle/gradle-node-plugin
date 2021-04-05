package com.github.gradle.node.npm.task

import com.github.gradle.node.task.AbstractTaskTest
import com.github.gradle.node.variant.VariantComputer

class NpxTaskTest extends AbstractTaskTest {
    def "exec npx task"() {
        given:
        props.setProperty('os.name', 'Linux')

        def task = project.tasks.create('simple', NpxTask)
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
        1 * execSpec.setExecutable('npx')
        1 * execSpec.setArgs(['command', 'a', 'b'])
        1 * execSpec.setWorkingDir(projectDir)
    }

    def "exec npx task (windows)"() {
        given:
        props.setProperty('os.name', 'Windows')

        def task = project.tasks.create('simple', NpxTask)
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
        1 * execSpec.setExecutable('npx.cmd')
        1 * execSpec.setArgs(['command', 'a', 'b'])
        1 * execSpec.setWorkingDir(projectDir)
    }

    def "exec npx task (download)"() {
        given:
        props.setProperty('os.name', 'Linux')
        nodeExtension.download.set(true)
        def variantComputer = new VariantComputer()
        def nodeDir = variantComputer.computeNodeDir(nodeExtension)
        def nodeBinDir = variantComputer.computeNodeBinDir(nodeDir)
        def npxScriptFile = variantComputer.computeNpmScriptFile(nodeDir, "npx")

        def task = project.tasks.create('simple', NpxTask)
        mockProjectApiHelperExec(task)

        when:
        project.evaluate()
        task.exec()

        then:
        // Ignore exit value is always set to true and handled manually
        1 * execSpec.setIgnoreExitValue(true)
        1 * execSpec.setExecutable(nodeBinDir.get().file("node").asFile.toString())
        1 * execSpec.setArgs([npxScriptFile.get()])
    }
}
