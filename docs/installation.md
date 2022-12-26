# Installation

Installing the node-related plugins can be done in multiple ways. The easiest is to use the `plugins`-closure 
in your `build.gradle` file:

```gradle
plugins {
  id "com.github.node-gradle.node" version "3.5.1"
}
```

You can also install the plugins by using the traditional Gradle way:

```gradle
buildscript {
  repositories {
    gradlePluginPortal()
  }

  dependencies {
    classpath "com.github.node-gradle:gradle-node-plugin:3.5.1"
  }
}

apply plugin: 'com.github.node-gradle.node'
```
