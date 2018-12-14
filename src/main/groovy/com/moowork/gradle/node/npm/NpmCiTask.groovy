package com.moowork.gradle.node.npm

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Task for making npmInstall use npm ci instead of npm install.*/
class NpmCiTask
    extends DefaultTask
{
    public final static String NAME = 'npmCi'

    NpmCiTask()
    {
        this.group = 'Node'
        this.description = 'Make npm installation use the "ci" command'
        this.outputs.upToDateWhen { false }
    }

    @TaskAction
    void configure() {
        project.tasks.withType(NpmInstallTask.class).each {
            it.npmCommand = ['ci']
            project.logger.debug("Reconfiguring: ${it.name}" )
        }
    }
}
