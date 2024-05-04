package com.github.gradle.node.npm.task

import com.github.gradle.AbstractIntegTest
import com.github.gradle.node.NodeExtension
import groovy.io.FileType
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.util.GradleVersion
import spock.lang.IgnoreIf

import static com.github.gradle.node.NodeExtension.DEFAULT_NODE_VERSION

class NpmInstall_integTest extends AbstractIntegTest {
    def 'install packages with npm (#gv.version)'() {
        given:
        gradleVersion = gv

        writeBuild('''
            plugins {
                id 'com.github.node-gradle.node'
            }
        ''')
        writeEmptyPackageJson()

        when:
        def result = build('npmInstall')

        then:
        result.task(":nodeSetup").outcome == TaskOutcome.SKIPPED
        result.task(":npmSetup").outcome == TaskOutcome.SKIPPED
        result.task(":npmInstall").outcome == TaskOutcome.SUCCESS

        when:
        result = build('npmInstall')

        then:
        result.task(":nodeSetup").outcome == TaskOutcome.SKIPPED
        result.task(":npmSetup").outcome == TaskOutcome.SKIPPED
        // because of package-lock.json that was generated during the previous npm install execution
        result.task(":npmInstall").outcome == TaskOutcome.SUCCESS

        when:
        result = build('npmInstall')

        then:
        result.task(":nodeSetup").outcome == TaskOutcome.SKIPPED
        result.task(":npmSetup").outcome == TaskOutcome.SKIPPED
        result.task(":npmInstall").outcome == TaskOutcome.UP_TO_DATE

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

    def 'install packages with fast npm install (#gv.version)'() {
        given:
        gradleVersion = gv

        writeBuild('''
            plugins {
                id 'com.github.node-gradle.node'
            }
            
            node {
                fastNpmInstall = true
            }
        ''')
        writePackageJson '''
{
  "name": "fastinstall",
  "dependencies": {
    "@isaacs/string-locale-compare": "1.1.0"
  }
}
'''

        when:
        def result = build('npmInstall')

        then:
        result.task(":npmInstall").outcome == TaskOutcome.SUCCESS

        when:
        result = build('npmInstall')

        then:
        // because of package-lock.json that was generated during the previous npm install execution
        result.task(":npmInstall").outcome == TaskOutcome.SUCCESS

        when:
        result = build('npmInstall')

        then:
        result.task(":npmInstall").outcome == TaskOutcome.UP_TO_DATE

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

    def 'fast npm install properly handles lockfile deleted through task (#gv.version)'() {
        given:
        gradleVersion = gv

        writePackageJson '''
{
  "name": "fastinstall",
  "dependencies": {
    "@isaacs/string-locale-compare": "1.1.0"
  }
}
'''

        def scriptfile = "script.js"
        writeFile(scriptfile, """
            const stringLocaleCompare = require('@isaacs/string-locale-compare')
            console.log(['b', 'a'].sort(stringLocaleCompare('en')))
""")
        writeBuild("""
            plugins {
                id 'com.github.node-gradle.node'
            }
            
            node {
                fastNpmInstall = true
            }
            
            def scriptWithDependency = file("$scriptfile")
            
            tasks.register("taskWithDependency", NodeTask) {
                script = scriptWithDependency
                dependsOn "npmInstall"
            }
            
            tasks.register("deleteNodeModules", Delete) {
                delete "node_modules"
            }
        """)


        when:
        def result = build('npmInstall')

        then:
        result.task(":npmInstall").outcome == TaskOutcome.SUCCESS

        when:
        // when the node_modules is removed
        result = build('deleteNodeModules')

        then:
        result.task(':deleteNodeModules').outcome == TaskOutcome.SUCCESS

        when:
        result = build('taskWithDependency')

        then:
        // npmInstall is re-run and the task runs successfully
        result.task(":npmInstall").outcome == TaskOutcome.SUCCESS
        result.task(":taskWithDependency").outcome == TaskOutcome.SUCCESS
        result.output.contains("[ 'a', 'b' ]")

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

    def 'fast npm install properly handles lockfile deleted manually (#gv.version)'() {
        given:
        gradleVersion = gv

        writePackageJson '''
{
  "name": "fastinstall",
  "dependencies": {
    "@isaacs/string-locale-compare": "1.1.0"
  }
}
'''

        def scriptfile = "script.js"
        writeFile(scriptfile, """
            const stringLocaleCompare = require('@isaacs/string-locale-compare')
            console.log(['b', 'a'].sort(stringLocaleCompare('en')))
""")
        writeBuild("""
            plugins {
                id 'com.github.node-gradle.node'
            }
            
            node {
                fastNpmInstall = true
            }
            
            def scriptWithDependency = file("$scriptfile")
            
            tasks.register("taskWithDependency", NodeTask) {
                script = scriptWithDependency
                dependsOn "npmInstall"
            }
        """)


        when:
        def result = build('npmInstall')

        then:
        result.task(":npmInstall").outcome == TaskOutcome.SUCCESS

        when:
        // when the node_modules is removed
        projectDir.eachFile(FileType.DIRECTORIES, {
            if (it.name == "node_modules") {
                it.deleteDir()
            }
        })
        result = build('taskWithDependency')

        then:
        // npmInstall is re-run and the task runs successfully
        result.task(":npmInstall").outcome == TaskOutcome.SUCCESS
        result.task(":taskWithDependency").outcome == TaskOutcome.SUCCESS
        result.output.contains("[ 'a', 'b' ]")

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

    def 'install packages with npm >= 7 (#gv.version)'() {
        given:
        gradleVersion = gv

        writeBuild("""
            plugins {
                id 'com.github.node-gradle.node'
            }

            node {
                download = true
                version = '$DEFAULT_NODE_VERSION'
                npmVersion = '7.0.1'
            }
        """)
        writeEmptyPackageJson()

        when:
        def result = build('npmInstall')

        then:
        result.task(":npmInstall").outcome == TaskOutcome.SUCCESS

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

    def 'install packages with npm and postinstall task requiring npm and node (#gv.version)'() {
        given:
        gradleVersion = gv

        writeBuild('''
            plugins {
                id 'com.github.node-gradle.node'
            }
        ''')
        writePackageJson(""" {
            "name": "example",
            "dependencies": {},
            "versionOutput" : "node --version",
            "postinstall" : "npm run versionOutput"
        }
        """)

        when:
        def result = build('npmInstall')

        then:
        result.task(":nodeSetup").outcome == TaskOutcome.SKIPPED
        result.task(":npmSetup").outcome == TaskOutcome.SKIPPED
        result.task(":npmInstall").outcome == TaskOutcome.SUCCESS

        when:
        result = build('npmInstall')

        then:
        result.task(":nodeSetup").outcome == TaskOutcome.SKIPPED
        result.task(":npmSetup").outcome == TaskOutcome.SKIPPED
        result.task(":npmInstall").outcome == TaskOutcome.SUCCESS

        when:
        result = build('npmInstall')

        then:
        result.task(":nodeSetup").outcome == TaskOutcome.SKIPPED
        result.task(":npmSetup").outcome == TaskOutcome.SKIPPED
        result.task(":npmInstall").outcome == TaskOutcome.UP_TO_DATE

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

    def 'configure npm install to use the ci command through extension (#gv.version)'() {
        given:
        gradleVersion = gv

        writeBuild('''
            plugins {
                id 'com.github.node-gradle.node'
            }

            node {
                npmInstallCommand = 'ci'
            }
        ''')
        writeEmptyPackageJson()

        when:
        def result = buildAndFail('npmInstall')

        then:
        result.output.contains('can only install with an existing package-lock.json')
        result.task(':npmInstall').outcome == TaskOutcome.FAILED

        when:
        writeEmptyLockFile()
        result = buildTask('npmInstall')

        then:
        result.outcome == TaskOutcome.SUCCESS

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

    def 'verify npm install inputs/outputs (#gv.version)'() {
        given:
        gradleVersion = gv

        writeBuild('''
            plugins {
                id 'com.github.node-gradle.node'
            }

            node {
                npmInstallCommand = 'install'
            }

            def lock = file('package-lock.json')
            def installTask = tasks.named("npmInstall").get()
            def outputs = installTask.outputs.files
            def inputs = installTask.inputs.files
            task verifyIO {
                doLast {
                    if (!outputs.contains(lock)) {
                        throw new RuntimeException("package-lock.json is not in INSTALL'S outputs!")
                    }
                    if (inputs.contains(lock)) {
                        throw new RuntimeException("package-lock.json is in INSTALL'S inputs!")
                    }
                }
            }
        ''')
        writeEmptyPackageJson()
        writeFile('package-lock.json', '')

        when:
        def result = buildTask('verifyIO')

        then:
        result.outcome == TaskOutcome.SUCCESS

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

    def 'verify npm ci inputs/outputs (#gv.version)'() {
        given:
        gradleVersion = gv

        writeBuild('''
            plugins {
                id 'com.github.node-gradle.node'
            }

            node {
                npmInstallCommand = 'ci'
            }

            def lock = file('package-lock.json')
            def installTask = tasks.named("npmInstall").get()
            def outputs = installTask.outputs.files
            def inputs = installTask.inputs.files
            task verifyIO {
                doLast {
                    if (outputs.contains(lock)) {
                        throw new RuntimeException("package-lock.json is in CI'S outputs!")
                    }
                    if (!inputs.contains(lock)) {
                        throw new RuntimeException("package-lock.json is not in CI'S inputs!")
                    }
                }
            }
        ''')
        writeEmptyPackageJson()
        writeEmptyLockFile()

        when:
        def result = buildTask('verifyIO')

        then:
        result.outcome == TaskOutcome.SUCCESS

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

    def 'verify output configuration (#gv.version)'() {
        given:
        gradleVersion = gv

        writeBuild('''
            plugins {
                id 'com.github.node-gradle.node'
            }
        ''')
        writePackageJson("""
            {
              "name": "hello",
              "dependencies": {
                "mocha": "6.2.0"
              }
            }
        """)

        when:
        def result1 = build("npmInstall")

        then:
        result1.task(":npmInstall").outcome == TaskOutcome.SUCCESS

        when:
        def result2 = build("npmInstall")

        then:
        // Because package-lock.json was created
        result2.task(":npmInstall").outcome == TaskOutcome.SUCCESS

        when:
        // Let's add a file in the node_modules directory
        writeFile("node_modules/mocha/newFile.txt", "hello")
        def result3 = build("npmInstall")

        then:
        // It should not make the build out-of-date
        result3.task(":npmInstall").outcome == TaskOutcome.UP_TO_DATE

        when:
        // Let's update a file in the node_modules directory
        writeFile("node_modules/mocha/package.json", "modified package.json")
        def result4 = build("npmInstall")

        then:
        // This time the build should not be up-to-date and the file should be reset
        result4.task(":npmInstall").outcome == TaskOutcome.SUCCESS
        createFile("node_modules/mocha/package.json").text != "modified package.json"

        when:
        // Let's delete a file in the node_modules directory
        createFile("node_modules/mocha/package.json").delete()
        def result5 = build("npmInstall")

        then:
        // This time the build should not be up-to-date and the file should be reset
        result5.task(":npmInstall").outcome == TaskOutcome.SUCCESS
        createFile("node_modules/mocha/package.json").exists()

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

    def 'verify output configuration when filtering node_modules output (#gv.version)'() {
        given:
        gradleVersion = gv

        writeBuild('''
            plugins {
                id 'com.github.node-gradle.node'
            }

            npmInstall {
                nodeModulesOutputFilter {
                    exclude("mocha/package.json")
                }
            }
        ''')
        writePackageJson("""
            {
              "name": "hello",
              "dependencies": {
                "mocha": "6.2.0"
              }
            }
        """)

        when:
        createFile("node_modules").deleteDir()
        def result1 = build("npmInstall")

        then:
        result1.task(":npmInstall").outcome == TaskOutcome.SUCCESS

        when:
        // Let's add a file in the node_modules directory
        writeFile("node_modules/mocha/newFile.txt", "hello")
        def result2 = build("npmInstall")

        then:
        // It should make the build out-of-date
        result2.task(":npmInstall").outcome == TaskOutcome.SUCCESS

        when:
        // Let's update a file in the node_modules directory
        writeFile("node_modules/mocha/package.json", "modified package.json")
        def result3 = build("npmInstall")

        then:
        // The build should still be up-to-date
        result3.task(":npmInstall").outcome == TaskOutcome.UP_TO_DATE

        when:
        // Let's delete an excluded file in the node_modules directory
        createFile("node_modules/mocha/package.json").delete()
        def result4 = build("npmInstall")

        then:
        // The build should still be up-to-date
        result4.task(":npmInstall").outcome == TaskOutcome.UP_TO_DATE

        when:
        // Let's delete a not excluded file in the node_modules directory
        createFile("node_modules/mocha/mocha.js").delete()
        def result5 = build("npmInstall")

        then:
        // This time the build should not be up-to-date since not the whole node_modules directory is excluded
        result5.task(":npmInstall").outcome == TaskOutcome.SUCCESS

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

    protected final void writeEmptyLockFile() {
        writeFile('package-lock.json', '''
            {
              "name": "example",
              "lockfileVersion": 1,
              "requires": true,
              "dependencies": {}
            }
        ''')
    }
}
