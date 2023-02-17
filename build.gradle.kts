import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
    id("com.gradle.plugin-publish") version "1.1.0"
    id("com.cinnober.gradle.semver-git") version "3.0.0"
    id("org.jetbrains.dokka") version "1.7.10"
    id("org.gradle.test-retry") version "1.5.0"
}

group = "com.github.node-gradle"

val compatibilityVersion = JavaVersion.VERSION_1_8

java {
    sourceCompatibility = compatibilityVersion
    targetCompatibility = compatibilityVersion
}

tasks.compileKotlin {
    kotlinOptions {
        apiVersion = "1.3"
        freeCompilerArgs = listOf("-Xno-optimized-callable-references")
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = compatibilityVersion.toString()
    }
}

repositories {
    mavenCentral()
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
    testImplementation(platform("org.spockframework:spock-bom:2.0-groovy-3.0"))
    testImplementation("org.spockframework:spock-core")
    testImplementation("org.spockframework:spock-junit4")
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
    systemProperty(
        com.github.gradle.buildlogic.GradleVersionsCommandLineArgumentProvider.PROPERTY_NAME,
        project.findProperty("testedGradleVersion") ?: gradle.gradleVersion
    )

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
        remoteExecutionPreferred.set(project.properties["com.github.gradle.node.preferremote"].toString().toBoolean())
        if (project.properties["com.github.gradle.node.remoteonly"].toString().toBoolean()) {
            maxLocalExecutors.set(0)
        }
    }

    predictiveSelection {
        enabled.set(project.properties["com.github.gradle.node.predictivetestselection"].toString().toBoolean())
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

tasks.register<Test>("testGradleReleases") {
    jvmArgumentProviders.add(
        com.github.gradle.buildlogic.GradleVersionsCommandLineArgumentProvider(
            com.github.gradle.buildlogic.GradleVersionData::getReleasedVersions
        )
    )
}

tasks.register("printVersions") {
    doLast {
        println(com.github.gradle.buildlogic.GradleVersionData::getReleasedVersions.invoke())
    }
}

tasks.register<Test>("testGradleNightlies") {
    jvmArgumentProviders.add(
        com.github.gradle.buildlogic.GradleVersionsCommandLineArgumentProvider(
            com.github.gradle.buildlogic.GradleVersionData::getNightlyVersions
        )
    )
}

tasks.register("runParameterTest", JavaExec::class.java) {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.github.gradle.node.util.PlatformHelperKt")
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
    dokkaSourceSets {
        named("main") {
            jdkVersion.set(8)
        }
    }
}

gradlePlugin {
    plugins {
        register("nodePlugin") {
            id = "com.github.node-gradle.node"
            implementationClass = "com.github.gradle.node.NodePlugin"
            displayName = "Gradle Node.js Plugin"
            description = "Gradle plugin for executing Node.js scripts. Supports npm, pnpm and Yarn."

            tags.set(listOf("java", "node", "node.js", "npm", "yarn", "pnpm"))
        }
    }

    website.set("https://github.com/node-gradle/gradle-node-plugin")
    vcsUrl.set("https://github.com/node-gradle/gradle-node-plugin")

}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}
