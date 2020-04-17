package com.github.gradle.node.variant

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.util.PlatformHelper
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
        PlatformHelper.INSTANCE = new PlatformHelper(props)
    }

    @Unroll
    def "test variant on windows version <4 (#osArch)"() {
        given:
        def project = ProjectBuilder.builder().build()

        props.setProperty("os.name", "Windows 8")
        props.setProperty("os.arch", osArch)

        def nodeExtension = new NodeExtension(project)
        nodeExtension.download = true
        nodeExtension.version = '5.12.0'
        nodeExtension.workDir = new File('.gradle/node').absoluteFile
        def variantComputer = new VariantComputer()

        when:
        def computedDependency = variantComputer.computeDependency(nodeExtension)
        def computedNodeDir = variantComputer.computeNodeDir(nodeExtension)
        def computeNodeBinDir = variantComputer.computeNodeBinDir(computedNodeDir)
        def computedNodeExec = variantComputer.computeNodeExec(nodeExtension, computeNodeBinDir)
        def computedNpmScriptFile = variantComputer.computeNpmScriptFile(computedNodeDir, "npm")
        def computedNpxScriptFile = variantComputer.computeNpmScriptFile(computedNodeDir, "npx")

        then:
        computedDependency.exeDependency == exeDependency
        computedDependency.archiveDependency == 'org.nodejs:node:5.12.0:linux-x86@tar.gz'
        computedNodeDir.toString().endsWith(NODE_BASE_PATH + nodeDir)
        computeNodeBinDir.toString().endsWith(NODE_BASE_PATH + nodeDir)
        computedNodeExec.toString().endsWith(NODE_BASE_PATH + nodeDir + PS + "node.exe")
        computedNpmScriptFile.toString().endsWith(NODE_BASE_PATH + nodeDir + PS + "node_modules${PS}npm${PS}bin${PS}npm-cli.js")
        computedNpxScriptFile.toString().endsWith(NODE_BASE_PATH + nodeDir + PS + "node_modules${PS}npm${PS}bin${PS}npx-cli.js")

        where:
        osArch   | nodeDir                | exeDependency
        'x86'    | 'node-v5.12.0-win-x86' | 'org.nodejs:win-x86/node:5.12.0@exe'
        'x86_64' | 'node-v5.12.0-win-x64' | 'org.nodejs:win-x64/node:5.12.0@exe'
    }

    @Unroll
    def "test variant on windows version 4.+ with exe (#osArch)"() {
        given:
        def project = ProjectBuilder.builder().build()

        props.setProperty("os.name", "Windows 8")
        props.setProperty("os.arch", osArch)

        def nodeExtension = new NodeExtension(project)
        nodeExtension.download = true
        nodeExtension.version = '4.0.0'
        nodeExtension.workDir = new File('.gradle/node').absoluteFile
        def variantComputer = new VariantComputer()

        when:
        def computedDependency = variantComputer.computeDependency(nodeExtension)
        def computedNodeDir = variantComputer.computeNodeDir(nodeExtension)
        def computedNodeBinDir = variantComputer.computeNodeBinDir(computedNodeDir)
        def computedNodeExec = variantComputer.computeNodeExec(nodeExtension, computedNodeBinDir)
        def computedNpmScriptFile = variantComputer.computeNpmScriptFile(computedNodeDir, "npm")
        def computedNpxScriptFile = variantComputer.computeNpmScriptFile(computedNodeDir, "npx")

        then:
        computedDependency.exeDependency == exeDependency
        computedDependency.archiveDependency == 'org.nodejs:node:4.0.0:linux-x86@tar.gz'

        computedNodeDir.toString().endsWith(NODE_BASE_PATH + nodeDir)
        computedNodeBinDir.toString().endsWith(NODE_BASE_PATH + nodeDir)
        computedNodeExec.toString().endsWith(NODE_BASE_PATH + nodeDir + PS + "node.exe")
        computedNpmScriptFile.toString().endsWith(NODE_BASE_PATH + nodeDir + PS + "node_modules${PS}npm${PS}bin${PS}npm-cli.js")
        computedNpxScriptFile.toString().endsWith(NODE_BASE_PATH + nodeDir + PS + "node_modules${PS}npm${PS}bin${PS}npx-cli.js")

        where:
        osArch   | nodeDir               | exeDependency
        'x86'    | 'node-v4.0.0-win-x86' | 'org.nodejs:win-x86/node:4.0.0@exe'
        'x86_64' | 'node-v4.0.0-win-x64' | 'org.nodejs:win-x64/node:4.0.0@exe'
    }

    @Unroll
    def "test variant on windows without exe (#version #osArch)"() {
        given:
        def project = ProjectBuilder.builder().build()

        props.setProperty("os.name", "Windows 8")
        props.setProperty("os.arch", osArch)

        def nodeExtension = new NodeExtension(project)
        nodeExtension.download = true
        nodeExtension.version = version
        nodeExtension.workDir = new File('.gradle/node').absoluteFile

        def nodeDir = "node-v${version}-win-${osArch}".toString()
        def depName = "org.nodejs:node:${version}:win-${osArch}@zip".toString()

        def variantComputer = new VariantComputer()

        when:
        def computedDependency = variantComputer.computeDependency(nodeExtension)
        def computedNodeDir = variantComputer.computeNodeDir(nodeExtension)
        def computedNodeBinDir = variantComputer.computeNodeBinDir(computedNodeDir)
        def computedNodeExec = variantComputer.computeNodeExec(nodeExtension, computedNodeBinDir)
        def computedNpmScriptFile = variantComputer.computeNpmScriptFile(computedNodeDir, "npm")
        def computedNpxScriptFile = variantComputer.computeNpmScriptFile(computedNodeDir, "npx")

        then:
        computedDependency.exeDependency == null
        computedDependency.archiveDependency == depName

        computedNodeDir.toString().endsWith(NODE_BASE_PATH + nodeDir)
        computedNodeBinDir.toString().endsWith(NODE_BASE_PATH + nodeDir)
        computedNodeExec.toString().endsWith(NODE_BASE_PATH + nodeDir + PS + "node.exe")
        computedNpmScriptFile.toString().endsWith(NODE_BASE_PATH + nodeDir + PS + "node_modules${PS}npm${PS}bin${PS}npm-cli.js")
        computedNpxScriptFile.toString().endsWith(NODE_BASE_PATH + nodeDir + PS + "node_modules${PS}npm${PS}bin${PS}npx-cli.js")

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

        def project = ProjectBuilder.builder().build()
        def nodeExtension = new NodeExtension(project)
        nodeExtension.download = true
        nodeExtension.version = '5.12.0'
        nodeExtension.workDir = new File('.gradle/node').absoluteFile

        def variantComputer = new VariantComputer()

        when:
        def computedDependency = variantComputer.computeDependency(nodeExtension)
        def computedNodeDir = variantComputer.computeNodeDir(nodeExtension)
        def computedNodeBinDir = variantComputer.computeNodeBinDir(computedNodeDir)
        def computedNodeExec = variantComputer.computeNodeExec(nodeExtension, computedNodeBinDir)
        def computedNpmScriptFile = variantComputer.computeNpmScriptFile(computedNodeDir, "npm")
        def computedNpxScriptFile = variantComputer.computeNpmScriptFile(computedNodeDir, "npx")

        then:
        computedDependency.exeDependency == null
        computedDependency.archiveDependency == depName

        computedNodeDir.toString().endsWith(NODE_BASE_PATH + nodeDir)
        computedNodeBinDir.toString().endsWith(NODE_BASE_PATH + nodeDir + PS + 'bin')
        computedNodeExec.toString().endsWith(NODE_BASE_PATH + nodeDir + PS + "bin${PS}node")
        computedNpmScriptFile.toString().endsWith(NODE_BASE_PATH + nodeDir + PS + "lib${PS}node_modules${PS}npm${PS}bin${PS}npm-cli.js")
        computedNpxScriptFile.toString().endsWith(NODE_BASE_PATH + nodeDir + PS + "lib${PS}node_modules${PS}npm${PS}bin${PS}npx-cli.js")

        where:
        osName     | osArch   | nodeDir                   | depName
        'Linux'    | 'x86'    | 'node-v5.12.0-linux-x86'  | 'org.nodejs:node:5.12.0:linux-x86@tar.gz'
        'Linux'    | 'x86_64' | 'node-v5.12.0-linux-x64'  | 'org.nodejs:node:5.12.0:linux-x64@tar.gz'
        'Mac OS X' | 'x86'    | 'node-v5.12.0-darwin-x86' | 'org.nodejs:node:5.12.0:darwin-x86@tar.gz'
        'Mac OS X' | 'x86_64' | 'node-v5.12.0-darwin-x64' | 'org.nodejs:node:5.12.0:darwin-x64@tar.gz'
        'FreeBSD'  | 'x86'    | 'node-v5.12.0-linux-x86'  | 'org.nodejs:node:5.12.0:linux-x86@tar.gz'
        'FreeBSD'  | 'x86_64' | 'node-v5.12.0-linux-x64'  | 'org.nodejs:node:5.12.0:linux-x64@tar.gz'
        'SunOS'    | 'x86'    | 'node-v5.12.0-sunos-x86'  | 'org.nodejs:node:5.12.0:sunos-x86@tar.gz'
        'SunOS'    | 'x86_64' | 'node-v5.12.0-sunos-x64'  | 'org.nodejs:node:5.12.0:sunos-x64@tar.gz'
    }

    @Unroll
    def "test variant on ARM (#osName, #osArch, #sysOsArch)"() {
        given:
        props.setProperty("os.name", osName)
        props.setProperty("os.arch", osArch)

        def project = ProjectBuilder.builder().build()
        def nodeExtension = new NodeExtension(project)
        nodeExtension.download = true
        nodeExtension.version = '5.12.0'
        nodeExtension.workDir = new File('.gradle/node').absoluteFile

        PlatformHelper platformHelperSpy = (PlatformHelper) Spy(PlatformHelper, constructorArgs: [props])
        platformHelperSpy.osArch >> { sysOsArch }
        def variantComputer = new VariantComputer(platformHelperSpy)

        when:
        def computedDependency = variantComputer.computeDependency(nodeExtension)
        def computedNodeDir = variantComputer.computeNodeDir(nodeExtension)
        def computedNodeBinDir = variantComputer.computeNodeBinDir(computedNodeDir)
        def computedNodeExec = variantComputer.computeNodeExec(nodeExtension, computedNodeBinDir)
        def computedNpmScriptFile = variantComputer.computeNpmScriptFile(computedNodeDir, "npm")
        def computedNpxScriptFile = variantComputer.computeNpmScriptFile(computedNodeDir, "npx")

        then:
        computedDependency.exeDependency == null
        computedDependency.archiveDependency == depName

        computedNodeDir.toString().endsWith(NODE_BASE_PATH + nodeDir)
        computedNodeBinDir.toString().endsWith(NODE_BASE_PATH + nodeDir + PS + 'bin')
        computedNodeExec.toString().endsWith(NODE_BASE_PATH + nodeDir + PS + "bin${PS}node")
        computedNpmScriptFile.toString().endsWith(NODE_BASE_PATH + nodeDir + PS + "lib${PS}node_modules${PS}npm${PS}bin${PS}npm-cli.js")
        computedNpxScriptFile.toString().endsWith(NODE_BASE_PATH + nodeDir + PS + "lib${PS}node_modules${PS}npm${PS}bin${PS}npx-cli.js")

        where:
        osName  | osArch | sysOsArch | nodeDir                     | depName
        'Linux' | 'arm'  | 'armv6l'  | 'node-v5.12.0-linux-armv6l' | 'org.nodejs:node:5.12.0:linux-armv6l@tar.gz'
        'Linux' | 'arm'  | 'armv7l'  | 'node-v5.12.0-linux-armv7l' | 'org.nodejs:node:5.12.0:linux-armv7l@tar.gz'
        'Linux' | 'arm'  | 'arm64'   | 'node-v5.12.0-linux-arm64'  | 'org.nodejs:node:5.12.0:linux-arm64@tar.gz'
    }

    @Unroll
    def "test npm paths on windows (npm: #npmVersion, download: #download)"() {
        given:
        props.setProperty("os.name", "Windows 8")
        props.setProperty("os.arch", "x86")
        def project = ProjectBuilder.builder().build()

        def nodeExtension = new NodeExtension(project)
        nodeExtension.download = download
        nodeExtension.npmVersion = npmVersion

        def variantComputer = new VariantComputer()

        when:
        def computedNodeDir = variantComputer.computeNodeDir(nodeExtension)
        def computedNpmDir = variantComputer.computeNpmDir(nodeExtension, computedNodeDir)
        def computedNodeBinDir = variantComputer.computeNodeBinDir(computedNodeDir)
        def computedNpmBinDir = variantComputer.computeNpmBinDir(computedNpmDir)
        def computedNpmExec = variantComputer.computeNpmExec(nodeExtension, computedNpmBinDir)
        def computedNpxExec = variantComputer.computeNpxExec(nodeExtension, computedNpmBinDir)

        def npmDir = computedNodeDir
        def npm = nodeExtension.npmCommand + ".cmd"
        def npx = nodeExtension.npxCommand + ".cmd"

        if (npmVersion != "") {
            npmDir = new File(nodeExtension.npmWorkDir, "npm-v${npmVersion}".toString())
        }

        if (download) {
            npm = new File(npmDir, npm).toString()
            npx = new File(npmDir, npx).toString()
        }

        then:
        computedNpmDir == npmDir
        computedNpmExec == npm
        computedNpxExec == npx

        // if no version use node paths
        npmVersion != "" || computedNpmDir == computedNodeDir
        npmVersion != "" || computedNpmBinDir == computedNodeBinDir

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
        def project = ProjectBuilder.builder().build()

        def nodeExtension = new NodeExtension(project)
        nodeExtension.download = download
        nodeExtension.npmVersion = npmVersion

        def variantComputer = new VariantComputer()

        when:
        def computedNodeDir = variantComputer.computeNodeDir(nodeExtension)
        def computedNpmDir = variantComputer.computeNpmDir(nodeExtension, computedNodeDir)
        def computedNodeBinDir = variantComputer.computeNodeBinDir(computedNodeDir)
        def computedNpmBinDir = variantComputer.computeNpmBinDir(computedNpmDir)
        def computedNpmExec = variantComputer.computeNpmExec(nodeExtension, computedNpmBinDir)
        def computedNpxExec = variantComputer.computeNpxExec(nodeExtension, computedNpmBinDir)

        def npmDir = computedNodeDir
        def npm = nodeExtension.npmCommand
        def npx = nodeExtension.npxCommand

        if (npmVersion != "") {
            npmDir = new File(nodeExtension.npmWorkDir, "npm-v${npmVersion}".toString())
        }

        def npmBinDir = new File(npmDir, "bin")

        if (download) {
            npm = new File(npmBinDir, npm).toString()
            npx = new File(npmBinDir, npx).toString()
        }

        then:
        computedNpmDir == npmDir
        computedNpmExec == npm
        computedNpxExec == npx

        // if no version use node paths
        npmVersion != "" || computedNpmDir == computedNodeDir
        npmVersion != "" || computedNpmBinDir == computedNodeBinDir

        where:
        download | npmVersion
        true     | "4.0.2"
        true     | ""
        false    | "4.0.2"
        false    | ""
    }
}
