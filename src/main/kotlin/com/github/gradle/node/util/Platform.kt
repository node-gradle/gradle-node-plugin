package com.github.gradle.node.util

data class Platform(val name: String, val arch: String) {
    fun isWindows(): Boolean {
        return name == "win"
    }
}
