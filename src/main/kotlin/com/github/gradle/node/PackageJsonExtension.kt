package com.github.gradle.node

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.gradle.api.Project
import org.gradle.kotlin.dsl.property
import org.gradle.util.GradleVersion


open class PackageJsonExtension(project: Project) {
    val node = project.objects.property<JsonNode>()

    init {
        if (GradleVersion.current() >= GradleVersion.version("6.1")) {
            node.finalizeValueOnRead()
        }
        node.set(project.provider { project.file("package.json").let(ObjectMapper()::readTree) })
    }

    val name = project.provider { node.get().get("name").asText() }

    val version = project.provider { node.get().get("version").asText() }

    val description = project.provider { node.get().get("description").asText() }

    val homepage = project.provider { node.get().get("homepage").asText() }

    val license = project.provider { node.get().get("license").asText() }

    val private = project.provider { node.get().get("private").asBoolean() }

    fun get(name: String): String {
        return node.get().get(name).asText()
    }

    fun getBoolean(name: String): Boolean {
        return node.get().get(name).asBoolean()
    }

    fun get(vararg name: String): String {
        return name.fold(node.get()) { acc, next -> acc.get(next) }.asText()
    }

    companion object {
        /**
         * Extension name in Gradle
         */
        const val NAME = "package.json"
    }
}