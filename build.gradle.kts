import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

buildscript {
    repositories {
        mavenCentral()
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
    id("com.gradle.plugin-publish") version "1.0.0-rc-3"
    id("com.cinnober.gradle.semver-git") version "3.0.0"
    id("org.jetbrains.dokka") version "0.10.0"
    id("org.gradle.test-retry") version "1.2.0"
}

group = "com.github.node-gradle"

val compatibilityVersion = JavaVersion.VERSION_1_8

java {
    sourceCompatibility = compatibilityVersion
    targetCompatibility = compatibilityVersion
}

repositories {
    mavenCentral()
    // Necessary for dokka (will have to be removed when dokka no longer
    // depends on artifacts only present in jcenter)
    jcenter()
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-databind:2.13.2.2")
    testImplementation(platform("org.junit:junit-bom:5.6.2"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
    testImplementation("org.assertj:assertj-core:3.17.2")
    testImplementation("cglib:cglib-nodep:3.3.0")
    testImplementation("org.objenesis:objenesis:3.1")
    testImplementation("org.apache.commons:commons-io:1.3.2")
    testImplementation("org.spockframework:spock-core:1.3-groovy-2.5")
    testImplementation("com.github.stefanbirkner:system-rules:1.19.0")
    testImplementation("org.mock-server:mockserver-netty:5.11.1")
}

tasks.compileTestGroovy {
    // Should be
    // classpath += files(sourceSets.test.get().kotlin.classesDirectory)
    // but unable to get it compile in the Kotlin DSL - works in the Groovy DSL as this
    // classpath += files(sourceSets.test.kotlin.classesDirectory)
    // This workaround works
    classpath += files("${buildDir}/classes/kotlin/test")
}

tasks.withType(Test::class) {
    useJUnitPlatform()
    systemProperty("testAllSupportedGradleVersions", project.properties["testAllSupportedGradleVersions"] ?: "false")
    systemProperty(
        "testMinimumSupportedGradleVersion", project.properties["testMinimumSupportedGradleVersion"]
            ?: "false"
    )
    systemProperty("testMinimumCurrentGradleVersion", project.properties["testMinimumCurrentGradleVersion"] ?: "false")
    systemProperty("testCurrentGradleVersion", project.properties["testCurrentGradleVersion"] ?: "true")
    systemProperty("testSpecificGradleVersion", project.properties["testSpecificGradleVersion"] ?: "false")

    val processorsCount = Runtime.getRuntime().availableProcessors()
    maxParallelForks = if (processorsCount > 2) processorsCount.div(2) else processorsCount
    testLogging {
        events = setOf(TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        exceptionFormat = TestExceptionFormat.FULL
    }

    retry {
        maxRetries.set(3)
        filter {
            includeClasses.add("*_integTest")
        }
    }

    distribution {
        enabled.set(project.properties["com.github.gradle.node.testdistribution"].toString().toBoolean())
    }
}

tasks.test {
    exclude("**/Pnpm*Test*")
    if (project.hasProperty("skipIT")) {
        exclude("**/*_integTest*")
    } else if (project.hasProperty("onlyIT")) {
        include("**/*_integTest*")
    }
}

tasks.register<Test>("pnpmTests") {
    include("**/Pnpm*Test*")
}

tasks.register("runParameterTest", JavaExec::class.java) {
    classpath = sourceSets["main"].runtimeClasspath
    main = "com.github.gradle.node.util.PlatformHelperKt"
}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = true
        html.isEnabled = true
    }
}

tasks.dokka {
    outputFormat = "javadoc"
    configuration { jdkVersion = 8 }
}

gradlePlugin {
    plugins {
        register("nodePlugin") {
            id = "com.github.node-gradle.node"
            implementationClass = "com.github.gradle.node.NodePlugin"
            displayName = "Gradle Node.js Plugin"
            description = "Gradle plugin for executing Node.js scripts. Supports npm, pnpm and Yarn."
        }
    }
}

pluginBundle {
    website = "https://github.com/node-gradle/gradle-node-plugin"
    vcsUrl = "https://github.com/node-gradle/gradle-node-plugin"

    tags = listOf("java", "node", "node.js", "npm", "yarn", "pnpm")
}

tasks.wrapper {
    gradleVersion = "6.8.3"
    distributionType = Wrapper.DistributionType.ALL
}
