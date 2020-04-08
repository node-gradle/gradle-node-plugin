# Usage

This plugin enables you to run any [NodeJS](https://nodejs.org) script as part of your build. It does
not require NodeJS (or NPM) being installed on your system but it is able to use them.

By default, it will use the globally installed tools (NodeJS, npm and Yarn).
If it is specified in the configuration, it is able to download and manage NodeJS distributions,
unpack them into your local `.gradle` directory and use them from there.
It can also install [NPM](https://www.npmjs.com/) packages from NPM or [Yarn](https://yarnpkg.com/).
The version of each tool to use can be specified in the configuration.

To start using the plugin, add this into your `build.gradle` 
file (see [Installing](installation.md) for details):


```gradle
plugins {
  id "com.github.node-gradle.node" version "2.2.3"
}
```

## Running a NodeJS Script

To use this plugin you have to define some tasks in your `build.gradle` file. If you have a NodeJS 
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

When executing this task for the first time, it will run a `nodeSetup` task that downloads NodeJS 
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

You can configure the plugin using the "node" extension block, like this:

```gradle
node {
  // Version of node to use.
  version = '0.11.10'

  // Version of npm to use.
  npmVersion = '2.1.5'

  // Version of Yarn to use.
  yarnVersion = '0.16.1'
  
  // Override the install command used by npmInstall
  npmInstallCommand = 'install'

  // Base URL for fetching node distributions (change if you have a mirror).
  // Or set to null if you want to add the repository on your own.
  distBaseUrl = 'https://nodejs.org/dist'

  // If true, it will download node using above parameters.
  // If false, it will try to use globally installed node.
  download = true

  // Set the work directory for unpacking node
  workDir = file("${project.buildDir}/nodejs")

  // Set the work directory for NPM
  npmWorkDir = file("${project.buildDir}/npm")

  // Set the work directory for Yarn
  yarnWorkDir = file("${project.buildDir}/yarn")

  // Set the work directory where node_modules should be located
  nodeModulesDir = file("${project.projectDir}")
}
```

**Note** that `download` flag is default to `false`. This will change in future versions.


## Using a Custom (project-local) Version of `npm`

If `npmVersion` is specified, the plugin installs that version of `npm` into `npmWorkDir`
by the `npmSetup` task and use it.

If `npmVersion` is not specified and a locally-installed `npm` exists, the plugin will
use it.

Otherwise, the plugin will use the `npm` bundled with the version of node installation.


## Using a Custom (project-local) Version of `yarn`

The plugin never uses a locally-installed `yarn` because it may be deleted during
`yarn` execution.
Instead, it installs `yarn` into `yarnWorkDir` (`.gradle/yarn/` by default) by
the `yarnSetup` task and use it.

If you would like the plugin to install use a custom version of yarn, you can set
`yarnVersion` in the `node` extension block.
