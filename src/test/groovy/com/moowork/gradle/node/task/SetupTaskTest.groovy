package com.moowork.gradle.node.task

import com.moowork.gradle.node.variant.Variant
import org.gradle.process.ExecSpec
import spock.lang.Ignore

class SetupTaskTest extends AbstractTaskTest {
    @Ignore //does not work yet
    def 'add repo if needed. no node repo in project'() {
        given:
        this.ext.download = true
        this.execSpec = Mock( ExecSpec )

        def task = this.project.tasks.create( 'setup', SetupTask )
        task.variant = Mock( Variant )

        def originalRepos = project.repositories.size()

        when:
        this.project.evaluate()
        task.exec()
        def actualRepos = project.repositories.size()

        then:
        task.result.exitValue == 0

        and:
        actualRepos == originalRepos + 1
        //0 *
    }
}
