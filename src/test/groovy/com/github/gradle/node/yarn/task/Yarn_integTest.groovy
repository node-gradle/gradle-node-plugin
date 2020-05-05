package com.github.gradle.node.yarn.task

import com.github.gradle.AbstractIntegTest
import org.gradle.testkit.runner.TaskOutcome

class Yarn_integTest extends AbstractIntegTest {
    def 'install packages with yarn and Node.js project in sub directory'() {
        given:
        copyResources("fixtures/yarn-in-subdirectory/")
        copyResources("fixtures/javascript-project/", "javascript-project")

        when:
        def result1 = build("build")

        then:
        result1.task(":yarn").outcome == TaskOutcome.SUCCESS
        result1.task(":build").outcome == TaskOutcome.SUCCESS
        createFile("javascript-project/yarn.lock").isFile()
        createFile("javascript-project/node_modules").isDirectory()
        !createFile("yarn.lock").exists()
        !createFile("node_modules").exists()
        createFile("javascript-project/output/index.js").isFile()

        when:
        def result2 = build("build", "--info")

        then:
        // Not up-to-date because the package-lock.json now exists
        result2.task(":yarn").outcome == TaskOutcome.SUCCESS
        result2.task(":build").outcome == TaskOutcome.UP_TO_DATE

        when:
        def result3 = build("build")

        then:
        result3.task(":yarn").outcome == TaskOutcome.UP_TO_DATE
        result3.task(":build").outcome == TaskOutcome.UP_TO_DATE
    }
}
