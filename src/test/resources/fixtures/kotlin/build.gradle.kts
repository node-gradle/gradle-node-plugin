import com.moowork.gradle.node.npm.NpmInstallTask
import com.moowork.gradle.node.npm.NpmTask
import com.moowork.gradle.node.npm.NpxTask
import com.moowork.gradle.node.task.NodeTask
import com.moowork.gradle.node.yarn.YarnInstallTask
import com.moowork.gradle.node.yarn.YarnTask

plugins {
    id("com.github.node-gradle.node")
}

node {
    version = "12.16.2"
    npmVersion = ""
    yarnVersion = ""
    npmInstallCommand = "install"
    distBaseUrl = "https://nodejs.org/dist"
    download = false
    workDir = file("${project.buildDir}/nodejs")
    npmWorkDir = file("${project.buildDir}/npm")
    yarnWorkDir = file("${project.buildDir}/yarn")
    nodeModulesDir = file("${project.projectDir}")
}

val npmInstallTask = tasks.named<NpmInstallTask>("npmInstall") {
    nodeModulesOutputFilter = closureOf<ConfigurableFileTree> {
        exclude("notExistingFile")
    }
}

tasks.named<YarnInstallTask>("yarn") {
    nodeModulesOutputFilter = closureOf<ConfigurableFileTree> {
        exclude("notExistingFile")
    }
}

val testTaskUsingNpx = tasks.register<NpxTask>("testNpx") {
    dependsOn(npmInstallTask)
    command = "mocha"
    setArgs(listOf("test", "--grep", "should say hello"))
    setIgnoreExitValue(false)
    setEnvironment(mapOf("MY_CUSTOM_VARIABLE" to "hello"))
    setWorkingDir(projectDir)
    setExecOverrides(closureOf<ExecSpec>({
        standardOutput = System.out
    }))
    inputs.dir("node_modules")
    inputs.file("package.json")
    inputs.dir("src")
    inputs.dir("test")
    outputs.upToDateWhen {
        true
    }
}

val testTaskUsingNpm = tasks.register<NpmTask>("testNpm") {
    dependsOn(npmInstallTask)
    setNpmCommand("run", "test")
    setArgs(listOf("test"))
    setIgnoreExitValue(false)
    setEnvironment(mapOf("MY_CUSTOM_VARIABLE" to "hello"))
    setWorkingDir(projectDir)
    setExecOverrides(closureOf<ExecSpec>({
        standardOutput = System.out
    }))
    inputs.dir("node_modules")
    inputs.file("package.json")
    inputs.dir("src")
    inputs.dir("test")
    outputs.upToDateWhen {
        true
    }
}

val testTaskUsingYarn = tasks.register<YarnTask>("testYarn") {
    dependsOn(npmInstallTask)
    setYarnCommand("run", "test")
    setArgs(listOf("test"))
    setIgnoreExitValue(false)
    setEnvironment(mapOf("MY_CUSTOM_VARIABLE" to "hello"))
    setWorkingDir(projectDir)
    setExecOverrides(closureOf<ExecSpec>({
        standardOutput = System.out
    }))
    inputs.dir("node_modules")
    inputs.file("package.json")
    inputs.dir("src")
    inputs.dir("test")
    outputs.upToDateWhen {
        true
    }
}

tasks.register<NodeTask>("run") {
    dependsOn(testTaskUsingNpx, testTaskUsingNpm, testTaskUsingYarn)
    script = file("src/main.js")
    setArgs(listOf("Bobby"))
    setIgnoreExitValue(false)
    setEnvironment(mapOf("MY_CUSTOM_VARIABLE" to "hello"))
    setWorkingDir(projectDir)
    setExecOverrides(closureOf<ExecSpec>({
        standardOutput = System.out
    }))
    inputs.dir("src")
    outputs.upToDateWhen {
        false
    }
}

val buildTaskUsingNpx = tasks.register<NpxTask>("buildNpx") {
    dependsOn(npmInstallTask)
    command = "babel"
    setArgs(listOf("src", "--out-dir", "${buildDir}/npx-output"))
    inputs.dir("src")
    outputs.dir("${buildDir}/npx-output")
}

val buildTaskUsingNpm = tasks.register<NpmTask>("buildNpm") {
    dependsOn(npmInstallTask)
    setNpmCommand("run", "build")
    setArgs(listOf("--", "--out-dir", "${buildDir}/npm-output"))
    inputs.dir("src")
    outputs.dir("${buildDir}/npm-output")
}

val buildTaskUsingYarn = tasks.register<YarnTask>("buildYarn") {
    dependsOn(npmInstallTask)
    setYarnCommand("run", "build")
    setArgs(listOf("--out-dir", "${buildDir}/yarn-output"))
    inputs.dir("src")
    outputs.dir("${buildDir}/yarn-output")
}

tasks.register<Zip>("package") {
    archiveName = "app.zip"
    destinationDir = buildDir
    from(buildTaskUsingNpx, buildTaskUsingNpm, buildTaskUsingYarn)
}
