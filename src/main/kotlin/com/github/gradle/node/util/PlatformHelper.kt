package com.github.gradle.node.util

import java.util.concurrent.Callable

internal enum class OsType(val osName: String) {
    WINDOWS("win"),
    MAC("darwin"),
    LINUX("linux"),
    FREEBSD("linux"), // https://github.com/node-gradle/gradle-node-plugin/issues/178
    SUN("sunos"),
}

internal fun parsePlatform(type: OsType, arch: String, uname: () -> String): Platform {
    val osArch = if (type == OsType.WINDOWS) parseWindowsArch(arch.toLowerCase(), uname)
                 else parseOsArch(arch.toLowerCase(), uname)
    return Platform(type.osName, osArch)
}

internal fun parseOsType(type: String): OsType {
    val name = type.toLowerCase()
    return when {
        name.contains("windows") -> OsType.WINDOWS
        name.contains("mac") -> OsType.MAC
        name.contains("linux") -> OsType.LINUX
        name.contains("freebsd") -> OsType.FREEBSD
        name.contains("sunos") -> OsType.SUN
        else -> error("Unsupported OS: $name")
    }
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

fun parseWindowsArch(arch: String, uname: Callable<String>): String {
    //
    return when {
        arch.startsWith("aarch") || arch.startsWith("arm")
        -> {
            val wmiArch = uname.call()
            return when (wmiArch) {
                /*
                 * Parse Win32_Processor.Architectures to real processor type
                 *
                 * Table from https://learn.microsoft.com/en-us/windows/win32/api/sysinfoapi/ns-sysinfoapi-system_info#members
                 */
                "12" -> "arm64"
                "9" -> "x64"
                // "6" -> "IA64"
                // "5" -> "arm" // 32-bit
                "0" -> "x86"
                // "0xffff" -> "Unknown"
                else -> error("Unexpected Win32_Processor.Architecture: $arch")
            }
        }
        arch.contains("64") -> "x64"
        else -> "x86"
    }
}

fun main(args: Array<String>) {
    val osName = System.getProperty("os.name")
    val osArch = System.getProperty("os.arch")

    val osType = parseOsType(osName)
    val uname = {
        val args = if (osType == OsType.WINDOWS) {
            listOf("powershell", "-NoProfile", "-Command", "(Get-WmiObject Win32_Processor).Architecture")
        } else {
            listOf("uname", "-m")
        }
        execute(*args.toTypedArray(), timeout = 10)
    }
    val platform = parsePlatform(osName, osArch, uname)

    println("Your os.name is: '${osName}' and is parsed as: '${platform.name}'")
    println("Your os.arch is: '${osArch}' and is parsed as: '${platform.arch}'")
    if (platform.isWindows()) {
        println("You're on windows (isWindows == true)")
    } else {
        println("You're not on windows (isWindows == false)")
    }
}
