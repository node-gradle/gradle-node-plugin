package com.github.gradle.node.npm.task


import com.github.gradle.node.task.AbstractTaskTest
import com.github.gradle.node.util.PlatformHelperKt
import com.github.gradle.node.variant.VariantComputer
import com.github.gradle.node.variant.VariantComputerKt

class NpxTaskTest extends AbstractTaskTest {
    def "exec npx task"() {
        given:
        nodeExtension.resolvedPlatform.set(PlatformHelperKt.parsePlatform("Linux", "x86_64", {}))
        nodeExtension.environment.set([:])

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
        nodeExtension.resolvedPlatform.set(PlatformHelperKt.parsePlatform("Windows", "x86_64", {}))

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
        nodeExtension.resolvedPlatform.set(PlatformHelperKt.parsePlatform("Linux", "x86_64", {}))
        nodeExtension.download.set(true)
        def variantComputer = new VariantComputer()
        def nodeDir = nodeExtension.resolvedNodeDir
        def nodeBinDir = variantComputer.computeNodeBinDir(nodeDir, nodeExtension.resolvedPlatform)
        def npxScriptFile = VariantComputerKt.computeNpmScriptFile(nodeDir, "npx", nodeExtension.resolvedPlatform.get().isWindows())

        def task = project.tasks.create('simple', NpxTask)
        mockProjectApiHelperExec(task)

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setIgnoreExitValue(false)
        1 * execSpec.setExecutable(nodeBinDir.get().file("node").asFile.toString())
        1 * execSpec.setArgs([npxScriptFile.get()])
    }
}
