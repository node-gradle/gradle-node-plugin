package com.moowork.gradle.node.npm
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
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
        }
    }

    @InputFile
    @Optional
    @PathSensitive(PathSensitivity.RELATIVE)
    protected getPackageJsonFile()
    {
        def file = new File( (File) this.project.node.nodeModulesDir, 'package.json' )
        return file.exists() ? file : null
    }

    @InputFile
    @Optional
    @PathSensitive(PathSensitivity.RELATIVE)
    protected getNpmShrinkwrap()
    {
        def file = new File( (File) this.project.node.nodeModulesDir, 'npm-shrinkwrap.json' )
        return file.exists() ? file : null
    }

    @InputFile
    @Optional
    @PathSensitive(PathSensitivity.RELATIVE)
    protected getPackageLockFileAsInput()
    {
        def lockFile = new File(this.project.extensions.getByType(NodeExtension).nodeModulesDir, 'package-lock.json')
        if (npmCommand[0] == "ci") {
            return lockFile.exists() ? lockFile : null
        }

        return null
    }

    @OutputFile
    @Optional
    @PathSensitive(PathSensitivity.RELATIVE)
    protected getPackageLockFileAsOutput()
    {
        if (npmCommand[0] == "install") {
            def file = new File(this.project.extensions.getByType(NodeExtension).nodeModulesDir, 'package-lock.json')
            return file.exists() ? file : null
        }

        return null
    }


    @OutputDirectory
    protected getNodeModulesDir()
    {
        return new File(this.project.extensions.getByType(NodeExtension).nodeModulesDir, 'node_modules')
    }
}
