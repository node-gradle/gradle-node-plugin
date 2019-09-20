package com.moowork.gradle.node.yarn

import com.moowork.gradle.node.exec.ExecRunner
import com.moowork.gradle.node.exec.NodeExecRunner
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.process.ExecResult

class YarnExecRunner
    extends ExecRunner
{
    public YarnExecRunner( final Project project )
    {
        super( project )
    }

    @Override
    protected ExecResult doExecute()
    {
        return run( this.variant.yarnExec, this.arguments )
    }

    @Override
    protected String computeAdditionalBinPath()
    {
        if (this.ext.download)
        {
            def yarnBinDir = this.variant.yarnBinDir.getAbsolutePath();
            def npmBinDir = this.variant.npmBinDir.getAbsolutePath();
            def nodeBinDir = this.variant.nodeBinDir.getAbsolutePath();
            return yarnBinDir + File.pathSeparator + npmBinDir + File.pathSeparator + nodeBinDir
        }
        return null
    }
}
