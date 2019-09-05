package com.moowork.gradle.node.npm

import org.gradle.api.Project

class NpxExecRunner
    extends NpmExecRunner
{
    public NpxExecRunner(Project project) {
        super(project)
    }

    @Override
    protected String getCommand() {
        return this.variant.npxExec
    }

    protected File getLocalCommandScript() {
        return project.file(new File(this.ext.nodeModulesDir, 'node_modules/npm/bin/npx-cli.js'))
    }

    protected String getCommandScript() {
        return this.variant.npxScriptFile
    }
}
