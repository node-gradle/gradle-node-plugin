package com.github.gradle.node.task

import com.github.gradle.node.variant.VariantComputer
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.process.ExecResult

abstract class BaseTask : DefaultTask() {

    @get:Internal
    var result: ExecResult? = null

    @get:Internal
    internal val variantComputer by lazy {
        VariantComputer()
    }


}
