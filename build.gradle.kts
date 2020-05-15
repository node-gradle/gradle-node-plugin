import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

buildscript {
    repositories {
        jcenter()
        gradlePluginPortal()
    }
    extra["nextVersion"] = "major"
}

plugins {
    `java-gradle-plugin`
    groovy
    `kotlin-dsl`
    idea
    jacoco
    id("com.gradle.plugin-publish") version "0.11.0"
    id("com.cinnober.gradle.semver-git") version "3.0.0"
    id ("com.jfrog.bintray") version "1.8.5"
}

group = "com.github.node-gradle"

val compatibilityVersion = JavaVersion.VERSION_1_8

java {
    sourceCompatibility = compatibilityVersion
    targetCompatibility = compatibilityVersion
}

repositories {
    jcenter()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.6.1"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
    testImplementation("org.assertj:assertj-core:3.15.0")
    testImplementation("cglib:cglib-nodep:3.2.9")
    testImplementation("org.objenesis:objenesis:3.1")
    testImplementation("org.apache.commons:commons-io:1.3.2")
    testImplementation("org.spockframework:spock-core:1.3-groovy-2.5")
    testImplementation("com.github.stefanbirkner:system-rules:1.19.0")
}

tasks.compileTestGroovy {
    // Should be
    // classpath += files(sourceSets.test.get().kotlin.classesDirectory)
    // but enable to get it compile in the Kotlin DSL - works in the Groovy DSL as this
    // classpath += files(sourceSets.test.kotlin.classesDirectory)
    // This workaround works
    classpath += files("${buildDir}/classes/kotlin/test")
}

tasks.test {
    useJUnitPlatform()
    if (project.hasProperty("skipIT")) {
        exclude("**/*_integTest*")
    }
    if (project.hasProperty("testAllSupportedGradleVersions")) {
        systemProperty("testAllSupportedGradleVersions", "true")
    }
    val processorsCount = Runtime.getRuntime().availableProcessors()
    maxParallelForks = if (processorsCount > 2) processorsCount.div(2) else processorsCount
    testLogging {
        events = setOf(TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        exceptionFormat = TestExceptionFormat.FULL
    }
}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = true
        html.isEnabled = true
    }
}

gradlePlugin {
    plugins {
        register("nodePlugin") {
            id = "com.github.node-gradle.node"
            implementationClass = "com.github.gradle.node.NodePlugin"
        }
    }
}

val pluginName = "Gradle Node.js Plugin"
val pluginDescription = "Gradle plugin for executing Node.js scripts. Supports npm and Yarn."
val pluginTags = listOf("java", "gradle", "node", "node.js", "npm", "yarn")

pluginBundle {
    website = "https://github.com/node-gradle/gradle-node-plugin"
    vcsUrl = "https://github.com/node-gradle/gradle-node-plugin"

    (plugins) {
        "nodePlugin" {
            id = "com.github.node-gradle.node"
            displayName = pluginName
            description = pluginDescription
            tags = pluginTags
        }
    }
}

bintray {
    user = project.properties.getOrDefault("bintrayUser", "") as String
    key = project.properties.getOrDefault("bintrayKey", "") as String

    pkg(delegateClosureOf<com.jfrog.bintray.gradle.BintrayExtension.PackageConfig> {
        repo = "maven"
        name = "node-gradle"
        userOrg = "node-gradle"
        setLicenses("Apache-2.0")
        websiteUrl = "https://github.com/node-gradle/gradle-node-plugin"
        vcsUrl = "https://github.com/node-gradle/gradle-node-plugin.git"
        desc = pluginDescription
        setLabels(*pluginTags.toTypedArray())
        publicDownloadNumbers = true
        version(delegateClosureOf<com.jfrog.bintray.gradle.BintrayExtension.VersionConfig> {
            name = project.version.toString()
        })
        filesSpec(closureOf<com.jfrog.bintray.gradle.tasks.RecordingCopyTask> {
            from("build/libs")
            into(".")
        })
    })
}
