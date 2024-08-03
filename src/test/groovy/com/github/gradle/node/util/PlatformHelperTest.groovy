package com.github.gradle.node.util

import spock.lang.Specification
import spock.lang.Unroll

class PlatformHelperTest extends Specification {

    @Unroll
    def "check os and architecture for #osProp (#archProp)"() {
        given:
        def platform = PlatformHelperKt.parsePlatform(osProp, archProp, {})

        expect:
        platform.name == osName
        platform.arch == osArch
        platform.windows == isWindows

        where:
        osProp       | archProp  || osName   | osArch    | isWindows
        'Windows 8'  | 'x86'     || 'win'    | 'x86'     | true
        'Windows 8'  | 'x86_64'  || 'win'    | 'x64'     | true
        'Windows 10' | 'x86_64'  || 'win'    | 'x64'     | true
        'Mac OS X'   | 'x86'     || 'darwin' | 'x86'     | false
        'Mac OS X'   | 'x86_64'  || 'darwin' | 'x64'     | false
        'Linux'      | 'x86'     || 'linux'  | 'x86'     | false
        'Linux'      | 'x86_64'  || 'linux'  | 'x64'     | false
        'Linux'      | 'ppc64le' || 'linux'  | 'ppc64le' | false
        'Linux'      | 's390x'   || 'linux'  | 's390x'   | false
        'SunOS'      | 'x86'     || 'sunos'  | 'x86'     | false
        'SunOS'      | 'x86_64'  || 'sunos'  | 'x64'     | false
    }

    @Unroll
    def "verify #osProp ARM handling #archProp (#unameProp)"() {
        given:
        def osType = PlatformHelperKt.parseOsType(osProp)
        def platform = PlatformHelperKt.parsePlatform(osType, archProp, { unameProp })

        expect:
        platform.name == osName
        platform.arch == osArch

        where:
        osProp       | archProp  || osName   | unameProp | osArch
        'Linux'      | 'arm'     || 'linux'  | 'armv7l'  | 'armv7l' // Raspberry Pi 3
        'Linux'      | 'arm'     || 'linux'  | 'armv8l'  | 'arm64'
        'Linux'      | 'aarch32' || 'linux'  | 'arm'     | 'arm'
        'Linux'      | 'aarch64' || 'linux'  | 'arm64'   | 'arm64'
        'Linux'      | 'aarch64' || 'linux'  | 'aarch64' | 'arm64'
        'Linux'      | 'ppc64le' || 'linux'  | 'ppc64le' | 'ppc64le'
        'Mac OS X'   | 'aarch32' || 'darwin' | 'arm'     | 'arm'
        'Mac OS X'   | 'aarch64' || 'darwin' | 'arm64'   | 'arm64'
        'Mac OS X'   | 'aarch64' || 'darwin' | 'aarch64' | 'arm64'
        'Mac OS X'   | 'aarch64' || 'darwin' | 'x86_64'  | 'x64' // This unfortunately happens see PR #204
        'Windows 10' | 'aarch64' || 'win'    | '12'      | 'arm64'
        'Windows 11' | 'aarch64' || 'win'    | '9'       | 'x64' // Not sure if this can actually happen
    }

    def "throw exception if unsupported os"() {
        when:
        PlatformHelperKt.parsePlatform('Nonsense', "", {})

        then:
        thrown(IllegalStateException)
    }
}
