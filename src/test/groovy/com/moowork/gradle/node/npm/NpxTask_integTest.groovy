package com.moowork.gradle.node.npm

import com.moowork.gradle.AbstractIntegTest
import org.gradle.testkit.runner.TaskOutcome

class NpxTask_integTest
    extends AbstractIntegTest
{
    def 'execute npx command with no package.json file'()
    {
        given:
        writeBuild( '''
            plugins {
                id 'com.github.node-gradle.node'
            }

            node {
                version = "10.14.0"
                npmVersion = "6.4.1"
                download = true
                workDir = file('build/node')
            }
            
            task camelCase(type: NpxTask) {
                command = 'chcase-cli'
                args = ['--help']
            }
        ''' )

        when:
        def result = build(":camelCase")

        then:
        result.task(":camelCase").outcome == TaskOutcome.SUCCESS
        result.output.contains("--case, -C  Which case to convert to")
    }

    def 'execute npx command with a package.json file'()
    {
        given:
        // mocha is installed locally whereas eslint not
        writeBuild( '''
            plugins {
                id 'com.github.node-gradle.node'
            }

            node {
                version = "10.14.0"
                npmVersion = "6.4.1"
                download = true
                workDir = file('build/node')
            }
            
            task lint(type: NpxTask) {
                dependsOn npmInstall
                command = 'eslint@6.3.0'
                args = ['test.js']
                inputs.files('.eslintrc.yml', 'test.js')
                outputs.upToDateWhen {
                    true
                }
            }
            
            task test(type: NpxTask) {
                dependsOn lint
                command = 'mocha'
                inputs.dir('node_modules')
                inputs.file('package.json')
                inputs.file('test.js')
                outputs.upToDateWhen {
                    true
                }
            }
        ''' )

        writePackageJson(""" {
            "name": "example",
            "dependencies": {
                "mocha": "~6.2.0",
                "chai": "~4.2.0"
            }
        }""")

        writeFile("test.js", """
        const chai = require('chai');
        const expect = chai.expect;
        
        describe('Array', () => {
            describe('#indexOf()', () => {
                it('should return -1 when the value is not present', () => {
                    expect([1, 2, 3].indexOf(4)).to.equal(-1);
                });
            });
        });
        """)

        writeFile(".eslintrc.yml", """
        env:
          browser: true
          commonjs: true
          es6: true
        extends: 'eslint:recommended'
        globals:
          Atomics: readonly
          SharedArrayBuffer: readonly
        parserOptions:
          ecmaVersion: 2018
        rules:
          no-undef: warn
        """)

        when:
        def result = build(":test")

        then:
        result.task(":lint").outcome == TaskOutcome.SUCCESS
        result.task(":test").outcome == TaskOutcome.SUCCESS
        result.output.contains("3 problems (0 errors, 3 warnings)")
        result.output.contains("1 passing")

        when:
        def result2 = build(":test")

        then:
        result2.task(":lint").outcome == TaskOutcome.UP_TO_DATE
        result2.task(":test").outcome == TaskOutcome.UP_TO_DATE
    }
}
