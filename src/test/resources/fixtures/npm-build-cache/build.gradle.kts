import com.github.gradle.node.npm.task.NpmTask
import org.gradle.api.tasks.PathSensitivity

plugins {
    id("com.github.node-gradle.node")
    base
}

node {
    version.set("16.13.0")

    download.set(true)
    distBaseUrl.set(null as String?)

    //fastNpmInstall.set(true)
}

tasks.npmInstall {
    val enableNpmInstallCaching =
        providers.gradleProperty("enableNpmInstallCaching").map(String::toBoolean).orElse(false)
    inputs.property("enableNpmInstallCaching", enableNpmInstallCaching)
    outputs.cacheIf { enableNpmInstallCaching.get() }
}

val distributionDirectory = layout.projectDirectory.dir("dist")

val npmRunBuild by tasks.registering(NpmTask::class) {
    dependsOn(tasks.npmInstall)

    npmCommand.set(listOf("run", "build"))

    inputs.dir("src/main")
        .withPropertyName("sources")
        .withPathSensitivity(PathSensitivity.RELATIVE)

    inputs.file("package.json")
        .withPropertyName("packageJson")
        .withPathSensitivity(PathSensitivity.RELATIVE)

    outputs.dir(distributionDirectory)
        .withPropertyName("distributionDirectory")

    outputs.cacheIf("always cache, this task produces files") { true }
}

tasks.assemble {
    dependsOn(npmRunBuild)
}

tasks.clean {
    delete(distributionDirectory)
    delete("node_modules")
}
