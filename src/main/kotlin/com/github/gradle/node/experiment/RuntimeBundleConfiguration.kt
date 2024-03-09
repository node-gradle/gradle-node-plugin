package com.github.gradle.node.experiment

import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import javax.inject.Inject

abstract class RuntimeBundleConfiguration @Inject constructor(@get:Internal val name: String) {
    @get:Input
    abstract val version: Property<String>
    @get:Internal
    abstract val enabled: Property<Boolean>
}
