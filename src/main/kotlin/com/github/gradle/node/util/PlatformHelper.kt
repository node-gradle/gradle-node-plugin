package com.github.gradle.node.util

import java.util.concurrent.Callable

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
        name.contains("aix") -> "aix"
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
        arch == "ppc64" -> "ppc64"
        arch == "ppc64le" -> "ppc64le"
        arch == "s390x" -> "s390x"
        arch.contains("64") -> "x64"
        else -> "x86"
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
