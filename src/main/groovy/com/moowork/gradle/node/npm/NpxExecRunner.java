package com.moowork.gradle.node.npm;

import org.gradle.api.Project;
import org.gradle.api.tasks.Internal;

import java.io.File;


public class NpxExecRunner extends NpmExecRunner {

	public NpxExecRunner(Project project) {
		super(project);
	}

	@Override
	@Internal
	protected String getCommand() {
		return this.variant.getNpxExec();
	}

	@Override
	@Internal
	protected File getLocalCommandScript() {
		return project.file(new File(this.ext.getNodeModulesDir(), "node_modules/npm/bin/npx-cli.js"));
	}

	@Override
	@Internal
	protected String getCommandScript() {
		return this.variant.getNpxScriptFile();
	}
}
