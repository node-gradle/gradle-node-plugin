import com.github.gradle.node.npm.proxy.ProxySettings
import com.github.gradle.node.npm.task.NpmTask
import com.github.gradle.node.npm.task.NpxTask
import com.github.gradle.node.pnpm.task.PnpmTask
import com.github.gradle.node.pnpm.task.PnpxTask
import com.github.gradle.node.task.NodeTask
import com.github.gradle.node.yarn.task.YarnTask

// This file shows how to use this plugin with the Kotlin DSL.
// All the properties are set, most of the time with the default value.
// /!\ We recommend to set only the values for which the default value is not satisfying.

plugins {
    // You have to specify the plugin version, for instance
    // id("com.github.node-gradle.node") version "3.0.0"
    // This works as is in the integration tests context
    id("com.github.node-gradle.node")
}

node {
    version.set("12.18.3")
    npmVersion.set("")
    yarnVersion.set("")
    pnpmVersion.set("")
    npmInstallCommand.set("install")
    distBaseUrl.set("https://nodejs.org/dist")
    download.set(false)
    workDir.set(file("${project.projectDir}/.cache/nodejs"))
    npmWorkDir.set(file("${project.projectDir}/.cache/npm"))
    yarnWorkDir.set(file("${project.projectDir}/.cache/yarn"))
    pnpmWorkDir.set(file("${project.projectDir}/.cache/pnpm"))
    nodeProjectDir.set(file("${project.projectDir}"))
    nodeProxySettings.set(ProxySettings.SMART)
}

tasks.npmInstall {
    nodeModulesOutputFilter {
        exclude("notExistingFile")
    }
}

tasks.yarn {
    nodeModulesOutputFilter {
        exclude("notExistingFile")
    }
}

tasks.pnpmInstall {
    nodeModulesOutputFilter {
        exclude("notExistingFile")
    }
}

val testTaskUsingNpx = tasks.register<NpxTask>("testNpx") {
    dependsOn(tasks.npmInstall)
    command.set("mocha")
    args.set(listOf("test", "--grep", "should say hello"))
    ignoreExitValue.set(false)
    environment.set(mapOf("MY_CUSTOM_VARIABLE" to "hello"))
    workingDir.set(projectDir)
    execOverrides {
        standardOutput = System.out
    }
    inputs.dir("node_modules")
    inputs.file("package.json")
    inputs.dir("src")
    inputs.dir("test")
    outputs.upToDateWhen {
        true
    }
}

val testTaskUsingNpm = tasks.register<NpmTask>("testNpm") {
    dependsOn(tasks.npmInstall)
    npmCommand.set(listOf("run", "test"))
    args.set(listOf("test"))
    ignoreExitValue.set(false)
    environment.set(mapOf("MY_CUSTOM_VARIABLE" to "hello"))
    workingDir.set(projectDir)
    execOverrides {
        standardOutput = System.out
    }
    inputs.dir("node_modules")
    inputs.file("package.json")
    inputs.dir("src")
    inputs.dir("test")
    outputs.upToDateWhen {
        true
    }
}

val testTaskUsingYarn = tasks.register<YarnTask>("testYarn") {
    dependsOn(tasks.npmInstall)
    yarnCommand.set(listOf("run", "test"))
    args.set(listOf("test"))
    ignoreExitValue.set(false)
    environment.set(mapOf("MY_CUSTOM_VARIABLE" to "hello"))
    workingDir.set(projectDir)
    execOverrides {
        standardOutput = System.out
    }
    inputs.dir("node_modules")
    inputs.file("package.json")
    inputs.dir("src")
    inputs.dir("test")
    outputs.upToDateWhen {
        true
    }
}

val testTaskUsingPnpx = tasks.register<PnpxTask>("testPnpx") {
    dependsOn(tasks.npmInstall)
    command.set("mocha")
    args.set(listOf("test", "--grep", "should say hello"))
    ignoreExitValue.set(false)
    environment.set(mapOf("MY_CUSTOM_VARIABLE" to "hello"))
    workingDir.set(projectDir)
    execOverrides {
        standardOutput = System.out
    }
    inputs.dir("node_modules")
    inputs.file("package.json")
    inputs.dir("src")
    inputs.dir("test")
    outputs.upToDateWhen {
        true
    }
}

val testTaskUsingPnpm = tasks.register<PnpmTask>("testPnpm") {
    dependsOn(tasks.npmInstall)
    pnpmCommand.set(listOf("run", "test"))
    args.set(listOf("test"))
    ignoreExitValue.set(false)
    environment.set(mapOf("MY_CUSTOM_VARIABLE" to "hello"))
    workingDir.set(projectDir)
    execOverrides {
        standardOutput = System.out
    }
    inputs.dir("node_modules")
    inputs.file("package.json")
    inputs.dir("src")
    inputs.dir("test")
    outputs.upToDateWhen {
        true
    }
}

tasks.register<NodeTask>("run") {
    dependsOn(testTaskUsingNpx, testTaskUsingNpm, testTaskUsingYarn, testTaskUsingPnpx, testTaskUsingPnpm)
    script.set(file("src/main.js"))
    args.set(listOf("Bobby"))
    ignoreExitValue.set(false)
    environment.set(mapOf("MY_CUSTOM_VARIABLE" to "hello"))
    workingDir.set(projectDir)
    execOverrides {
        standardOutput = System.out
    }
    inputs.dir("src")
    outputs.upToDateWhen {
        false
    }
}

val buildTaskUsingNpx = tasks.register<NpxTask>("buildNpx") {
    dependsOn(tasks.npmInstall)
    command.set("babel")
    args.set(listOf("src", "--out-dir", "${buildDir}/npx-output"))
    inputs.dir("src")
    outputs.dir("${buildDir}/npx-output")
}

val buildTaskUsingNpm = tasks.register<NpmTask>("buildNpm") {
    dependsOn(tasks.npmInstall)
    npmCommand.set(listOf("run", "build"))
    args.set(listOf("--", "--out-dir", "${buildDir}/npm-output"))
    inputs.dir("src")
    outputs.dir("${buildDir}/npm-output")
}

val buildTaskUsingYarn = tasks.register<YarnTask>("buildYarn") {
    dependsOn(tasks.npmInstall)
    yarnCommand.set(listOf("run", "build"))
    args.set(listOf("--out-dir", "${buildDir}/yarn-output"))
    inputs.dir("src")
    outputs.dir("${buildDir}/yarn-output")
}

val buildTaskUsingPnpx = tasks.register<PnpxTask>("buildPnpx") {
    dependsOn(tasks.npmInstall)
    command.set("babel")
    args.set(listOf("src", "--out-dir", "${buildDir}/pnpx-output"))
    inputs.dir("src")
    outputs.dir("${buildDir}/pnpx-output")
}

val buildTaskUsingPnpm = tasks.register<PnpmTask>("buildPnpm") {
    dependsOn(tasks.npmInstall)
    pnpmCommand.set(listOf("run", "build"))
    args.set(listOf("--", "--out-dir", "${buildDir}/pnpm-output"))
    inputs.dir("src")
    outputs.dir("${buildDir}/pnpm-output")
}

tasks.register<Zip>("package") {
    archiveFileName.set("app.zip")
    destinationDirectory.set(buildDir)
    from(buildTaskUsingNpx) {
        into("npx")
    }
    from(buildTaskUsingNpm) {
        into("npm")
    }
    from(buildTaskUsingYarn) {
        into("yarn")
    }
    from(buildTaskUsingPnpx) {
        into("pnpx")
    }
    from(buildTaskUsingPnpm) {
        into("pnpm")
    }
}
