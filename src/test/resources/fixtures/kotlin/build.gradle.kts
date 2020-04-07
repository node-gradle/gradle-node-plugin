import com.github.gradle.node.NodeExtension
import com.github.gradle.node.npm.task.NpmInstallTask
import com.github.gradle.node.npm.task.NpmTask
import com.github.gradle.node.npm.task.NpxTask
import com.github.gradle.node.task.NodeTask
import com.github.gradle.node.yarn.task.YarnInstallTask
import com.github.gradle.node.yarn.task.YarnTask

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

val npmInstallTask = tasks.withType(NpmInstallTask::class).named("npmInstall")
npmInstallTask.configure {
    nodeModulesOutputFilter = {
        exclude("notExistingFile")
    }
}

val yarnInstallTask = tasks.withType(YarnInstallTask::class).named("yarn")
yarnInstallTask.configure {
    nodeModulesOutputFilter = {
        exclude("notExistingFile")
    }
}

val testTaskUsingNpx = tasks.register<NpxTask>("testNpx") {
    dependsOn(npmInstallTask)
    command = "mocha"
    args = listOf("test", "--grep", "should say hello")
    ignoreExitValue = false
    environment = mapOf("MY_CUSTOM_VARIABLE" to "hello")
    workingDir = projectDir
    execOverrides = {
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
    dependsOn(npmInstallTask)
    npmCommand = listOf("run", "test")
    args = listOf("test")
    ignoreExitValue = false
    environment = mapOf("MY_CUSTOM_VARIABLE" to "hello")
    workingDir = projectDir
    execOverrides = {
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
    dependsOn(npmInstallTask)
    yarnCommand = listOf("run", "test")
    args = listOf("test")
    ignoreExitValue = false
    environment = mapOf("MY_CUSTOM_VARIABLE" to "hello")
    workingDir = projectDir
    execOverrides = {
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
    dependsOn(testTaskUsingNpx, testTaskUsingNpm, testTaskUsingYarn)
    script = file("src/main.js")
    args = listOf("Bobby")
    ignoreExitValue = false
    environment = mapOf("MY_CUSTOM_VARIABLE" to "hello")
    workingDir = projectDir
    execOverrides = {
        standardOutput = System.out
    }
    inputs.dir("src")
    outputs.upToDateWhen {
        false
    }
}

val buildTaskUsingNpx = tasks.register<NpxTask>("buildNpx") {
    dependsOn(npmInstallTask)
    command = "babel"
    args = listOf("src", "--out-dir", "${buildDir}/npx-output")
    inputs.dir("src")
    outputs.dir("${buildDir}/npx-output")
}

val buildTaskUsingNpm = tasks.register<NpmTask>("buildNpm") {
    dependsOn(npmInstallTask)
    // For some reason the --out-dir parameter is not passed to babel, so we use a dedicated command
    npmCommand = listOf("run", "buildNpm")
    args = listOf()
    inputs.dir("src")
    outputs.dir("${buildDir}/npm-output")
}

val buildTaskUsingYarn = tasks.register<YarnTask>("buildYarn") {
    dependsOn(npmInstallTask)
    yarnCommand = listOf("run", "build")
    args = listOf("--out-dir", "${buildDir}/yarn-output")
    inputs.dir("src")
    outputs.dir("${buildDir}/yarn-output")
}

tasks.register<Zip>("package") {
    // Using old deprecated properties to get it work with Gradle 5.0
    archiveName = "app.zip"
    destinationDir = buildDir
    from(buildTaskUsingNpx, buildTaskUsingNpm, buildTaskUsingYarn)
}
