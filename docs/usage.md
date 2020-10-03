# Usage

This plugin enables you to run any [Node.js](https://nodejs.org) script as part of your build. It does
not require Node.js (or NPM) being installed on your system but it is able to use them.

By default, it will use the globally installed tools (Node.js, npm and Yarn).
If it is specified in the configuration, it is able to download and manage Node.js distributions,
unpack them into your local `.gradle` directory and use them from there.
It also automatically installs [npm](https://www.npmjs.com/) when installing Node.js.

Is is also able to install [Yarn](https://yarnpkg.com/) by downloading it from a npm registry.

The version of each tool to use can be specified in the configuration.

To start using the plugin, add this into your `build.gradle` 
file (see [Installing](installation.md) for details):


```gradle
plugins {
  id "com.github.node-gradle.node" version "2.2.4"
}
```

## Running a Node.js Script

To use this plugin you have to define some tasks in your `build.gradle` file. If you have a Node.js 
script in `src/scripts/my.js`, then you can execute this by defining the following Gradle task:

```gradle
task myScript(type: NodeTask) {
  script = file('src/scripts/my.js')
}
```

You can also add arguments, like this:

```gradle
task myScript(type: NodeTask) {
  script = file('src/scripts/my.js')
  args = ['arg1', 'arg2']
}
```

You can add Node.js options like this:

```gradle
task myScript(type: NodeTask) {
  script = file('src/scripts/my.js')
  options = ['--node-option', '--another-node-option']
}
```

When executing this task for the first time, it will run a `nodeSetup` task that downloads Node.js 
(for your platform) and NPM (Node Package Manager) if on Windows (other platforms include 
it into the distribution).

## Executing `npm` Tasks

When adding the node plugin, you will have a `npmInstall` task already added. This task will 
execute `npm install` and installs all dependencies in `package.json`. It will only run when changes 
are made to `package.json`, `npm-shrinkwrap.json`, `package-lock.json` or `node_modules`. Execute it like this:

```bash
$ gradle npmInstall
```

Keep in mind that this task is **not** equivalent to `npm_install`.
The *only* task that will respect settings like `npmInstallCommand` is `npmInstall`.

All npm command can also be invoked using underscore notation based on a gradle rule:

```bash
$ gradle npm_update
$ gradle npm_list
$ gradle npm_cache_clean
...
```

These however are not shown when running gradle tasks, as they generated dynamically. However they can 
be used for dependency declarations, such as:

```gradle
npm_audit.dependsOn(npm_cache_clean)
```

More arguments can be passed via the `build.gradle` file:

```gradle
npm_update {
  args = ['--production', '--loglevel', 'warn']
}
```

If you want to extend the tasks more or create custom variants, you can extend the class `NpmTask`:

```gradle
task installExpress(type: NpmTask) {
  // install the express package only
  args = ['install', 'express', '--save-dev']
}
```

## Executing `npm` Commands via `npx`

[As of 5.2](https://blog.npmjs.org/post/162869356040/introducing-npx-an-npm-package-runner),
 `npm` is bundled with a new command called [`npx`](https://www.npmjs.com/package/npx) which is aimed at running CLI 
 commands from NPM packages. 

It enables to execute `npm` commands without needing to declare them as a `script` in the `package.json` file and run 
thanks to the `npm run` command.

It does not require the command to be locally or globally installed. If the command is not already installed, the 
corresponding package is installed then the command is run. In this case, it is necessary to indicate the package
 name instead of the command name.
 
The `NpxTask` is able to execute some `npx` commands. It depends on the `npmSetup` to ensure `npx` is available. 

To generate a new Angular project with the `ng` command coming from `@angular/cli` which is not installed 
(note that we can specify the version):

```gradle
task generateAngularApp(type: NpxTask) {
  command = '@angular/cli@8.3.2'
  args = ['new', 'myApp']
}
```

To build an Angular application with `@angular/cli` locally installed:

```gradle
task buildAngularApp(type: NpxTask) {
  dependsOn npmInstall
  command = 'ng'
  args = ['build', '--prod']
  inputs.files('package.json', 'package-lock.json', 'angular.json', 'tsconfig.json', 'tsconfig.app.json')
  inputs.dir('src')
  inputs.dir(fileTree("node_modules").exclude(".cache"))
  outputs.dir('dist')
}
```

## Executing Yarn Tasks

When adding the node plugin, you will have a yarn task already added. This task will 
execute `yarn` and installs all dependencies in `package.json`. It will only run when changes 
are made to `package.json`, `yarn.lock`, or `node_modules`. Execute it like this:

```bash
$ gradle yarn
```

All yarn command can also be invoked using underscore notation based on a gradle rule:

```bash
$ gradle yarn_install
$ gradle yarn_upgrade
$ gradle yarn_ls
$ gradle yarn_cache_clean
...
```

These however are not shown when running gradle tasks, as they generated dynamically. However they can be 
used for dependency declarations, such as:

```gradle
yarn_install.dependsOn(yarn_cache_clean)
```

More arguments can be passed via the `build.gradle` file:

```gradle
yarn_cache_clean {
  args = ['--no-emoji', '--json']
}
```

If you want to extend the tasks more or create custom variants, you can extend the class `YarnTask`:

```gradle
task addExpress(type: YarnTask) {
  // add the express package only
  args = ['add', 'express', '--dev']
}
```

## Configuring the Plugin

You can configure the plugin through the `node` extension.

Here is the list of all available configuration properties using the Groovy DSL.
See [here](../src/test/resources/fixtures/kotlin/build.gradle.kts) to see a Kotlin DSL example.

The values shown here are the default ones. We recommend to define only the ones for which the 
default value is not satisfying.

```gradle
node {
    // Whether to download and install a specific Node.js version or not
    // If false, it will use the globally installed Node.js
    // If true, it will download node using above parameters
    // Note that npm is bundled with Node.js
    download = false
    
    //if false, it will enable Gradle Task Rules for `yarn_*`, `npm_*` and `npm_*` task names,
    //to support automatic task creation.
    //if true (the default), it will not register task rules when the plugin is applied, 
    //so all rule-generated tasks will need to be configured manually (excluding static `yarn_setup`,`node_setup`,`npm_install`,`yarn_install`)   
    disableTaskRules = true

    // Version of node to download and install (only used if download is true)
    // It will be unpacked in the workDir
    version = "12.18.3"
    
    // Version of npm to use
    // If specified, installs it in the npmWorkDir
    // If empty, the plugin will use the npm command bundled with Node.js
    npmVersion = ""
    
    // Version of Yarn to use
    // Any Yarn task first installs Yarn in the yarnWorkDir
    // It uses the specified version if defined and the latest version otherwise (by default)
    yarnVersion = ""
    
    // Base URL for fetching node distributions
    // Only used if download is true
    // Change it if you want to use a mirror
    // Or set to null if you want to add the repository on your own.
    distBaseUrl = "https://nodejs.org/dist"
    
    // The npm command executed by the npmInstall task
    // By default it is install but it can be changed to ci
    npmInstallCommand = "install"
    
    // The directory where Node.js is unpacked (when download is true) 
    workDir = file("${project.projectDir}/.gradle/nodejs")
    
    // The directory where npm is installed (when a specific version is defined)
    npmWorkDir = file("${project.projectDir}/.gradle/npm")
    
    // The directory where yarn is installed (when a Yarn task is used)
    yarnWorkDir = file("${project.projectDir}/.gradle/yarn")
    
    // The Node.js project directory location
    // This is where the package.json file and node_modules directory are located
    // By default it is at the root of the current project
    nodeProjectDir = file("${project.projectDir}")
    
    // Whether the plugin automatically should add the proxy configuration to npm and yarn commands
    // according the proxy configuration defined for Gradle
    // Disable this option if you want to configure the proxy for npm or yarn on your own
    // (in the .npmrc file for instance)
    useGradleProxySettings = true
}
```

### Using a Custom (project-local) Version of `npm`

If `npmVersion` is specified, the plugin installs that version of `npm` into `npmWorkDir`
by the `npmSetup` task and use it.

If `npmVersion` is not specified and a locally-installed `npm` exists, the plugin will
use it.

Otherwise, the plugin will use the `npm` bundled with the version of node installation.


### Using a Custom (project-local) Version of `yarn`

The plugin never uses a locally-installed `yarn` because it may be deleted during
`yarn` execution.
Instead, it installs `yarn` into `yarnWorkDir` (`.gradle/yarn/` by default) by
the `yarnSetup` task and use it.

If you would like the plugin to install use a custom version of yarn, you can set
`yarnVersion` in the `node` extension block.

## Using a Proxy

By default and unless if `useGradleProxySettings` is `false`, the plugin will configure 
`npm` and `yarn` to get them use the proxy configuration defined in the 
[Gradle project configuration](https://docs.gradle.org/current/userguide/build_environment.html#sec:accessing_the_web_via_a_proxy).
This is done by automatically setting the `HTTP_PROXY`, `HTTPS_PROXY` and `NO_PROXY` 
environment variables when invoking `npm` and `yarn`.

Note that `npm` and `yarn` support host exclusion (`NO_PROXY`) variable but
they do not support host name and port exclusion. In the case some host names and ports
are defined in the proxy exclusion, the port will be removed. The exclusion will apply to 
both HTTP and HTTPS protocols. 
