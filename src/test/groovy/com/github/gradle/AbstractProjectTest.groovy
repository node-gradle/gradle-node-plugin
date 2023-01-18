package com.github.gradle

import org.gradle.api.Project
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.initialization.GradlePropertiesController
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class AbstractProjectTest extends Specification {
    @Rule
    final TemporaryFolder temporaryFolder = new TemporaryFolder()
    Project project
    File projectDir

    def setup() {
        this.projectDir = this.temporaryFolder.root
    }

    def getLayout() {
        return project.layout
    }

    def initializeProject() {
        this.project = ProjectBuilder.builder()
                .withProjectDir(this.projectDir)
                .build()
    }

    def applyPlugin() {
        project.apply plugin: 'com.github.node-gradle.node'
    }
}
