package com.github.gradle.node.bun.task

import com.github.gradle.AbstractIntegTest
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.IgnoreIf

@IgnoreIf({ os.windows })
class Bun_integTest extends AbstractIntegTest {
    def 'install packages with Bun and project in sub directory (#gv.version)'() {
        given:
        gradleVersion = gv

        copyResources("fixtures/bun-in-subdirectory/")
        copyResources("fixtures/javascript-project/", "javascript-project")

        when:
        def result1 = build("build")

        then:
        result1.task(":bunInstall").outcome == TaskOutcome.SUCCESS
        result1.task(":buildBunx").outcome == TaskOutcome.SUCCESS
        result1.task(":buildBun").outcome == TaskOutcome.SUCCESS
        createFile("javascript-project/bun.lock").isFile()
        createFile("javascript-project/node_modules").isDirectory()
        !createFile("bun.lock").exists()
        !createFile("node_modules").exists()
        createFile("javascript-project/output-bunx/index.js").isFile()
        createFile("javascript-project/output-bun/index.js").isFile()

        when:
        def result2 = build("build")

        then:
        // Not up-to-date because the bun.lock now exists
        result2.task(":bunInstall").outcome == TaskOutcome.SUCCESS
        result2.task(":buildBunx").outcome == TaskOutcome.UP_TO_DATE
        result2.task(":buildBun").outcome == TaskOutcome.UP_TO_DATE

        when:
        def result3 = build("build")

        then:
        result3.task(":bunInstall").outcome == TaskOutcome.UP_TO_DATE
        result3.task(":buildBunx").outcome == TaskOutcome.UP_TO_DATE
        result3.task(":buildBun").outcome == TaskOutcome.UP_TO_DATE

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }
}
