package com.github.gradle.node.util

import java.util.*

open class PlatformHelper constructor(private val props: Properties = System.getProperties()) {
    open val osName: String by lazy {
        val name = property("os.name").toLowerCase()
        when {
            name.contains("windows") -> "win"
            name.contains("mac") -> "darwin"
            name.contains("linux") -> "linux"
            name.contains("sunos") -> "sunos"
            else -> "unsupported"
        }
    }

    open val osArch: String by lazy {
        val arch = property("os.arch").toLowerCase()
        when {
            /*
             * As Java just returns "arm" on all ARM variants, we need a system call to determine the exact arch. Unfortunately some JVMs say aarch32/64, so we need an additional
             * conditional. Additionally, the node binaries for 'armv8l' are called 'arm64', so we need to distinguish here.
             */
            arch == "arm" || arch.startsWith("aarch") -> property("uname")
                .mapIf({ it == "armv8l" || it == "aarch64" }) { "arm64" }
            arch == "ppc64le" -> "ppc64le"
            arch.contains("64") -> "x64"
            else -> "x86"
        }
    }

    open val isWindows: Boolean by lazy { osName == "win" }

    open val isSupported: Boolean by lazy { osName != "unsupported" }

    fun failOnUnsupportedOs() {
        if (!isSupported) {
            error("Unsupported OS")
        }
    }

    private fun property(name: String): String {
        val value = props.getProperty(name)
        return value ?: System.getProperty(name) ?:
            // Added so that we can test osArch on Windows and on non-arm systems
            if (name == "uname") execute("uname", "-m")
            else error("Unable to find a value for property [$name].")
    }

    companion object {
        var INSTANCE = PlatformHelper()
    }
}

fun main(args: Array<String>) {
    println("Your os.name is: '${System.getProperty("os.name")}' and is parsed as: ${PlatformHelper.INSTANCE.osName}")
    println("Your os.arch is: '${System.getProperty("os.arch")}' and is parsed as: ${PlatformHelper.INSTANCE.osArch}")
    if (!PlatformHelper.INSTANCE.isSupported) {
        println("Your platform is \"unsupported\" (isSupported == false)")
        println("Your platform does not support 'download = true' as there's no official Node.js binaries" +
                " being published for it. You can still use the plugin, but you need to install Node.js manually")
    }
    if (PlatformHelper.INSTANCE.isWindows) {
        println("You're on windows (isWindows == true)")
    } else {
        println("You're not on windows (isWindows == false)")
    }
}