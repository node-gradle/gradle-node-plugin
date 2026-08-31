package com.github.gradle.node.npm.exec

import org.gradle.api.GradleException
import org.gradle.process.ExecResult

class NpmExecResult internal constructor(
    val exitValue: Int,
    val failure: GradleException?,
    val capturedOutput: String,
) {

    internal fun asExecResult(): ExecResult = object : ExecResult {

        override fun assertNormalExitValue(): ExecResult {
            if (failure != null) {
                throw failure
            }
            return this
        }

        override fun getExitValue(): Int = exitValue

        override fun rethrowFailure(): ExecResult {
            assertNormalExitValue()
            return this
        }
    }

    override fun toString(): String = "NpmExecResult(exitValue=$exitValue, failure=$failure)"
}
