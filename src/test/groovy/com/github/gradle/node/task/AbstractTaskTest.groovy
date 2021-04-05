package com.github.gradle.node.task

import com.github.gradle.AbstractProjectTest
import com.github.gradle.node.NodeExtension
import com.github.gradle.node.util.PlatformHelper
import com.github.gradle.node.util.ProjectApiHelper
import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec

import java.lang.reflect.Field

abstract class AbstractTaskTest extends AbstractProjectTest {
    ExecResult execResult
    ExecSpec execSpec
    Properties props
    NodeExtension nodeExtension
    PlatformHelper originalPlatformHelper

    def setup() {
        props = new Properties()
        originalPlatformHelper = PlatformHelper.INSTANCE
        PlatformHelper.INSTANCE = new PlatformHelper(props)

        execSpec = Mock(ExecSpec)
        execResult = Mock(ExecResult)

        project.apply plugin: 'com.github.node-gradle.node'
        nodeExtension = NodeExtension.get(project)
    }

    def cleanup() {
        PlatformHelper.INSTANCE = originalPlatformHelper
    }

    protected mockProjectApiHelperExec(Task task, String fieldName = "projectHelper") {
        Field projectApiHelperField = findProjectApiHelperTaskField(task, fieldName)
        projectApiHelperField.setAccessible(true)
        ProjectApiHelper projectApiHelper = Spy(projectApiHelperField.get(task)) as ProjectApiHelper
        projectApiHelper.exec(_ as Action<? extends ExecSpec>) >> { Action<? extends ExecSpec> action ->
            action.execute(execSpec)
            return execResult
        }
        projectApiHelperField.set(task, projectApiHelper)
        projectApiHelperField.setAccessible(false)
    }

    private static Field findProjectApiHelperTaskField(Task task, String fieldName) {
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
