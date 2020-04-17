package com.github.gradle.node.task

import org.gradle.process.ExecSpec

class NodeTaskTest extends AbstractTaskTest {
    def "script not set"() {
        given:
        def task = project.tasks.create('simple', NodeTask)

        when:
        project.evaluate()
        task.exec()

        then:
        thrown(IllegalStateException)
    }

    def "exec node task"() {
        given:
        props.setProperty('os.name', 'Linux')
        execSpec = Mock(ExecSpec)
        nodeExtension.download = false

        def task = project.tasks.create('simple', NodeTask)
        task.args = ['a', 'b']
        task.options = ['c', 'd']
        task.environment = ['a': '1']
        task.ignoreExitValue = true

        def script = new File(projectDir, 'script.js')
        task.script = script
        task.workingDir = projectDir
        task.execOverrides = {}

        when:
        project.evaluate()
        task.exec()

        then:
        task.args == ['a', 'b']
        task.options == ['c', 'd']
        1 * execSpec.setIgnoreExitValue(true)
        1 * execSpec.setEnvironment({ it['a'] == '1' && containsPath(it) })
        1 * execSpec.setExecutable('node')
        1 * execSpec.setArgs(['c', 'd', script.absolutePath, 'a', 'b'])
    }

    def "execOverrides test"() {
        given:
        props.setProperty('os.name', 'Linux')
        execSpec = Mock(ExecSpec)
        nodeExtension.download = false

        def task = project.tasks.create('simple', NodeTask)
        task.ignoreExitValue = true

        def script = new File(projectDir, 'script.js')
        script.write("console.log(\"hello world\");")
        task.script = script
        task.workingDir = projectDir
        def baos = new ByteArrayOutputStream()
        task.execOverrides = { it.standardOutput = baos }

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setIgnoreExitValue(true)
        1 * execSpec.setArgs([script.absolutePath])
    }

    def "exec node task (download)"() {
        given:
        props.setProperty('os.name', 'Linux')
        nodeExtension.download = true
        execSpec = Mock(ExecSpec)

        def task = project.tasks.create('simple', NodeTask)
        def script = new File(projectDir, 'script.js')
        task.script = script

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setIgnoreExitValue(false)
        1 * execSpec.setEnvironment({ containsPath(it) })
        1 * execSpec.setArgs([script.absolutePath])
    }

    def "exec node task (windows)"() {
        given:
        props.setProperty('os.name', 'Windows')
        nodeExtension.download = false
        execSpec = Mock(ExecSpec)

        def task = project.tasks.create('simple', NodeTask)
        def script = new File(projectDir, 'script.js')

        task.args = ['a', 'b']
        task.options = ['c', 'd']
        task.script = script

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setIgnoreExitValue(false)
        1 * execSpec.setEnvironment({ containsPath(it) })
        1 * execSpec.setExecutable('node')
        1 * execSpec.setArgs(['c', 'd', script.absolutePath, 'a', 'b'])
    }

    def "exec node task (windows download)"() {
        given:
        props.setProperty('os.name', 'Windows')
        nodeExtension.download = true
        execSpec = Mock(ExecSpec)

        def task = project.tasks.create('simple', NodeTask)
        def script = new File(projectDir, 'script.js')
        task.script = script

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setEnvironment({ containsPath(it) })
        1 * execSpec.setIgnoreExitValue(false)
    }
}
