package com.github.gradle.node.npm.task

import org.gradle.api.Action
import org.gradle.process.ExecResult

interface TaskHooks {
    fun onSuccess(successHandler: Action<in ExecResult>)

    fun onFailure(failureHandler: Action<in Exception>)
}
