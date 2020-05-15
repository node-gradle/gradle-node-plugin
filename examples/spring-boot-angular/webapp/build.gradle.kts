import com.github.gradle.node.npm.task.NpxTask

plugins {
  java
  // You need to specify the version to use (we don't need to do it in the demo since we use the plugin source)
  id("com.github.node-gradle.node")
}

val npmInstallTask = tasks.npmInstall

val lintTask = tasks.register<NpxTask>("lintWebapp") {
  command.set("ng")
  args.set(listOf("lint"))
  dependsOn(npmInstallTask)
  inputs.dir("src")
  inputs.dir("node_modules")
  inputs.files("angular.json", "browserslist", "tsconfig.json", "tsconfig.app.json", "tsconfig.spec.json",
    "tslint.json")
  outputs.upToDateWhen { true }
}

val buildTask = tasks.register<NpxTask>("buildWebapp") {
  command.set("ng")
  args.set(listOf("build", "--prod"))
  dependsOn(npmInstallTask, lintTask)
  inputs.dir(project.fileTree("src").exclude("**/*.spec.ts"))
  inputs.dir("node_modules")
  inputs.files("angular.json", "browserslist", "tsconfig.json", "tsconfig.app.json", "tsconfig.spec.json")
  outputs.dir("${project.buildDir}/webapp")
}

val testTask = tasks.register<NpxTask>("testWebapp") {
  command.set("ng")
  args.set(listOf("test"))
  dependsOn(npmInstallTask, lintTask)
  inputs.dir("src")
  inputs.dir("node_modules")
  inputs.files("angular.json", "browserslist", "tsconfig.json", "tsconfig.app.json", "tsconfig.spec.json")
  outputs.upToDateWhen { true }
}

tasks.build {
  dependsOn(lintTask, buildTask, testTask)
}


sourceSets {
  java {
    main {
      resources {
        srcDir(buildTask)
      }
    }
  }
}
