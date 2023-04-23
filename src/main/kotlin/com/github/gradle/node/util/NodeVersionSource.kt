package com.github.gradle.node.util

import com.github.gradle.node.variant.VariantComputer
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.process.ExecOperations
import javax.inject.Inject

abstract class NodeVersionSource: ValueSource<Provider<String>, NodeVersionSource.NodeVersionSourceParameters> {
    @get:Inject
    abstract val execOperations: ExecOperations

    interface NodeVersionSourceParameters : ValueSourceParameters {
        val nodeVersion: Property<String>
    }

    override fun obtain(): Provider<String> {
        val platformHelper = PlatformHelper(GradleHelperExecution(execOperations))
        val variantComputer = VariantComputer(platformHelper)
        return variantComputer.computeNodeArchiveDependency(parameters.nodeVersion)
    }
}

