<a href="#support"><img align="right" src="docs/images/support.png?raw=true"></a>

# Gradle Plugin for Node

![Build Status](https://github.com/node-gradle/gradle-node-plugin/workflows/Build/badge.svg?branch=master)
[![License](https://img.shields.io/github/license/node-gradle/gradle-node-plugin.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
![Version](https://img.shields.io/badge/Version-3.5.1-orange.svg)

This plugin enables you to use a lot of [Node.js](https://nodejs.org)-based technologies as part of your 
build without having Node.js installed locally on your system. It integrates the following Node.js-based system
with Gradle:

* [Node.js](https://nodejs.org)
* [npm](https://www.npmjs.com/)
* [Yarn](https://yarnpkg.com/)
* [pnpm](https://pnpm.io/)

The plugin is published in the [Gradle plugins portal](https://plugins.gradle.org/plugin/com.github.node-gradle.node)
with the `com.github.node-gradle.node` identifier.

It supports Gradle 5.6.4+ and Node.js 10+.

## Documentation

⚠️ This is the documentation of the development version. See below in the releases history to read the 
documentation of the version you're using.

Here's how you get started using this plugin. If you do not find what you are looking for, please add an 
issue to [GitHub Issues](https://github.com/node-gradle/gradle-node-plugin/issues).

* [Installation](docs/installation.md)
* [Usage](docs/usage.md)
* [FAQ](docs/faq.md)
* [Changelog](CHANGELOG.md)
* [Example Projects](examples)


## Releases History

Here's the documentation for older releases of the plugin:

* [3.5.1](https://github.com/node-gradle/gradle-node-plugin/blob/3.5.1/README.md) (current)
* [3.5.0](https://github.com/node-gradle/gradle-node-plugin/blob/3.5.0/README.md)
* [3.4.0](https://github.com/node-gradle/gradle-node-plugin/blob/3.4.0/README.md)
* [3.3.0](https://github.com/node-gradle/gradle-node-plugin/blob/3.3.0/README.md)
* [3.2.1](https://github.com/node-gradle/gradle-node-plugin/blob/3.2.1/README.md)
* [3.2.0](https://github.com/node-gradle/gradle-node-plugin/blob/3.2.0/README.md)
* [3.1.1](https://github.com/node-gradle/gradle-node-plugin/blob/3.1.1/README.md)
* [3.1.0](https://github.com/node-gradle/gradle-node-plugin/blob/3.1.0/README.md)
* [3.0.1](https://github.com/node-gradle/gradle-node-plugin/blob/3.0.1/README.md)
* [3.0.0](https://github.com/node-gradle/gradle-node-plugin/blob/3.0.0/README.md)
* [2.2.4](https://github.com/node-gradle/gradle-node-plugin/blob/2.2.4/README.md)
* [2.2.3](https://github.com/node-gradle/gradle-node-plugin/blob/2.2.3/README.md)
* [2.2.2](https://github.com/node-gradle/gradle-node-plugin/blob/2.2.2/README.md)
* [2.2.1](https://github.com/node-gradle/gradle-node-plugin/blob/2.2.1/README.md)
* [2.2.0](https://github.com/node-gradle/gradle-node-plugin/blob/2.2.0/README.md)
* [2.1.1](https://github.com/node-gradle/gradle-node-plugin/blob/2.1.1/README.md)
* [2.1.0](https://github.com/node-gradle/gradle-node-plugin/blob/2.1.0/README.md)
* [2.0.0](https://github.com/node-gradle/gradle-node-plugin/blob/2.0.0/README.md)
* [1.5.3](https://github.com/node-gradle/gradle-node-plugin/blob/1.5.3/README.md)
* [1.4.0](https://github.com/node-gradle/gradle-node-plugin/blob/1.4.0/README.md)


## Building the Plugin

### Prerequisites

Some integration test ensure that this plugin is able to use the globally installed Node.js, npm or yarn tools.
This requires those tools are globally installed on your system in order to get all integration tests pass.

### Command

To build the plugin, just type the following command:

```bash
./gradlew build
```

The integration tests are run using multiple Gradle versions to ensure it works on all supported versions.
But this takes a lot of time. To speed up the build, the tests run only on the current Gradle versions.
To run the tests against all Gradle versions, use the following option (it is done by the CI).

```bash
./gradlew build -PtestAllSupportedGradleVersions=true
```

## Contributing

Contributions are always welcome! If you'd like to contribute (and we hope you do) please send 
one of the existing contributors a nudge.

## <a name="support"></a> Support this project :heart:

This plugin is open source project and completely free to use. If you are using this project in your products/projects, please consider sponsoring to ensure it is actively developed and maintained.

[Donate via PayPal (one time)](https://www.paypal.me/ANordlund)

## License

```
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
