package com.github.gradle.node.npm.task

import com.github.gradle.AbstractIntegTest
import org.gradle.testkit.runner.TaskOutcome

class Npm_integTest extends AbstractIntegTest {
    def 'install packages with npm and Node.js project in sub directory'() {
        given:
        copyResources("fixtures/npm-in-subdirectory/")
        copyResources("fixtures/javascript-project/", "javascript-project")

        when:
        def result1 = build("build")

        then:
        result1.task(":npmInstall").outcome == TaskOutcome.SUCCESS
        result1.task(":buildNpx").outcome == TaskOutcome.SUCCESS
        result1.task(":buildNpm").outcome == TaskOutcome.SUCCESS
        createFile("javascript-project/package-lock.json").isFile()
        createFile("javascript-project/node_modules").isDirectory()
        !createFile("package-lock.json").exists()
        !createFile("node_modules").exists()
        createFile("javascript-project/output-npx/index.js").isFile()
        createFile("javascript-project/output-npm/index.js").isFile()

        when:
        def result2 = build("build")

        then:
        // Not up-to-date because the package-lock.json now exists
        result2.task(":npmInstall").outcome == TaskOutcome.SUCCESS
        result2.task(":buildNpx").outcome == TaskOutcome.UP_TO_DATE
        result2.task(":buildNpm").outcome == TaskOutcome.UP_TO_DATE

        when:
        def result3 = build("build")

        then:
        result3.task(":npmInstall").outcome == TaskOutcome.UP_TO_DATE
        result3.task(":buildNpx").outcome == TaskOutcome.UP_TO_DATE
        result3.task(":buildNpm").outcome == TaskOutcome.UP_TO_DATE
    }
}
