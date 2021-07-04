package com.github.gradle.node.util

import spock.lang.Specification
import spock.lang.Unroll

class PlatformHelperTest extends Specification {
    private Properties props
    private PlatformHelper helper

    def setup() {
        this.props = new Properties()
        PlatformHelper.INSTANCE = this.helper = new PlatformHelper(this.props)
    }

    @Unroll
    def "check os and architecture for #osProp (#archProp)"() {
        given:
        this.props.setProperty("os.name", osProp)
        this.props.setProperty("os.arch", archProp)

        expect:
        this.helper.getOsName() == osName
        this.helper.getOsArch() == osArch
        this.helper.isWindows() == isWindows
        this.helper.isSupported() == isSupported

        where:
        osProp      | archProp  | osName        | osArch    | isWindows | isSupported
        'Windows 8' | 'x86'     | 'win'         | 'x86'     | true      | true
        'Windows 8' | 'x86_64'  | 'win'         | 'x64'     | true      | true
        'Mac OS X'  | 'x86'     | 'darwin'      | 'x86'     | false     | true
        'Mac OS X'  | 'x86_64'  | 'darwin'      | 'x64'     | false     | true
        'Linux'     | 'x86'     | 'linux'       | 'x86'     | false     | true
        'Linux'     | 'x86_64'  | 'linux'       | 'x64'     | false     | true
        'Linux'     | 'ppc64le' | 'linux'       | 'ppc64le' | false     | true
        'SunOS'     | 'x86'     | 'sunos'       | 'x86'     | false     | true
        'SunOS'     | 'x86_64'  | 'sunos'       | 'x64'     | false     | true
        'FreeBSD'   | 'amd64'   | 'unsupported' | 'x64'     | false     | false
    }

    @Unroll
    def "verify ARM handling #archProp (#unameProp)"() {
        given:
        this.props.setProperty("os.name", "Linux")
        this.props.setProperty("os.arch", archProp)
        this.props.setProperty("uname", unameProp)

        expect:
        this.helper.getOsName() == "linux"
        this.helper.getOsArch() == osArch

        where:
        archProp  | unameProp | osArch
        'arm'     | 'armv7l'  | 'armv7l' // Raspberry Pi 3
        'arm'     | 'armv8l'  | 'arm64'
        'aarch32' | 'arm'     | 'arm'
        'aarch64' | 'arm64'   | 'arm64'
        'aarch64' | 'aarch64' | 'arm64'
        'ppc64le' | 'ppc64le' | 'ppc64le'
    }

    def "throw exception if unsupported os"() {
        given:
        this.props.setProperty("os.name", 'Nonsense')

        when:
        this.helper.failOnUnsupportedOs()

        then:
        thrown(IllegalStateException)
    }
}
