//file:noinspection GroovyPointlessBoolean
package com.github.gradle.node

import com.github.gradle.AbstractSimpleProjectTest
import org.gradle.api.internal.provider.MissingValueException

class PackageJsonExtensionTest extends AbstractSimpleProjectTest {
    def 'check standard attributes'() {
        when:
        temporaryFolder.newFile("package.json") << """
            { "name": "test", "version": "1.10.2", "private": false }
        """
        project.apply plugin: 'com.github.node-gradle.node'
        project.evaluate()

        then:
        def ext = project.extensions.getByName('package.json') as PackageJsonExtension
        ext.name.get() == "test"
        ext.version.get() == "1.10.2"
        ext.private.get() == false
    }

    def 'get missing attribute'() {
        when:
        temporaryFolder.newFile("package.json") << """
            { "name": "test", "version": "1.10.2", "private": false }
        """
        project.apply plugin: 'com.github.node-gradle.node'
        project.evaluate()
        def ext = project.extensions.getByName('package.json') as PackageJsonExtension
        ext.homepage.get() == null

        then:
        thrown(MissingValueException)
    }

    def 'get raw attributes'() {
        when:
        temporaryFolder.newFile("package.json") << """
            { "name": "test", "version": "1.10.2", "private": false }
        """
        project.apply plugin: 'com.github.node-gradle.node'
        project.evaluate()

        then:
        def ext = project.extensions.getByName('package.json') as PackageJsonExtension
        ext.get("name") == "test"
        ext.getBoolean("private") == false
    }

    def 'get nested attributes'() {
        when:
        temporaryFolder.newFile("package.json") << """
            {
            "upper": { "lower": { "end": "done" } }
            }
        """
        project.apply plugin: 'com.github.node-gradle.node'
        project.evaluate()

        then:
        def ext = project.extensions.getByName('package.json') as PackageJsonExtension
        ext.get("upper", "lower", "end") == "done"
    }
}