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

If your network requires using a proxy to access to the internet, you probably already 
[configured Gradle to use the proxy](https://docs.gradle.org/current/userguide/build_environment.html#sec:accessing_the_web_via_a_proxy).
In this case, the plugin will automatically apply the proxy configuration to all `npm` and `yarn` commands.

Note that:
* This does not work with `npx` since it does not support proxy usage
* This does work either for all `node` commands. It's the `node` script's responsibility to use the proxy or not
* For `npm` and `yarn`, it will only work for network requests done directly by the tool (for instance downloading a 
dependency). This will not work if you run a Node.js script for instance via `npm run`.

To disable this behavior, set `useGradleProxySettings` to `ProxySetting.OFF` in the `node` extension. In this case, the plugin will
do nothing regarding proxy and you may want to configure it manually, for instance using the `.npmrc` file as 
explained [here](https://www.devtech101.com/2016/07/21/how-to-set-npm-proxy-settings-in-npmrc/) for `npm`.

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
