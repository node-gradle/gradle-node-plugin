package com.github.gradle.node.services.api

import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import java.io.File

interface Node {
    @Nested
    fun getMetadata(): NodeInstallationMetadata

    @Internal
    fun getFile(): File
}