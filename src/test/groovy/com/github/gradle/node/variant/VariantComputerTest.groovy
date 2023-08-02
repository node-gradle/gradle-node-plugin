package com.github.gradle.node.variant

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.util.KotlinUtilsKt
import com.github.gradle.node.util.Platform
import com.github.gradle.node.util.TestablePlatformHelper
import com.github.gradle.node.util.PlatformHelperKt
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Unroll

class VariantComputerTest extends Specification {
    /* OS dependant line separator */

    static final String PS = File.separator

    /* Relative base path for nodejs installation */

    static final String NODE_BASE_PATH = "${PS}.gradle${PS}node${PS}"

    private Properties props

    def setup() {
        props = new Properties()
    }

    @Unroll
    def "test variant on windows (#version #osArch)"() {
        given:
        def project = ProjectBuilder.builder().build()

        props.setProperty("os.name", "Windows 8")
        props.setProperty("os.arch", osArch)
        def platform = getPlatform("Windows 8", osArch)
        def platformHelper = new TestablePlatformHelper(props)

        def nodeExtension = new NodeExtension(project)
        nodeExtension.resolvedPlatform.set(platform)
        nodeExtension.download.set(true)
        nodeExtension.version.set(version)
        nodeExtension.workDir.set(project.layout.projectDirectory.dir(".gradle/node"))

        def nodeDir = "node-v${version}-win-${osArch}".toString()
        def depName = "org.nodejs:node:${version}:win-${osArch}@zip".toString()

        def variantComputer = new VariantComputer(platformHelper)

        when:
        def computedArchiveDependency = VariantComputerKt.computeNodeArchiveDependency(nodeExtension)
        def resolvedNodeDir = VariantComputerKt.computeNodeDir(nodeExtension)
        def computedNodeBinDir = variantComputer.computeNodeBinDir(resolvedNodeDir)
        def computedNodeExec = variantComputer.computeNodeExec(nodeExtension, computedNodeBinDir)
        def computedNpmScriptFile = variantComputer.computeNpmScriptFile(resolvedNodeDir, "npm")
        def computedNpxScriptFile = variantComputer.computeNpmScriptFile(resolvedNodeDir, "npx")

        then:
        computedArchiveDependency.get() == depName

        resolvedNodeDir.get().toString().endsWith(NODE_BASE_PATH + nodeDir)
        computedNodeBinDir.get().toString().endsWith(NODE_BASE_PATH + nodeDir)
        computedNodeExec.get().toString().endsWith(NODE_BASE_PATH + nodeDir + PS + "node.exe")
        computedNpmScriptFile.get().toString().endsWith(NODE_BASE_PATH + nodeDir + PS + "node_modules${PS}npm${PS}bin${PS}npm-cli.js")
        computedNpxScriptFile.get().toString().endsWith(NODE_BASE_PATH + nodeDir + PS + "node_modules${PS}npm${PS}bin${PS}npx-cli.js")

        where:
        version | osArch
        "4.5.0" | "x86"
        "6.2.1" | "x86"
        "7.0.0" | "x86"
        "4.5.0" | "x64"
        "6.2.1" | "x64"
        "7.0.0" | "x64"
    }

