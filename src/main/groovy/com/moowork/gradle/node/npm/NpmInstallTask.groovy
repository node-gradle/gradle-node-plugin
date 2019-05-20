package com.moowork.gradle.node.npm

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
            getOutputs().dir( new File( (File) this.project.node.nodeModulesDir, 'node_modules' ) )
        }
    }
}
