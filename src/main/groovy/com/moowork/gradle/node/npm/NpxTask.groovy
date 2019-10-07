package com.moowork.gradle.node.npm

import com.moowork.gradle.node.NodePlugin
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecResult

class NpxTask
    extends DefaultTask
{
    protected NpxExecRunner runner

    private List<?> args = []

    private ExecResult result

    private String command

    NpxTask()
    {
        this.group = NodePlugin.NODE_GROUP
        this.runner = new NpxExecRunner( this.project )
        dependsOn( NpmSetupTask.NAME )
    }

    void setArgs( final Iterable<?> value )
    {
        this.args = value.asList()
    }

    @Input
    String getCommand() {
        return command
    }

    void setCommand(String cmd )
    {
        this.command = cmd
    }

    @Input
    List<?> getArgs()
    {
        return this.args
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

    @Nested
    NpxExecRunner getRunner()
    {
        return runner
    }

    @Internal
    ExecResult getResult()
    {
        return this.result
    }

    @TaskAction
    void exec()
    {
        if ( this.command != null )
        {
            this.runner.arguments.addAll( this.command )
        }

        this.runner.arguments.addAll( this.args )
        this.result = this.runner.execute()
    }
}
