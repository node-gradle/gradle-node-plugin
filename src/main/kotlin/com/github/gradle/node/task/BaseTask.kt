package com.github.gradle.node.task

import com.github.gradle.node.util.PlatformHelper
import com.github.gradle.node.variant.VariantComputer
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal

abstract class BaseTask : DefaultTask() {

    @get:Internal
    var platformHelper = PlatformHelper.INSTANCE

    @get:Internal
    internal val variantComputer by lazy {
        VariantComputer(platformHelper)
    }

}