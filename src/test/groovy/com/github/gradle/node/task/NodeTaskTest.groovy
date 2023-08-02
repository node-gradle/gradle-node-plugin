package com.github.gradle.node.task

import com.github.gradle.node.util.PlatformHelperKt

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
        nodeExtension.resolvedPlatform.set(PlatformHelperKt.parsePlatform("Linux", "x86_64", {}))
        nodeExtension.download.set(false)

        def task = project.tasks.create('simple', NodeTask)
        mockProjectApiHelperExec(task)
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
        nodeExtension.resolvedPlatform.set(PlatformHelperKt.parsePlatform("Linux", "x86_64", {}))
        nodeExtension.download.set(false)

        def task = project.tasks.create('simple', NodeTask)
        mockProjectApiHelperExec(task)
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
        nodeExtension.resolvedPlatform.set(PlatformHelperKt.parsePlatform("Linux", "x86_64", {}))
        nodeExtension.download.set(true)

        def task = project.tasks.create('simple', NodeTask)
        mockProjectApiHelperExec(task)
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
        nodeExtension.resolvedPlatform.set(PlatformHelperKt.parsePlatform("Windows", "x86_64", {}))
        nodeExtension.download.set(false)

        def task = project.tasks.create('simple', NodeTask)
        mockProjectApiHelperExec(task)
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
        nodeExtension.resolvedPlatform.set(PlatformHelperKt.parsePlatform("Windows", "x86_64", {}))
        nodeExtension.download.set(true)

        def task = project.tasks.create('simple', NodeTask)
        mockProjectApiHelperExec(task)
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
