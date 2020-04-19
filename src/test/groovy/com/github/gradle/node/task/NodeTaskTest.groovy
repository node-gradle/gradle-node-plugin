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
        nodeExtension.download.set(false)

        def task = project.tasks.create('simple', NodeTask)
        task.args.set(['a', 'b'])
        task.options.set(['c', 'd'])
        task.environment.set(['a': '1'])
        task.ignoreExitValue.set(true)

        def script = new File(projectDir, 'script.js')
        task.script.set(script)
        task.workingDir.set(projectDir)

        when:
        project.evaluate()
        task.exec()

        then:
        task.args.get() == ['a', 'b']
        task.options.get() == ['c', 'd']
        1 * execSpec.setIgnoreExitValue(true)
        1 * execSpec.setEnvironment({ it['a'] == '1' && containsPath(it) })
        1 * execSpec.setExecutable('node')
        1 * execSpec.setArgs(['c', 'd', script.absolutePath, 'a', 'b'])
    }

    def "execOverrides test"() {
        given:
        props.setProperty('os.name', 'Linux')
        execSpec = Mock(ExecSpec)
        nodeExtension.download.set(false)

        def task = project.tasks.create('simple', NodeTask)
        task.ignoreExitValue.set(true)

        def script = new File(projectDir, 'script.js')
        script.write("console.log(\"hello world\");")
        task.script.set(script)
        task.workingDir.set(projectDir)
        def baos = new ByteArrayOutputStream()
        task.execOverrides { standardOutput = baos }

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
        nodeExtension.download.set(true)
        execSpec = Mock(ExecSpec)

        def task = project.tasks.create('simple', NodeTask)
        def script = new File(projectDir, 'script.js')
        task.script.set(script)

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
        nodeExtension.download.set(false)
        execSpec = Mock(ExecSpec)

        def task = project.tasks.create('simple', NodeTask)
        def script = new File(projectDir, 'script.js')

        task.args.set(['a', 'b'])
        task.options.set(['c', 'd'])
        task.script.set(script)

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
        nodeExtension.download.set(true)
        execSpec = Mock(ExecSpec)

        def task = project.tasks.create('simple', NodeTask)
        def script = new File(projectDir, 'script.js')
        task.script.set(script)

        when:
        project.evaluate()
        task.exec()

        then:
        1 * execSpec.setEnvironment({ containsPath(it) })
        1 * execSpec.setIgnoreExitValue(false)
    }
}
