package com.github.gradle.node.services

import com.github.gradle.node.NodeExtension
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

abstract class NpmPathTestTask : DefaultTask() {
    @get:Internal
    abstract val nodeRuntime: Property<NodeRuntime>

    @get:Internal
    val extension = NodeExtension[project]

    @TaskAction
    fun run() {
        logger.lifecycle("npm is at: ${nodeRuntime.get().getNpm(extension)}")
    }
}