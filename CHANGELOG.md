Changelog
=========

Version 3.0 *(not yet released)*
----------------------------

* Rewrite the code to Kotlin (issue [#17](https://github.com/node-gradle/gradle-node-plugin/issues/17))
(thanks [mikejhill](https://github.com/mikejhill)
for the [pull request](https://github.com/node-gradle/gradle-node-plugin/pull/57))
* Improve the Kotlin DSL support (see this [Kotlin DSL example](src/test/resources/fixtures/kotlin/build.gradle.kts)
that shows how to use this plugin version with Kotlin)
* Upgrade default Node.js version to 12.18.3 (bundled with npm 6.14.5), the latest LTS version
* Add full support of lazy configuration (issue [#39](https://github.com/node-gradle/gradle-node-plugin/issues/39))
* Fix some remaining input/output declaration issues (issue
 [#34](https://github.com/node-gradle/gradle-node-plugin/issues/34))
* Gradle 5.6.4+ support (instead of Gradle 5.0.0+ before)
* Node.js 10+ support (issue [#100](https://github.com/node-gradle/gradle-node-plugin/issues/100))

This version breaks backward compatibility. It should not visible for most Groovy DSL users since the Groovy DSL
handles transparently most of these changes.
Here is what changed:
* All the packages were renamed (they were inherited from the original forked project):
  * `com.moowork.gradle.node` (and all children) renamed to `com.github.gradle.node`
  * `com.moowork.gradle.node.npm` renamed to `com.github.gradle.node.npm.task`
  * `com.moowork.gradle.node.yarn` renamed to `com.github.gradle.node.yan.task`
* All the configuration properties (the `node` extension and all tasks) are now some 
[lazy properties](https://docs.gradle.org/current/userguide/lazy_configuration.html#lazy_properties) as recommended by 
Gradle. This makes this plugin fully compatible with lazy configuration (tasks will be configured only if they need to 
run and configuration properties are read only at runtime if needed and not at configuration time).
* Thanks to the Kotlin rewrite, some properties now have a stronger typing.
* `nodeModulesDir` option was renamed to `nodeProjectDir` (name more explicit and less confusing)
 (issue [#99](https://github.com/node-gradle/gradle-node-plugin/issues/99)). The former name still works but is 
 deprecated.
* Change the syntax to configure `nodeModulesOutputFilter` on `npmInstall` and `yarn` tasks. It also affects Groovy DSL 
users. Use now `nodeModulesOutputFilter { ... }` instead of `nodeModulesOutputFilter = { ... }`.

Version 2.2.4 *(2020-05-18)*
----------------------------

* Fix a duplicated `node_modules` output declaration in the `yarn` task. This fix speeds up this task.

Version 2.2.3 *(2020-02-28)*
----------------------------

The previous release (2.2.2) was released by error from the development branch which contains an entire Kotlin rewrite 
of the plugin code and many backward compatibility breaks. This new version replaces the previous one and adds one fix.
* Make npm and npx symlinks relative. PR #68
* NpmSetupTask does not work when using separate http and https proxy settings #69

Version 2.2.2 *(2020-02-21)*
----------------------------

* Make npm and npx symlinks relative. PR #68

Version 2.2.1 *(2020-01-31)*
----------------------------

* Only use fileTree for npmInstall output if a filter is configured. Hotfix for #63

Version 2.2.0 *(2019-10-13)*
----------------------------

* Improve the inputs declarations of tasks:
  * `NodeTask`'s `script` now has relative path sensitivity (issue [#41](https://github.com/node-gradle/gradle-node-plugin/issues/41))
  * No longer consider the working dir as an input for all tasks (issue [#40](https://github.com/node-gradle/gradle-node-plugin/issues/40))
  * Explicitly exclude the `execOverrides` option of tasks from the inputs (issue [#40](https://github.com/node-gradle/gradle-node-plugin/issues/40))
  * Add the ability to remove some files of the `node_modules` directory from the `NpmInstallTask` and `YarnInstallTask` 
  outputs from the task output ; this is necessary when some tasks change some files of the `node_modules` directory ;
  the `NpmInstallTask` and `YarnInstallTask` are never up-to-date in this case
  (issue [#38](https://github.com/node-gradle/gradle-node-plugin/issues/38))  
* Deprecate the usage of `NodeTask` with a `script` which is a directory ; Node.js supports that and looks for an
  `index.js` file in the directory but this is not compliant with a correct input/output declaration (issue [#41](https://github.com/node-gradle/gradle-node-plugin/issues/41))
* No longer use `Project.afterEvaluate` as a first step to support lazy tasks configuration (issue [#39](https://github.com/node-gradle/gradle-node-plugin/issues/39))
* Gradle 6 compatibility (all integration tests run also on Gradle 6.0-rc-2)
* Improve the integration tests coverage

Version 2.1.1 *(2019-09-28)*
----------------------------

* Fix an issue in `NodeTask`, `NpmTask`, `NpxTask` and `YarnTask` that caused the up-to-date checking to be too much sensitive regarding the environment: any change of an environment variable caused the task to be considered as out-of-date
* Improve the inputs declarations of the `YarnTask`

Version 2.1.0 *(2019-09-19)*
----------------------------
* Adds NpxTask for making use of https://www.npmjs.com/package/npx PR #32 
* Improved up-to-date checks PR #32 
* Support ARM even if the JDK reports aarch64 #33
* Setting distBaseUrl to null disables repository adding PR #25 

Version 2.0.0 *(2019-07-29)*
----------------------------
* Only support Gradle 5.x officially.
* Drop support for grunt/gulp plugins.

Version 1.5.1 *(2019-06-19)*
----------------------------
* Fix inputs/outputs for NpmInstallTask/YarnInstallTask. 

Version 1.5.0 *(2019-06-19)*
----------------------------
* Backport from srs: Added gradle build cache support for npm install (bjornmagnusson)

Version 1.4.0 *(2019-05-20)*
----------------------------
* Adds npmInstallCommand for overriding NpmInstallTasks behaviour.

Version 1.3.0 *(2018-12-04)*
----------------------------
* Relocated to com.github.node-gradle.(node|grunt|gulp)
* Conditionally use `metadataSources` on gradle >= 4.5 (#1)

Version 1.2.0 *(2017-06-14)*
----------------------------

* Support using Gradle Build Cache with Yarn tasks (#205) _(mark-vieira)_
* Bumped Gradle wrapper version to 3.5
* New args can be added for npmSetup and yarnSetup (#226)
* Uses --no-save for npmSetup and yarnSetup (#222)
* Added execOverrides and ignoreExitValue on npmSetupTask (#196)
* Added gruntFile in grunt extension to use different file (#189)
* npm_* tasks using nodeModulesDir (##136) _(janario)_

Version 1.1.1 *(2017-01-16)*
----------------------------

* Plugin publishing problems (#188)

Version 1.1.0 *(2017-01-13)*
----------------------------

* Override environment instead of setting it (#176) _(s0x)_
* Fix typo in resolveSingle (#166) _(s0x)_
* Add support for node options (#141, #174) _(whboyd)_
* Fix symlink problem using NPM (#164, #165, #181) _(s0x)_
* Set PATH variable for node, npm and yarn (#134, #146, #149, #164, #179) _(s0x)_

Version 1.0.1 *(2016-12-04)*
----------------------------

* Publish directly to plugins.gradle.org instead of bintray (#172)
* Fixed problem with resolving Grunt and Gulp plugins (#170)

Version 1.0.0 *(2016-12-02)*
----------------------------

* Move npm and yarn classes into separate package (#158)
* Move grunt plugin code to this plugin (#159)
* Move gulp plugin code to this plugin (#160)
* Use 6.9.1 as default node version (#163)
* Fix missing property exception when using plugin in conjunction with Node 6.x.x on Windows (#167) _(mark-vieira)_
* Switch over to use semantic versioning (#169)

Version 0.14 *(2016-11-29)*
---------------------------

* Bumped gradle wrapper version to 3.2.1
* Use gradle-testkit instead of nebula (#153)
* Update to use Windows zip dists when available (#142) _(datallah)_
* Added support for Yarn (#145, #151) _(kaitoy)_

Version 0.13 *(2016-06-27)*
---------------------------

* Bumped gradle wrapper version to 2.14 
* Implement ARM compatibility _(madmas)_
* Allow node modules to be used when calling npm_run _(jmcampanini)_
* Updated plugin versions and test versions
* Node.workingDir set to nodeModulesDir (fixes #107)
* Creates nodeModulesDir if it does not exist (fixes #108)
* Use single repo for node download (fixes #120)

Version 0.12 *(2016-03-10)*
---------------------------

* Updated wrapper to use Gradle 2.11
* Refactored windows-specific logic for npm _(peol)_
* Use temporary repository for resolving node dependencies
* Using 4.4.0 (latest LTS) as default node version
* Changed default workDir location to be local to project directory

Version 0.11 *(2015-09-26)*
---------------------------

* Handle 4+ nodejs releases on windows _(dvaske)_
* Add npmCommand parameter to the node extension _(janrotter)_
* Set executable flag on node in SetupTask
* Upgraded wrapper to use Gradle 2.7
* Update node distribution base url to use https _(AvihayTsayeg)_
* Added more tests (unit, integration and functional tests)
* NodeTask environment is now correctly propagated to the runner

Version 0.10 *(2015-05-19)*
---------------------------

* Fixed problem with spaces in workDir
* Add configuration for node_modules location _(nmalaguti)_
* Solaris support _(halfninja)_
* Upgraded wrapper to use Gradle 2.4

Version 0.9 *(2015-02-28)*
--------------------------

* Updated some plugin references
* Fixed some tests on Windows _(abelsromero)_
* Fixed issue for windows environments, not containing "PATH" but "Path" _(tspaeth)_
* Allow 64 bit windows to use the x64 node.exe _(ekaufman)_
* Renamed "ext" property on SetupTask so that it's not causing any conflicts
* Added detection for FreeBSD as a Linux variant
* Compiling using Java 1.6 compatiblity _(stianl)_

Version 0.8 *(2014-11-19)*
--------------------------

* Publish snapshots to jcenter _(dougborg)_
* Add node to execution path for NodeExec _(dougborg)_
* Use 'com.moowork.node' id instead of 'node
* Upgraded wrapper to use Gradle 2.2

Version 0.7 *(2014-11-03)*
--------------------------

* Allow local npm to override bundled npm _(dougborg)_
* Allow for configuring npmVersion _(dougborg)_
* Upgrade to Gradle 2.1

Version 0.6 *(2014-07-10)*
--------------------------

* Upgrade to Gradle 2.0
* Using 'com.moowork.node' as plugin id, but 'node' still works for another version
* Possible to read execResult for npm and node tasks _(johnrengelman)_

Version 0.5 *(2014-03-29)*
--------------------------

* Upgraded to use Node version 0.10.22 _(tkruse)_
* Provide gradle rule to run any NPM commands _(tkruse)_

Version 0.4 *(2014-03-07)*
--------------------------

* Possible to ignoreExitValue _(konrad-garus)_
* Added support for exec taks overrides (delegates down to Gradle exec task) _(konrad-garus)_
* Now adding npmInstall outside afterEvaluate to allow better dependsOn usage
* Reworked SetupTask so that it is using task input/output change tracking
* Updated gradle wrapper to version 1.11

Version 0.3 *(2014-01-10)*
--------------------------

* Initial usable version
