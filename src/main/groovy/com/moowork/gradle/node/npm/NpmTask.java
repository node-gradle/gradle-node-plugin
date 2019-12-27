package com.moowork.gradle.node.npm

import com.moowork.gradle.node.NodePlugin
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecResult

class NpmTask
    extends DefaultTask
{
    protected NpmExecRunner runner

    private List<?> args = []

    private ExecResult result

    private String[] npmCommand

    NpmTask()
    {
        this.group = NodePlugin.NODE_GROUP
        this.runner = new NpmExecRunner( this.project )
        dependsOn( NpmSetupTask.NAME )
    }

    void setArgs( final Iterable<?> value )
    {
        this.args = value.asList()
    }

    @Input
    @Optional
    String[] getNpmCommand() {
        return npmCommand
    }

    void setNpmCommand( String[] cmd )
    {
        this.npmCommand = cmd
    }

    @Input
    @Optional
    List<?> getArgs()
    {
        return this.args
    }

    @Nested
    NpmExecRunner getExecRunner()
    {
        return this.runner
    }

    void setEnvironment( final Map<String, ?> value )
    {
        this.runner.environment << value
    }

    void setWorkingDir( final File workingDir )
    {
        this.runner.workingDir = workingDir
    }

    void setIgnoreExitValue( final boolean value )
    {
        this.runner.ignoreExitValue = value
    }

    void setExecOverrides( final Closure closure )
    {
        this.runner.execOverrides = closure
    }

    @Internal
    ExecResult getResult()
    {
        return this.result
    }

    @TaskAction
    void exec()
    {
        if ( this.npmCommand != null )
        {
            this.runner.arguments.addAll( this.npmCommand )
        }

        this.runner.arguments.addAll( this.args )
        this.result = this.runner.execute()
    }
}
