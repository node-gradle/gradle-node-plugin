package com.github.gradle.node

import com.github.gradle.AbstractIntegTest
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Assume

class PackageJsonExtension_integTest extends AbstractIntegTest {
    def 'check standard attribute (#gv.version)'() {
        given:
        gradleVersion = gv
        Assume.assumeTrue(isConfigurationCacheEnabled())
        copyResources("fixtures/npm-env/")
        copyResources("fixtures/env/")
        createFile("build.gradle") << """
def nameProvider = project.extensions.getByName("${PackageJsonExtension.NAME}").name
tasks.register('printPackageJsonName') {
    inputs.property('name', nameProvider)
    doLast {
        println nameProvider.get()
    }
}
"""

        when:
        def result1 = build(":printPackageJsonName")
        def result2 = build(":printPackageJsonName")

        then:
        result1.task(":printPackageJsonName").outcome == TaskOutcome.SUCCESS
        result2.task(":printPackageJsonName").outcome == TaskOutcome.SUCCESS

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }
}