    @Unroll
    def "test variant on non-windows (#osName, #osArch)"() {
        given:
        props.setProperty("os.name", osName)
        props.setProperty("os.arch", osArch)
        def platform = getPlatform(osName, osArch)
        def platformHelper = new TestablePlatformHelper(props)

        def project = ProjectBuilder.builder().build()
        def nodeExtension = new NodeExtension(project)
        nodeExtension.resolvedPlatform.set(platform)
        nodeExtension.download.set(true)
        nodeExtension.version.set('5.12.0')
        nodeExtension.workDir.set(project.layout.projectDirectory.dir(".gradle/node"))

        def variantComputer = new VariantComputer(platformHelper)

        when:
        def computedArchiveDependency = VariantComputerKt.computeNodeArchiveDependency(nodeExtension)
        def resolvedNodeDir = VariantComputerKt.computeNodeDir(nodeExtension)
        def computedNodeBinDir = variantComputer.computeNodeBinDir(resolvedNodeDir)
        def computedNodeExec = variantComputer.computeNodeExec(nodeExtension, computedNodeBinDir)
        def computedNpmScriptFile = variantComputer.computeNpmScriptFile(resolvedNodeDir, "npm")
        def computedNpxScriptFile = variantComputer.computeNpmScriptFile(resolvedNodeDir, "npx")

        then:
        computedArchiveDependency.get() == depName

        resolvedNodeDir.get().toString().endsWith(NODE_BASE_PATH + nodeDir)
        computedNodeBinDir.get().toString().endsWith(NODE_BASE_PATH + nodeDir + PS + 'bin')
        computedNodeExec.get().toString().endsWith(NODE_BASE_PATH + nodeDir + PS + "bin${PS}node")
        computedNpmScriptFile.get().toString().endsWith(NODE_BASE_PATH + nodeDir + PS + "lib${PS}node_modules${PS}npm${PS}bin${PS}npm-cli.js")
        computedNpxScriptFile.get().toString().endsWith(NODE_BASE_PATH + nodeDir + PS + "lib${PS}node_modules${PS}npm${PS}bin${PS}npx-cli.js")

        where:
        osName     | osArch    | nodeDir                      | depName
        'Linux'    | 'x86'     | 'node-v5.12.0-linux-x86'     | 'org.nodejs:node:5.12.0:linux-x86@tar.gz'
        'Linux'    | 'x86_64'  | 'node-v5.12.0-linux-x64'     | 'org.nodejs:node:5.12.0:linux-x64@tar.gz'
        'Linux'    | 'ppc64le' | 'node-v5.12.0-linux-ppc64le' | 'org.nodejs:node:5.12.0:linux-ppc64le@tar.gz'
        'Mac OS X' | 'x86'     | 'node-v5.12.0-darwin-x86'    | 'org.nodejs:node:5.12.0:darwin-x86@tar.gz'
        'Mac OS X' | 'x86_64'  | 'node-v5.12.0-darwin-x64'    | 'org.nodejs:node:5.12.0:darwin-x64@tar.gz'
        'FreeBSD'  | 'x86'     | 'node-v5.12.0-linux-x86'     | 'org.nodejs:node:5.12.0:linux-x86@tar.gz'
        'FreeBSD'  | 'x86_64'  | 'node-v5.12.0-linux-x64'     | 'org.nodejs:node:5.12.0:linux-x64@tar.gz'
        'SunOS'    | 'x86'     | 'node-v5.12.0-sunos-x86'     | 'org.nodejs:node:5.12.0:sunos-x86@tar.gz'
        'SunOS'    | 'x86_64'  | 'node-v5.12.0-sunos-x64'     | 'org.nodejs:node:5.12.0:sunos-x64@tar.gz'
    }

    @Unroll
    def "test variant on ARM (#osName, #osArch, #sysOsArch)"() {
        given:
        props.setProperty("os.name", osName)
        props.setProperty("os.arch", osArch)
        props.setProperty("uname", sysOsArch)
        def platform = getPlatform(osName, osArch, sysOsArch)
        def platformHelper = new TestablePlatformHelper(props)

        def project = ProjectBuilder.builder().build()
        def nodeExtension = new NodeExtension(project)
        nodeExtension.resolvedPlatform.set(platform)
        nodeExtension.download.set(true)
        nodeExtension.version.set('5.12.0')
        nodeExtension.workDir.set(project.layout.projectDirectory.dir(".gradle/node"))

        def variantComputer = new VariantComputer(platformHelper)

        when:
        def computedArchiveDependency = VariantComputerKt.computeNodeArchiveDependency(nodeExtension)
        def resolvedNodeDir = VariantComputerKt.computeNodeDir(nodeExtension)
        def computedNodeBinDir = variantComputer.computeNodeBinDir(resolvedNodeDir)
        def computedNodeExec = variantComputer.computeNodeExec(nodeExtension, computedNodeBinDir)
        def computedNpmScriptFile = variantComputer.computeNpmScriptFile(resolvedNodeDir, "npm")
        def computedNpxScriptFile = variantComputer.computeNpmScriptFile(resolvedNodeDir, "npx")

        then:
        computedArchiveDependency.get() == depName

        resolvedNodeDir.get().toString().endsWith(NODE_BASE_PATH + nodeDir)
        computedNodeBinDir.get().toString().endsWith(NODE_BASE_PATH + nodeDir + PS + 'bin')
        computedNodeExec.get().toString().endsWith(NODE_BASE_PATH + nodeDir + PS + "bin${PS}node")
        computedNpmScriptFile.get().toString().endsWith(NODE_BASE_PATH + nodeDir + PS + "lib${PS}node_modules${PS}npm${PS}bin${PS}npm-cli.js")
        computedNpxScriptFile.get().toString().endsWith(NODE_BASE_PATH + nodeDir + PS + "lib${PS}node_modules${PS}npm${PS}bin${PS}npx-cli.js")

        where:
        osName  | osArch    | sysOsArch | nodeDir                      | depName
        'Linux' | 'arm'     | 'armv6l'  | 'node-v5.12.0-linux-armv6l'  | 'org.nodejs:node:5.12.0:linux-armv6l@tar.gz'
        'Linux' | 'arm'     | 'armv7l'  | 'node-v5.12.0-linux-armv7l'  | 'org.nodejs:node:5.12.0:linux-armv7l@tar.gz'
        'Linux' | 'arm'     | 'arm64'   | 'node-v5.12.0-linux-arm64'   | 'org.nodejs:node:5.12.0:linux-arm64@tar.gz'
        'Linux' | 'ppc64le' | 'ppc64le' | 'node-v5.12.0-linux-ppc64le' | 'org.nodejs:node:5.12.0:linux-ppc64le@tar.gz'
    }

