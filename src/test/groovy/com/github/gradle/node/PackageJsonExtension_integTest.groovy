package com.github.gradle.node

import com.github.gradle.AbstractIntegTest
import com.github.gradle.RunWithMultipleGradleVersions
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Assume

@RunWithMultipleGradleVersions
class PackageJsonExtension_integTest extends AbstractIntegTest {

    def 'check standard attribute'() {
        given:
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
    }
}