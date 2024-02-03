package com.github.gradle.node.npm.task

import com.github.gradle.AbstractIntegTest

import static org.gradle.testkit.runner.TaskOutcome.FROM_CACHE
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class NpmBuildCache_integTest extends AbstractIntegTest {

    def 'npmInstall can be loaded from-cache'() {
        given:
        gradleVersion = gv

        copyResources("fixtures/npm-build-cache/")

        createFile("build-cache/").deleteDir()
        createFile("dist/").deleteDir()


        when:
        def assemble1Result = build("assemble", "--stacktrace", "-PenableNpmInstallCaching=true")

        then:
        assemble1Result.task(":npmInstall").outcome == SUCCESS
        assemble1Result.task(":npmRunBuild").outcome == SUCCESS

        createFile("package-lock.json").exists()
        createFile("node_modules").exists()
        createFile("dist/app.js").isFile()


        when:
        def cleanResult = build("clean", "--stacktrace", "-PenableNpmInstallCaching=true")

        then:
        cleanResult.task(":clean").outcome == SUCCESS
        createFile("package-lock.json").exists()
        !createFile("node_modules").exists()
        !createFile("dist").exists()


        when:
        def assemble2Result = build("assemble", "--stacktrace", "-PenableNpmInstallCaching=true")

        then:
        assemble2Result.task(":npmInstall").outcome == FROM_CACHE
        assemble2Result.task(":npmRunBuild").outcome == FROM_CACHE
        createFile("node_modules").exists()
        createFile("dist").exists()
        createFile("dist/app.js").isFile()

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }


    def 'test npmInstall has cacheable outputs'() {
        // check the `org.gradle.caching.debug` logs to verify that the output is cacheable

        given:
        gradleVersion = gv

        copyResources("fixtures/npm-build-cache/")

        createFile("build-cache/").deleteDir()
        createFile("dist/").deleteDir()


        when:
        def args = ["assemble", "--stacktrace", "-Dorg.gradle.caching.debug=true", "-PenableNpmInstallCaching=true"]
        def assemble1Result = build(*args)
        def npmInstall1Output = assemble1Result.output
                .takeAfter("> Task :npmInstall")
                .takeAfter("\n")
                .takeBefore("\n\n")

        then:
        !npmInstall1Output.contains("Non-cacheable")
        !npmInstall1Output.contains("[OVERLAPPING_OUTPUTS]")
        !npmInstall1Output.contains("Gradle does not know how file 'node_modules/.package-lock.json' was created")


        when:
        def assemble2Result = build(*args)
        def npmInstall2Output = assemble2Result.output
                .takeAfter("> Task :npmInstall")
                .takeAfter("\n")
                .takeBefore("\n\n")

        then:
        !npmInstall2Output.contains("Non-cacheable")
        !npmInstall2Output.contains("[OVERLAPPING_OUTPUTS]")
        !npmInstall2Output.contains("Gradle does not know how file 'node_modules/.package-lock.json' was created")

        // the inputs and outputs shouldn't have changed, so the fingerprinted properties should be the same
        npmInstall1Output == npmInstall2Output

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }
}
