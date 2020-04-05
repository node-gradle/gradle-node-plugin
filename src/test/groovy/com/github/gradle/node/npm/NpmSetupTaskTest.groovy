package com.github.gradle.node.npm


import com.github.gradle.node.task.AbstractTaskTest
import org.gradle.process.ExecSpec

class NpmSetupTaskTest
    extends AbstractTaskTest
{
    def "exec npmSetup task"()
    {
        given:
        this.props.setProperty( 'os.name', 'Linux' )
        this.execSpec = Mock( ExecSpec )

        def task = this.project.tasks.create( 'simple', NpmSetupTask )

        when:
        this.project.evaluate()
        task.exec()

        then:
        task.result.exitValue == 0
        1 * this.execSpec.setArgs( [] )
    }

    def "exec npmSetup task (version specified)"()
    {
        given:
        this.props.setProperty( 'os.name', 'Linux' )
        this.ext.npmVersion = '6.4.1'
        this.execSpec = Mock( ExecSpec )

        this.execSpec = Mock( ExecSpec )
        def task = this.project.tasks.create( 'simple', NpmSetupTask )

        when:
        this.project.evaluate()
        task.exec()

        then:
        task.result.exitValue == 0
        1 * this.execSpec.setArgs( [] )
    }
}
