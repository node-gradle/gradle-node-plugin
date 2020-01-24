package com.moowork.gradle.node.npm

import com.moowork.gradle.node.NodePlugin
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
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

    private Closure nodeModulesOutputFilter

    NpmInstallTask()
    {
        this.group = NodePlugin.NODE_GROUP
        this.description = 'Install node packages from package.json.'
        dependsOn( [NpmSetupTask.NAME] )

        this.project.afterEvaluate {
            def nodeExtension = this.project.extensions.getByType(NodeExtension)
            setNpmCommand( nodeExtension.getNpmInstallCommand() )

            def nodeModulesDirectory = new File(nodeExtension.nodeModulesDir, 'node_modules')
            if (nodeModulesOutputFilter) {
                def nodeModulesFileTree = project.fileTree(nodeModulesDirectory)
                nodeModulesOutputFilter(nodeModulesFileTree)
                outputs.files(nodeModulesFileTree)
            } else {
                outputs.dir(nodeModulesDirectory)
            }
        }
    }

    @Internal
    Closure getNodeModulesOutputFilter()
    {
        return nodeModulesOutputFilter
    }

    void setNodeModulesOutputFilter(Closure nodeModulesOutputFilter)
    {
        this.nodeModulesOutputFilter = nodeModulesOutputFilter
    }

    @InputFile
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
    protected getPackageLockFileAsOutput()
    {
        if (npmCommand[0] == "install") {
            def file = new File(this.project.extensions.getByType(NodeExtension).nodeModulesDir, 'package-lock.json')
            return file.exists() ? file : null
        }

        return null
    }
}
