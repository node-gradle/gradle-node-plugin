package com.github.gradle.node.task

import com.github.gradle.AbstractProjectTest
import com.github.gradle.node.NodeExtension
import com.github.gradle.node.util.ProjectApiHelper
import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.process.ExecOperations
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec

import java.lang.reflect.Field

abstract class AbstractTaskTest extends AbstractProjectTest {
    ExecResult execResult
    ExecSpec execSpec
    NodeExtension nodeExtension

    def setup() {
        execSpec = Mock(ExecSpec)
        execResult = Mock(ExecResult)

        project.apply plugin: 'com.github.node-gradle.node'
        nodeExtension = NodeExtension.get(project)
    }

    protected mockExecOperationsExec(Task task, String fieldName = "execOperations") {
        Field execOperationsField = findExecOperationsTaskField(task, fieldName)
        execOperationsField.setAccessible(true)
        ExecOperations execOperations = Spy(execOperationsField.get(task)) as ExecOperations
        execOperations.exec(_ as Action<ExecSpec>) >> { Action<ExecSpec> action ->
            action.execute(execSpec)
            return execResult
        }
        execOperationsField.set(task, execOperations)
        execOperationsField.setAccessible(false)
    }

    private static Field findExecOperationsTaskField(Task task, String fieldName) {
        Class<?> type = task.getClass()
        while (type != null) {
            try {
                return type.getDeclaredField(fieldName)
            } catch (NoSuchFieldException _) {
                type = type.getSuperclass()
            }
        }
        throw new IllegalStateException("No ${fieldName} field found in class ${task.getClass()}")
    }

    protected static containsPath(final Map<String, ?> env) {
        return env['PATH'] != null || env['Path'] != null
    }

    // Workaround a strange issue on Github actions macOS and Windows hosts
    protected static List<String> fixAbsolutePaths(Iterable<String> path) {
        return path.collect { fixAbsolutePath(it) }
    }

    protected static fixAbsolutePath(String path) {
        return path.replace('/private/', '/')
                .replace('C:\\Users\\runneradmin\\', 'C:\\Users\\RUNNER~1\\')
    }
}
