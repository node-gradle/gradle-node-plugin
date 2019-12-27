package com.moowork.gradle.node.exec;

import org.gradle.api.Project;
import org.gradle.process.ExecResult;


public class NodeExecRunner extends ExecRunner {

	public NodeExecRunner(final Project project) {
		super(project);
	}

	@Override
	protected ExecResult doExecute() {
		String exec = "node";
		if (this.ext.getDownload()) {
			exec = this.variant.getNodeExec();
		}

		return run(exec, this.getArguments());
	}

	@Override
	protected String computeAdditionalBinPath() {
		if (this.ext.getDownload()) {
			return this.variant.getNodeBinDir().getAbsolutePath();
		}

		return null;
	}
}
