package com.moowork.gradle.node.npm
import com.moowork.gradle.node.NodeExtension
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity

import com.moowork.gradle.node.NodeExtension

/**
 * npm install that only gets executed if gradle decides so.*/
class NpmInstallTask
    extends NpmTask
{
    public final static String NAME = 'npmInstall'

    NpmInstallTask()
    {
        this.group = 'Node'
        this.description = 'Install node packages from package.json.'
        dependsOn( [NpmSetupTask.NAME] )

        this.project.afterEvaluate {
            def ext = this.project.extensions.getByType(NodeExtension)
            setNpmCommand( ext.getNpmInstallCommand() )

            getInputs().file( new File( (File) this.project.node.nodeModulesDir, 'package.json' ) )
                    .withPathSensitivity(PathSensitivity.RELATIVE)
            getInputs().file( new File( (File) this.project.node.nodeModulesDir, 'npm-shrinkwrap.json' ) )
                    .withPathSensitivity(PathSensitivity.RELATIVE)
                    .optional()
            getOutputs().dir( new File( (File) this.project.node.nodeModulesDir, 'node_modules' ) )

            def lockFile = new File(this.project.extensions.getByType(NodeExtension).nodeModulesDir, 'package-lock.json')
            if (npmCommand[0] == "ci") {
                getInputs().file(lockFile.exists() ? lockFile : null)
                        .withPathSensitivity(PathSensitivity.RELATIVE)
                        .optional()
            } else if (npmCommand[0] == "install") {
                getOutputs().file(lockFile)
            }
        }
    }

    @OutputDirectory
    protected getNodeModulesDir()
    {
        return new File(this.project.extensions.getByType(NodeExtension).nodeModulesDir, 'node_modules')
    }
}
