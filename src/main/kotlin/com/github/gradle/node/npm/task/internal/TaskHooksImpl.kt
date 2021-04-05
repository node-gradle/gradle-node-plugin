package com.github.gradle.node.npm.task.internal

import com.github.gradle.node.npm.task.TaskHooks
import org.gradle.api.Action
import org.gradle.process.ExecResult

data class TaskHooksImpl(
    var successHandler: Action<in ExecResult>? = null,
    var failureHandler: Action<in ExecResult>? = null,
    var execFinishedHandler: Action<in ExecResult>? = null
) : TaskHooks {
    override fun onSuccess(successHandler: Action<in ExecResult>) {
        this.successHandler = successHandler
    }

    override fun onFailure(failureHandler: Action<in ExecResult>) {
        this.failureHandler = failureHandler
    }

    override fun onExecFinished(execFinishedHandler: Action<in ExecResult>) {
        this.execFinishedHandler = execFinishedHandler
    }
}
