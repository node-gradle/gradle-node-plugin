package com.github.gradle.node.util

import java.io.Serializable

data class Platform(
    val name: String,
    val arch: String,
): Serializable {
    fun isWindows(): Boolean {
        return name == "win"
    }
}
