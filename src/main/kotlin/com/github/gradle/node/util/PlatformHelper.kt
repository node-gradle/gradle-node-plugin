package com.github.gradle.node.util

import java.util.concurrent.Callable


@Deprecated(message = "This class is no longer needed, the computed output is stored in the NodeExtension")
internal class DefaultHelperExecution : HelperExecution {
    override fun exec(command: String, vararg args: String, timeout: Long): String {
        return execute(command, *args, timeout = timeout)
    }
}

@Deprecated(message = "This class is no longer needed, the computed output is stored in the NodeExtension")
interface HelperExecution {
    fun exec(command: String, vararg args: String, timeout: Long = 60): String
}

fun parsePlatform(name: String, arch: String, uname: () -> String): Platform {
    return Platform(parseOsName(name.toLowerCase()), parseOsArch(arch.toLowerCase(), uname))
}

fun parseOsName(name: String): String {
    return when {
        name.contains("windows") -> "win"
        name.contains("mac") -> "darwin"
        name.contains("linux") -> "linux"
        name.contains("freebsd") -> "linux"
        name.contains("sunos") -> "sunos"
        else -> error("Unsupported OS: $name")
    }
}

fun parseOsArch(arch: String, uname: Callable<String>): String {
    return when {
        /*
         * As Java just returns "arm" on all ARM variants, we need a system call to determine the exact arch. Unfortunately some JVMs say aarch32/64, so we need an additional
         * conditional. Additionally, the node binaries for 'armv8l' are called 'arm64', so we need to distinguish here.
         */
        arch == "arm" || arch.startsWith("aarch") -> uname.call()
            .mapIf({ it == "armv8l" || it == "aarch64" }) { "arm64" }
            .mapIf({ it == "x86_64" }) {"x64"}
        arch == "ppc64le" -> "ppc64le"
        arch == "s390x" -> "s390x"
        arch.contains("64") -> "x64"
        else -> "x86"
    }
}

open class PlatformHelper(private val execution: HelperExecution = DefaultHelperExecution()) {
    @get:Deprecated(message = "moved to NodeExtension")
    open val osName: String by lazy {
        parseOsName(property("os.name").toLowerCase())
    }

    @get:Deprecated(message = "moved to NodeExtension")
    open val osArch: String by lazy {
        val arch = property("os.arch").toLowerCase()
        val uname = { property("uname") }
        parseOsArch(arch, uname)
    }

    @get:Deprecated(message = "moved to NodeExtension",
        replaceWith = ReplaceWith("nodeExtension.resolvedPlatform.get().isWindows()"))
    open val isWindows: Boolean by lazy { osName == "win" }

    private fun property(name: String): String {
        return getSystemProperty(name) ?:
            // Added so that we can test osArch on Windows and on non-arm systems
            if (name == "uname") execution.exec("uname", "-m")
            else error("Unable to find a value for property [$name].")
    }

    open fun getSystemProperty(name: String): String? {
        return System.getProperty(name)
    }

    companion object {
        var INSTANCE = PlatformHelper()
    }
}

fun main(args: Array<String>) {
    val osName = System.getProperty("os.name")
    val osArch = System.getProperty("os.arch")
    val uname = { execute("uname", "-m", timeout = 10) }
    val platform = parsePlatform(osName, osArch, uname)

    println("Your os.name is: '${osName}' and is parsed as: '${platform.name}'")
    println("Your os.arch is: '${osArch}' and is parsed as: '${platform.arch}'")
    if (platform.isWindows()) {
        println("You're on windows (isWindows == true)")
    } else {
        println("You're not on windows (isWindows == false)")
    }
}
