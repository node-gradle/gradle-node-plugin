# Installation

Installing the node-related plugins can be done in multiple ways. The easiest is to use the `plugins`-closure 
in your `build.gradle` file:

```gradle
plugins {
  id "com.github.node-gradle.node" version "2.2.3"
}
```

You can also install the plugins by using the traditional Gradle way:

```gradle
buildscript {
  repositories {
    maven {
      gradlePluginPortal()
    } 
  }

  dependencies {
    classpath "com.github.node-gradle:gradle-node-plugin:2.2.3"
  }
}

apply plugin: 'com.github.node-gradle.node'
```


## Installing snapshots

If you want to install snapshot versions of this plugin, you can add the [OJO repository](http://oss.jfrog.org)
to your build:

```gradle
buildscript {
  repositories {
    maven {
      url "http://oss.jfrog.org"
    } 
  }

  dependencies {
    classpath "com.github.node-gradle:gradle-node-plugin:2.0.0-SNAPSHOT"
  }
}

apply plugin: 'com.github.node-gradle.node'
```
