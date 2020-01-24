package com.moowork.gradle.node.yarn

import com.moowork.gradle.node.NodeExtension
import com.moowork.gradle.node.NodePlugin
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity

/**
 * yarn install that only gets executed if gradle decides so.*/
class YarnInstallTask
    extends YarnTask
{
    public final static String NAME = 'yarn'

    private Closure nodeModulesOutputFilter

    YarnInstallTask()
    {
        this.group = NodePlugin.NODE_GROUP
        this.description = 'Install node packages using Yarn.'
        setYarnCommand( '' )
        dependsOn( [YarnSetupTask.NAME] )

        this.project.afterEvaluate {
            def nodeExtension = this.project.extensions.getByType(NodeExtension)
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
    @Optional
    @PathSensitive(PathSensitivity.RELATIVE)
    protected getPackageJsonFile()
    {
        def packageJsonFile = new File(this.project.extensions.getByType(NodeExtension).nodeModulesDir, 'package.json')
        return packageJsonFile.exists() ? packageJsonFile : null
    }

    @InputFile
    @Optional
    @PathSensitive(PathSensitivity.RELATIVE)
    protected getYarnLockFile()
    {
        def lockFile = new File(this.project.extensions.getByType(NodeExtension).nodeModulesDir, 'yarn.lock')
        return lockFile.exists() ? lockFile : null
    }

    @OutputFiles
    protected getNodeModulesDir()
    {
        def nodeModulesDirectory =
                new File(this.project.extensions.getByType(NodeExtension).nodeModulesDir, 'node_modules')
        def nodeModulesFileTree = project.fileTree(nodeModulesDirectory)
        if (nodeModulesOutputFilter)
        {
            nodeModulesOutputFilter(nodeModulesFileTree)
        }
        return nodeModulesFileTree
    }
}
