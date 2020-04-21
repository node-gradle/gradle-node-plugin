import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

buildscript {
    repositories {
        jcenter()
        mavenCentral()
        gradlePluginPortal()
    }

    dependencies {
        classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.4")
        classpath("org.jfrog.buildinfo:build-info-extractor-gradle:4.9.9")
        classpath("com.gradle.publish:plugin-publish-plugin:0.10.0")
        classpath("com.cinnober.gradle:semver-git:2.4.0")
    }
}

plugins {
    id("idea")
    id("groovy")
    id("maven-publish")
    `kotlin-dsl`
    id("com.cinnober.gradle.semver-git") version "2.4.0"
}

group = "com.github.node-gradle"

extra.properties["nextVersion"] = "minor"

apply(from = "$rootDir/gradle/additional-artifacts.gradle")
apply(from = "$rootDir/gradle/coverage.gradle.kts")
apply(from = "$rootDir/gradle/ci.gradle.kts")
apply(from = "$rootDir/gradle/publishing.gradle")

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

tasks.named<Test>("test") {
    useJUnitPlatform()
    if (project.hasProperty("skipIT")) {
        exclude("**/*_integTest*")
    }
    if (project.hasProperty("testAllSupportedGradleVersions")) {
        systemProperty("testAllSupportedGradleVersions", "true")
    }
    val processorsCount = Runtime.getRuntime().availableProcessors()
    maxParallelForks = if (processorsCount > 2) processorsCount.div(2) else 1
    testLogging {
        events = setOf(TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        exceptionFormat = TestExceptionFormat.FULL
    }
}

configure<GradlePluginDevelopmentExtension> {
    plugins {
        create("nodePlugin") {
            id = "com.github.node-gradle.node"
            implementationClass = "com.github.gradle.node.NodePlugin"
        }
    }
}
