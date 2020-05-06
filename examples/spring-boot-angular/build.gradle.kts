import org.springframework.boot.gradle.plugin.SpringBootPlugin

// This is necessary to get Gradle use the plugin from its source instead of fetching it from the repository
// Don't use that in your project
buildscript {
    dependencies {
        classpath("com.github.node-gradle:gradle-node-plugin:3.0.0")
    }
}

plugins {
    java
    id("org.springframework.boot") version "2.2.6.RELEASE"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(SpringBootPlugin.BOM_COORDINATES))
    implementation(project(":webapp"))
    implementation("org.springframework.boot:spring-boot-starter-web")
}
