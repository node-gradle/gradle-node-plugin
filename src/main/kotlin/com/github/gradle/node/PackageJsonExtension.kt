package com.github.gradle.node

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.gradle.api.Project
import org.gradle.kotlin.dsl.property
import org.gradle.util.GradleVersion

/**
 * Provides a parsed view of package.json
 */
open class PackageJsonExtension(project: Project) {

    /**
     * Raw JsonNode returned by Jackson, this may be removed in a future release
     */
    val node = project.objects.property<JsonNode>()

    init {
        node.finalizeValueOnRead()
        node.set(project.provider { project.file("package.json").let(ObjectMapper()::readTree) })
    }

    val name = project.provider { node.get().get("name")?.asText() }

    val version = project.provider { node.get().get("version")?.asText() }

    val description = project.provider { node.get().get("description")?.asText() }

    val homepage = project.provider { node.get().get("homepage")?.asText() }

    val license = project.provider { node.get().get("license")?.asText() }

    val private = project.provider { node.get().get("private")?.asBoolean() }

    /**
     * Get the text value of a given field
     */
    fun get(name: String): String? {
        return node.get().get(name)?.asText()
    }

    /**
     * Get the boolean value of a given field
     */
    fun getBoolean(name: String): Boolean? {
        return node.get().get(name)?.asBoolean()
    }

    /**
     * Get the text value of a field containing nested objects
     *
     * e.g. <pre>{ "outer": { "inner": "nested } }</pre>
     */
    fun get(vararg name: String): String {
        return name.fold(node.get()) { acc, next -> acc.get(next) }.asText()
    }

    companion object {
        /**
         * Extension name in Gradle
         */
        const val NAME = "packageJson"
    }
}