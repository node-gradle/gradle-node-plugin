package com.moowork.gradle.node.util

import java.util.*

open class PlatformHelper constructor(private val props: Properties = System.getProperties()) {

    open val osName: String by lazy {
        val name = property("os.name").toLowerCase()
        when {
            name.contains("windows") -> "win"
            name.contains("mac") -> "darwin"
            name.contains("linux") -> "linux"
            name.contains("freebsd") -> "linux"
            name.contains("sunos") -> "sunos"
            else -> error("Unsupported OS: $name")
        }
    }

    open val osArch: String by lazy {
        val arch = property("os.arch").toLowerCase()
        when {
            /*
             * As Java just returns "arm" on all ARM variants, we need a system call to determine the exact arch. Unfortunately some JVMs say aarch32/64, so we need an additional
             * conditional. Additionally, the node binaries for 'armv8l' are called 'arm64', so we need to distinguish here.
             */
            arch == "arm" || arch.startsWith("aarch") -> execute("uname", "-m").mapIf({ it == "armv8l" }) { "arm64" }
            arch.contains("64") -> "x64"
            else -> "x86"
        }
    }

    open val isWindows: Boolean by lazy { osName == "win" }

    private fun property(name: String): String {
        val value = props.getProperty(name)
        return value ?: System.getProperty(name) ?: error("Unable to find a value for property [$name].")
    }

    companion object {
        var INSTANCE = PlatformHelper()
    }
}
