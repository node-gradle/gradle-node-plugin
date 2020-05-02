package com.github.gradle.node.task

import com.github.gradle.AbstractProjectTest
import com.github.gradle.node.NodeExtension
import com.github.gradle.node.util.PlatformHelper
import org.gradle.api.Action
import org.gradle.api.internal.ProcessOperations
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec

import java.lang.reflect.Field

abstract class AbstractTaskTest extends AbstractProjectTest {
    ExecResult execResult
    ExecSpec execSpec
    Properties props
    NodeExtension nodeExtension

    def setup() {
        props = new Properties()
        PlatformHelper.INSTANCE = new PlatformHelper(props)

        execResult = Mock(ExecResult)

        project.apply plugin: 'com.github.node-gradle.node'
        nodeExtension = NodeExtension.get(project)

        mockExec()
    }

    private void mockExec() {
        // Create mock to track exec calls
        ProcessOperations processOperations = Spy(project.getProcessOperations())
        processOperations.exec(_ as Action<ExecSpec>) >> { Action<ExecSpec> action ->
            action.execute(execSpec)
            return execResult
        }
        // Gradle does not allow us to easily inject our own services; manually override the ProcessOperations service
        Field processOperationsField = project.getClass().getDeclaredFields()
                .findAll { it.name ==~ /\w+processOperations\w+/ }
                .tap { assert it.size() == 1 }
                .first()
        processOperationsField.setAccessible(true)
        processOperationsField.set(project, processOperations)
    }

    protected containsPath(final Map<String, ?> env) {
        return env['PATH'] != null || env['Path'] != null
    }

    // Workaround a strange issue on Github actions macOS and Windows hosts
    protected List<String> fixAbsolutePaths(Iterable<String> path) {
        return path.collect { fixAbsolutePath(it) }
    }

    protected fixAbsolutePath(String path) {
        return path.replace('/private/', '/')
                .replace('C:\\Users\\runneradmin\\', 'C:\\Users\\RUNNER~1\\')
    }
}
