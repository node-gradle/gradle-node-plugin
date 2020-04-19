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
    version.set("12.16.2")
    npmVersion.set("")
    yarnVersion.set("")
    npmInstallCommand.set("install")
    distBaseUrl.set("https://nodejs.org/dist")
    download.set(false)
    workDir.set(file("${project.buildDir}/nodejs"))
    npmWorkDir.set(file("${project.buildDir}/npm"))
    yarnWorkDir.set(file("${project.buildDir}/yarn"))
    nodeModulesDir.set(file("${project.projectDir}"))
}

val npmInstallTask = tasks.named<NpmInstallTask>("npmInstall") {
    nodeModulesOutputFilter {
        exclude("notExistingFile")
    }
}

tasks.named<YarnInstallTask>("yarn") {
    nodeModulesOutputFilter {
        exclude("notExistingFile")
    }
}

val testTaskUsingNpx = tasks.register<NpxTask>("testNpx") {
    dependsOn(npmInstallTask)
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
    dependsOn(npmInstallTask)
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
    dependsOn(npmInstallTask)
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

tasks.register<NodeTask>("run") {
    dependsOn(testTaskUsingNpx, testTaskUsingNpm, testTaskUsingYarn)
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
    dependsOn(npmInstallTask)
    command.set("babel")
    args.set(listOf("src", "--out-dir", "${buildDir}/npx-output"))
    inputs.dir("src")
    outputs.dir("${buildDir}/npx-output")
}

val buildTaskUsingNpm = tasks.register<NpmTask>("buildNpm") {
    dependsOn(npmInstallTask)
    npmCommand.set(listOf("run", "build"))
    args.set(listOf("--", "--out-dir", "${buildDir}/npm-output"))
    inputs.dir("src")
    outputs.dir("${buildDir}/npm-output")
}

val buildTaskUsingYarn = tasks.register<YarnTask>("buildYarn") {
    dependsOn(npmInstallTask)
    yarnCommand.set(listOf("run", "build"))
    args.set(listOf("--out-dir", "${buildDir}/yarn-output"))
    inputs.dir("src")
    outputs.dir("${buildDir}/yarn-output")
}

tasks.register<Zip>("package") {
    // Using old deprecated properties to get it work with Gradle 5.0
    archiveName = "app.zip"
    destinationDir = buildDir
    from(buildTaskUsingNpx, buildTaskUsingNpm, buildTaskUsingYarn)
}
