package com.moowork.gradle.node.exec

import com.moowork.gradle.node.NodeExtension
import com.moowork.gradle.node.variant.Variant
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.process.ExecResult

abstract class ExecRunner
{
    protected Project project

    protected NodeExtension ext

    protected Variant variant

    def Map<String, ?> environment = [:]

    def File workingDir

    @Internal
    def List<?> arguments = []

    def boolean ignoreExitValue = false

    def Closure execOverrides

    public ExecRunner( final Project project )
    {
        this.project = project
    }

    @Internal
    File getWorkingDir()
    {
        return workingDir
    }

    @Input
    Map<String, ?> getEnvironment()
    {
        return this.environment
    }

    @Input
    boolean getIgnoreExitValue()
    {
        return ignoreExitValue
    }

    protected final ExecResult run( final String exec, final List<?> args )
    {
        def realExec = exec
        def realArgs = args
        def execEnvironment = computeExecEnvironment()
        def execWorkingDir = computeWorkingDir()
        return this.project.exec( {
            it.executable = realExec
            it.args = realArgs
            it.environment = execEnvironment
            it.ignoreExitValue = this.ignoreExitValue
            it.workingDir = execWorkingDir

            if ( this.execOverrides != null )
            {
                this.execOverrides( it )
            }
        } )
    }

    private File computeWorkingDir()
    {
        File workingDir = this.workingDir != null ? this.workingDir : this.project.node.nodeModulesDir
        if (!workingDir.exists())
        {
            workingDir.mkdirs()
        }
        return workingDir
    }

    private Map<Object, Object> computeExecEnvironment()
    {
        def environment = [:]
        environment << System.getenv()
        environment << this.environment
        String path = computeAdditionalBinPath()
        if (path != null)
        {
            // Take care of Windows environments that may contain "Path" OR "PATH" - both existing
            // possibly (but not in parallel as of now)
            if (environment['Path'] != null) {
                environment['Path'] = path + File.pathSeparator + environment['Path']
            } else {
                environment['PATH'] = path + File.pathSeparator + environment['PATH']
            }
        }
        return environment
    }

    protected abstract String computeAdditionalBinPath()

    public final ExecResult execute()
    {
        this.ext = NodeExtension.get( this.project )
        this.variant = this.ext.variant
        return doExecute()
    }

    protected abstract ExecResult doExecute()
}
