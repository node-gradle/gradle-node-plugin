/*
 * Copyright 2014-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("UnstableApiUsage")

rootProject.name = "npm-build-cache"

pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  repositories {
    mavenCentral()

    exclusiveContent {
      forRepository {
        ivy("https://nodejs.org/dist/") {
          name = "Node Distributions at $url"
          patternLayout { artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]") }
          metadataSources { artifact() }
          content { includeModule("org.nodejs", "node") }
        }
      }
      filter { includeGroup("org.nodejs") }
    }

    exclusiveContent {
      forRepository {
        ivy("https://github.com/yarnpkg/yarn/releases/download") {
          name = "Yarn Distributions at $url"
          patternLayout { artifact("v[revision]/[artifact](-v[revision]).[ext]") }
          metadataSources { artifact() }
          content { includeModule("com.yarnpkg", "yarn") }
        }
      }
      filter { includeGroup("com.yarnpkg") }
    }
  }
}

buildCache {
  local {
    directory = file("build-cache")
  }
}
