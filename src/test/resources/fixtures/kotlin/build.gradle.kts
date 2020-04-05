import com.github.gradle.node.NodeExtension
import com.github.gradle.node.npm.NpmTask
import com.github.gradle.node.npm.NpxTask
import com.github.gradle.node.task.NodeTask
import com.github.gradle.node.yarn.YarnTask

plugins {
    id("com.github.node-gradle.node")
}

configure<NodeExtension> {
    version = "12.16.1"
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

val npmInstallTask = tasks.named("npmInstall")

val testTaskUsingNpx = tasks.register<NpxTask>("testNpx") {
    dependsOn(npmInstallTask)
    command = "mocha"
    args = listOf("--grep", "should say hello")
    ignoreExitValue = false
    environment = mapOf("MY_CUSTOM_VARIABLE" to "hello")
    workingDir = file("./")
    execOverrides {
        standardOutput = System.out
    }
    inputs.dir("node_modules")
    inputs.file("package.json")
    inputs.files("index.js", "test.js")
    outputs.upToDateWhen {
        true
    }
}

val testTaskUsingNpm = tasks.register<NpmTask>("testNpm") {
    dependsOn(npmInstallTask)
    npmCommand = listOf("run", "test")
    ignoreExitValue = false
    environment = mapOf("MY_CUSTOM_VARIABLE" to "hello")
    workingDir = file("./")
    execOverrides {
        standardOutput = System.out
    }
    inputs.dir("node_modules")
    inputs.file("package.json")
    inputs.files("index.js", "test.js")
    outputs.upToDateWhen {
        true
    }
}

val testTaskUsingYarn = tasks.register<YarnTask>("testYarn") {
    dependsOn(npmInstallTask)
    yarnCommand = listOf("run", "test")
    ignoreExitValue = false
    environment = mapOf("MY_CUSTOM_VARIABLE" to "hello")
    workingDir = file("./")
    execOverrides {
        standardOutput = System.out
    }
    inputs.dir("node_modules")
    inputs.file("package.json")
    inputs.files("index.js", "test.js")
    outputs.upToDateWhen {
        true
    }
}

tasks.register<NodeTask>("run") {
    dependsOn(testTaskUsingNpx, testTaskUsingNpm, testTaskUsingYarn)
    script = file("main.js")
    args = listOf("Bobby")
    ignoreExitValue = false
    environment = mapOf("MY_CUSTOM_VARIABLE" to "hello")
    workingDir = file("./")
    execOverrides {
        standardOutput = System.out
    }
    inputs.files("index.js", "main.js")
    outputs.upToDateWhen {
        false
    }
}
