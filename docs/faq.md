# FAQ

This page contains a collection of frequently asked questions.

# How to use this plugin using the Kotlin DSL?

See this [Kotlin DSL example](../src/test/resources/fixtures/kotlin/build.gradle.kts). It shows how to use all the 
configuration properties.

# How to avoid node/npm/yarn task execution if no changes in web files?

Just add to your bundle task filesets (in and out) which this task depends on:

```gradle
task bundle(type: YarnTask) {
    inputs.files(fileTree('node_modules'))
    inputs.files(fileTree('src'))
    inputs.file('package.json')
    inputs.file('webpack.config.js')
    
    outputs.dir('build/resources/static')
 
    dependsOn yarn_install
    args = ['run', 'build']
}
```

More info in [Gradle doc](https://docs.gradle.org/current/userguide/more_about_tasks.html#sec:up_to_date_checks)

# How do I use npm ci instead of npm install?

```gradle
node {
    npmInstallCommand = System.getenv("CI") ? 'ci' : 'install'
}
```

# How do I set log level for NPM install task?

This can be done adding some arguments to the already defined `npmInstall`-task. To set the log level to `silly` do this:

```gradle
npmInstall.args = ['--loglevel', 'silly']
```

# How do I specify a registry for the NPM setup task?

This can be done by adding to the arguments for the already defined `npmSetup` task.

```gradle
tasks.npmSetup {
    doFirst {
        args = args + ['--registry', 'http://myregistry.npm.com']
    }
}
```

You can also add any other arguments to this list that work with `npm install` i.e. more verbose modes.

# How do I run commands provided by npm packages?

Some packages (for instance Grunt, Gulp, Angular CLI, mocha...) provide some command line tools.

Instead of installing them globally, install them as `devDependencies` and run them using `npx` through the `NpxTask`.
Read more regarding the `NpxTask` [here](node.md).

Here are some examples:

## Grunt

Install the `grunt-cli` package:

```bash
npm install --save-dev grunt-cli
```

For a build described in `Gruntfile.js` with sources in the `src` directory and output in the `dist` directory:

```groovy
task buildWebapp(type: NpxTask) {
    dependsOn npmInstall
    npxCommand = "grunt"
    args = ["build"]
    inputs.file("Gruntfile.js")
    inputs.dir("src")
    inputs.dir("node_modules")
    outputs.dir("dist")
}
```

The task will only run if needed.

## Gulp

Install the `gulp-cli` package:

```bash
npm install gulp-cli
```

For a build described in `gulpfile.js` with sources in the `src` directory and output in the `dist` directory:

```groovy
task buildWebapp(type: NpxTask) {
    dependsOn npmInstall
    npxCommand = "gulp"
    args = ["build"]
    inputs.file("gulpfile.js")
    inputs.dir("src")
    inputs.dir("node_modules")
    outputs.dir("dist")
}
```

The task will only run if needed.

# How do I customize the way the processes are launched?

`NodeTask`, `NpmTask`, `NpxTask` and `YarnTask` are some wrappers around the core `Exec` task.
They have several parameters that enable to customize the way the corresponding command is launched.

The `ignoreExitValue` property enables to avoid the task failing if the process exit value is not `0`:
```gradle
task myScript(type: NodeTask) {
  script = file('src/scripts/my.js')
  ignoreExitValue = true
}
````

The `workingDirectory` option enables to change the working directory of the process. Note that some commands such as `npm` 
force the working directory to be the one in which the `package.json` file is located.
This option is most of the time useless.

```gradle
task myScript(type: NodeTask) {
  script = file('src/file.js')
  workingDir = file('./customWorkingDirectory')
}
````

The `environment` option enables to define some new environment variables or override some existing ones:

```gradle
task command(type: NpxTask) {
  command = 'aCommand'
  environment = ['CUSTOM_VARIABLE': 'Hello world']
}
````

The `execOverrides` option enables to customize all the other thinks that can be configured in an `ExecSpec` thanks to
a closure that takes the `ExecSpec` as parameter. Note that it is executed last, possibly overriding already set 
parameters such as the working directory.

```gradle
task myScript(type: NpmTask) {
  npmCommand = ['run', 'hello']
  execOverrides {
    // The it variable contains the `ExecSpec`
    it.ignoreExitValue = true
    // We can also omit it since "it" is implicit
    workingDir = file('./myWorkingDirectory')
    standardOutput = new FileOutputStream('logs/my.log')
  }
}
```

# How to use an HTTP or HTTPS proxy?

If Gradle is configured to use a proxy, the plugin will automatically configure `yarn` and `npm` to use it, unless it is already configured thanks to the dedicated environment variables. This behavior can be configured. Read more in the [proxy manual](./usage.md#using-a-proxy).

# How do I ignore some files of the `node_modules` directory that are modified by the build and prevent tasks from being up-to-date ?

`NpmInstallTask` and `YarnInstallTask` have an option that enables to exclude some files from the task's output.
Its type is a closure that contains a [`FileTree`](https://docs.gradle.org/current/javadoc/org/gradle/api/file/FileTree.html)
whose root directory is `node_modules`.
   
With npm:
```gradle
npmInstall {
  nodeModulesOutputFilter {
    exclude("package/package.json")
  }
}
```
    
Note that the `exclude` method comes from a [`FileTree`](https://docs.gradle.org/current/javadoc/org/gradle/api/file/FileTree.html).
It can be also written this way:
```gradle
nodeModulesOutputFilter {
  fileTree -> fileTree.exclude("package/package.json")
}
```
    
With yarn:
```gradle
yarn {
  nodeModulesOutputFilter {
    exclude("package/**")
    exclude("anotherPackage")
  }
}
```

Note: the Gradle's up-to-date checking is much slower when using this option. See issue #63.

# Is this plugin compatible with centralized repositories declaration?

Gradle 6.8 improves the dependency resolution management by adding a way to [centralize repositories declaration](https://docs.gradle.org/current/userguide/declaring_repositories.html#sub:centralized-repository-declaration). The `repositoryMode` option controls where the repositories can be declared (in the projects or in the settings).

By default, and when using the `PREFER_PROJECT` mode, the plugin works as expected. When using the `PREFER_SETTINGS` or `FAIL_ON_PROJECT_REPOS` modes, it will not work if it needs to download and install Node.js (i.e. if `download` is `true`). That's because it uses a repository to download the Node.js bundle and this configuration mode prevents it from adding a repository.

To get it work in this mode, you have to declare the repository yourself in the build settings and tell the plugin you declared the repository yourself.

In the `build.gradle` file:

```gradle
node {
    download = true
    // Do not declare the repository
    distBaseUrl = null
}
```

In the `settings.gradle/settings.gradle.kts` file:

```gradle
dependencyResolutionManagement {
    repositories {
        repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

        // Declare the Node.js download repository
        ivy {
            name = "Node.js"
            setUrl("https://nodejs.org/dist/")
            patternLayout {
                artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]")
            }
            metadataSources {
                artifact()
            }
            content {
                includeModule("org.nodejs", "node")
            }
        }
    }
}
```

See issue [#134](https://github.com/node-gradle/gradle-node-plugin/issues/134).
