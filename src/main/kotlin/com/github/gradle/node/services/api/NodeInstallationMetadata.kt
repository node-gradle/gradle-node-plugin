package com.github.gradle.node.services.api

import org.gradle.api.tasks.Input

interface NodeInstallationMetadata {
    @Input
    fun getVersion(): String
}