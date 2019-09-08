package com.moowork.gradle.node.npm

import com.moowork.gradle.node.exec.ExecRunner
import org.gradle.api.Project
import org.gradle.api.tasks.Internal
import org.gradle.process.ExecResult

class NpmExecRunner
    extends ExecRunner
{
    public NpmExecRunner( final Project project )
    {
        super( project )
    }

    @Override
    protected ExecResult doExecute()
    {

        def exec = getCommand()
        def arguments = this.arguments

        if ( this.ext.download )
        {
            def npmBinDir = this.variant.npmBinDir.getAbsolutePath();

            def nodeBinDir = this.variant.nodeBinDir.getAbsolutePath();

            def path = npmBinDir + File.pathSeparator + nodeBinDir;

            // Take care of Windows environments that may contain "Path" OR "PATH" - both existing
            // possibly (but not in parallel as of now)
            if ( environment['Path'] != null )
            {
                environment['Path'] = path + File.pathSeparator + environment['Path']
            }
            else
            {
                environment['PATH'] = path + File.pathSeparator + environment['PATH']
            }

            def File localNpm = getLocalCommandScript()
            if ( localNpm.exists() )
            {
                exec = this.variant.nodeExec
                arguments = [localNpm.absolutePath] + arguments
            }
            else if ( !new File(exec).exists() )
            {
                exec = this.variant.nodeExec
                arguments = [getCommandScript()] + arguments
            }
        }
        return run( exec, arguments )
    }

    @Internal
    protected String getCommand() {
        this.variant.npmExec
    }

    @Internal
    protected File getLocalCommandScript() {
        return project.file(new File(this.ext.nodeModulesDir, 'node_modules/npm/bin/npm-cli.js'))
    }

    @Internal
    protected String getCommandScript() {
        return this.variant.npmScriptFile
    }
}
