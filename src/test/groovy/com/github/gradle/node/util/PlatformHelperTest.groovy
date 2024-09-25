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
        osProp      | archProp  | osName   | osArch    | isWindows
        'Windows 8' | 'x86'     | 'win'    | 'x86'     | true
        'Windows 8' | 'x86_64'  | 'win'    | 'x64'     | true
        'Mac OS X'  | 'x86'     | 'darwin' | 'x86'     | false
        'Mac OS X'  | 'x86_64'  | 'darwin' | 'x64'     | false
        'Linux'     | 'x86'     | 'linux'  | 'x86'     | false
        'Linux'     | 'x86_64'  | 'linux'  | 'x64'     | false
        'Linux'     | 'ppc64le' | 'linux'  | 'ppc64le' | false
        'Linux'     | 's390x'   | 'linux'  | 's390x'   | false
        'SunOS'     | 'x86'     | 'sunos'  | 'x86'     | false
        'SunOS'     | 'x86_64'  | 'sunos'  | 'x64'     | false
        'AIX'       | 'ppc64'   | 'aix'    | 'ppc64'   | false
    }

    @Unroll
    def "verify ARM handling #archProp (#unameProp)"() {
        given:
        def platform = PlatformHelperKt.parsePlatform("Linux", archProp, { unameProp })

        expect:
        platform.name == "linux"
        platform.arch == osArch

        where:
        archProp  | unameProp | osArch
        'arm'     | 'armv7l'  | 'armv7l' // Raspberry Pi 3
        'arm'     | 'armv8l'  | 'arm64'
        'aarch32' | 'arm'     | 'arm'
        'aarch64' | 'arm64'   | 'arm64'
        'aarch64' | 'aarch64' | 'arm64'
        'ppc64le' | 'ppc64le' | 'ppc64le'
    }

    @Unroll
    def "verify ARM handling Mac OS #archProp (#unameProp)"() {
        given:
        def platform = PlatformHelperKt.parsePlatform("Mac OS X", archProp, { unameProp })

        expect:
        platform.name == "darwin"
        platform.arch == osArch

        where:
        archProp  | unameProp | osArch
        'aarch32' | 'arm'     | 'arm'
        'aarch64' | 'arm64'   | 'arm64'
        'aarch64' | 'aarch64' | 'arm64'
        'aarch64' | 'x86_64'  | 'x64' // This shouldn't really happen but according to PR #204 it does
    }

    def "throw exception if unsupported os"() {
        when:
        PlatformHelperKt.parsePlatform('Nonsense', "", {})

        then:
        thrown(IllegalStateException)
    }
}