    @Unroll
    def "test npm paths on windows (npm: #npmVersion, download: #download)"() {
        given:
        props.setProperty("os.name", "Windows 8")
        props.setProperty("os.arch", "x86")
        def platform = getPlatform("Windows 8", "x86")
        def platformHelper = new TestablePlatformHelper(props)
        def project = ProjectBuilder.builder().build()

        def nodeExtension = new NodeExtension(project)
        nodeExtension.resolvedPlatform.set(platform)
        nodeExtension.download.set(download)
        nodeExtension.npmVersion.set(npmVersion)

        def variantComputer = new VariantComputer(platformHelper)

        when:
        def resolvedNodeDir = VariantComputerKt.computeNodeDir(nodeExtension)
        def computedNpmDir = variantComputer.computeNpmDir(nodeExtension, resolvedNodeDir)
        def computedNodeBinDir = variantComputer.computeNodeBinDir(resolvedNodeDir)
        def computedNpmBinDir = variantComputer.computeNpmBinDir(computedNpmDir)
        def computedNpmExec = variantComputer.computeNpmExec(nodeExtension, computedNpmBinDir)
        def computedNpxExec = variantComputer.computeNpxExec(nodeExtension, computedNpmBinDir)

        def npmDir = resolvedNodeDir.get()
        def npm = nodeExtension.npmCommand.get() + ".cmd"
        def npx = nodeExtension.npxCommand.get() + ".cmd"

        if (npmVersion != "") {
            npmDir = nodeExtension.npmWorkDir.get().dir("npm-v${npmVersion}")
        }

        if (download) {
            npm = npmDir.dir(npm).asFile.toString()
            npx = npmDir.dir(npx).asFile.toString()
        }

        then:
        computedNpmDir.get() == npmDir
        computedNpmExec.get() == npm
        computedNpxExec.get() == npx

        // if no version use node paths
        npmVersion != "" || computedNpmDir.get() == resolvedNodeDir.get()
        npmVersion != "" || computedNpmBinDir.get() == computedNodeBinDir.get()

        where:
        download | npmVersion
        true     | "4.0.2"
        true     | ""
        false    | "4.0.2"
        false    | ""
    }

    @Unroll
    def "test npm paths on non-windows (npm: #npmVersion, download: #download)"() {
        given:
        props.setProperty("os.name", "Linux")
        props.setProperty("os.arch", "x86")
        def platformHelper = new TestablePlatformHelper(props)
        def platform = getPlatform("Linux", "x86")
        def project = ProjectBuilder.builder().build()

        def nodeExtension = new NodeExtension(project)
        nodeExtension.resolvedPlatform.set(platform)
        nodeExtension.download.set(download)
        nodeExtension.npmVersion.set(npmVersion)

        def variantComputer = new VariantComputer(platformHelper)

        when:
        def resolvedNodeDir = VariantComputerKt.computeNodeDir(nodeExtension)
        def computedNpmDir = variantComputer.computeNpmDir(nodeExtension, resolvedNodeDir)
        def computedNodeBinDir = variantComputer.computeNodeBinDir(resolvedNodeDir)
        def computedNpmBinDir = variantComputer.computeNpmBinDir(computedNpmDir)
        def computedNpmExec = variantComputer.computeNpmExec(nodeExtension, computedNpmBinDir)
        def computedNpxExec = variantComputer.computeNpxExec(nodeExtension, computedNpmBinDir)

        def npmDir = resolvedNodeDir.get()
        def npm = nodeExtension.npmCommand.get()
        def npx = nodeExtension.npxCommand.get()

        if (npmVersion != "") {
            npmDir = nodeExtension.npmWorkDir.get().dir("npm-v${npmVersion}".toString())
        }

        def npmBinDir = npmDir.dir("bin")

        if (download) {
            npm = npmBinDir.file(npm).toString()
            npx = npmBinDir.file(npx).toString()
        }

        then:
        computedNpmDir.get() == npmDir
        computedNpmExec.get() == npm
        computedNpxExec.get() == npx

        // if no version use node paths
        npmVersion != "" || computedNpmDir.get() == resolvedNodeDir.get()
        npmVersion != "" || computedNpmBinDir.get() == computedNodeBinDir.get()

        where:
        download | npmVersion
        true     | "4.0.2"
        true     | ""
        false    | "4.0.2"
        false    | ""
    }

    private Platform getPlatform(String osName, String osArch, uname = null) {
        return PlatformHelperKt.parsePlatform(osName, osArch, { uname })
    }
}
