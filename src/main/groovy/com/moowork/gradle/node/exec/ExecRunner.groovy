package com.moowork.gradle.node.exec

import com.moowork.gradle.node.NodeExtension
import com.moowork.gradle.node.util.ProjectApiHelper
import com.moowork.gradle.node.variant.Variant
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec

abstract class ExecRunner
{
    protected Project project

    protected ProjectApiHelper projectApi;

    protected NodeExtension ext

    protected Variant variant

    def Map<String, ?> environment = [:]

    def File workingDir

    @Internal
    def List<?> arguments = []

    def boolean ignoreExitValue = false

    @Internal
    def Closure execOverrides

    public ExecRunner( final Project project )
    {
        this.project = project
        this.ext = NodeExtension.get( project )
        this.projectApi = ProjectApiHelper.newInstance(project)
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
        return this.project.exec({
            ExecSpec it ->
            it.executable = realExec
            it.args = realArgs.collect {it.toString()}
            it.environment = execEnvironment
            it.ignoreExitValue = this.ignoreExitValue
            it.workingDir = execWorkingDir

            if ( this.execOverrides != null )
            {
                this.execOverrides( it )
            }
        })
    }

    private File computeWorkingDir()
    {
        File workingDir = this.workingDir != null ? this.workingDir : this.ext.nodeModulesDir
        if (!workingDir.exists())
        {
            workingDir.mkdirs()
        }
        return workingDir
    }

    private Map<String, Object> computeExecEnvironment()
    {
        Map<String, Object> environment = [:]
        environment << System.getenv()
        this.environment.each {environment[it.getKey().toString()] = it.getValue()}
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
        this.variant = this.ext.variant
        return doExecute()
    }

    protected abstract ExecResult doExecute()
}
