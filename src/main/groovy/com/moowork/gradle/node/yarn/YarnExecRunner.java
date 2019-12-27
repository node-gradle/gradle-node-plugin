package com.moowork.gradle.node.yarn;

import com.moowork.gradle.node.exec.ExecRunner;
import org.gradle.api.Project;
import org.gradle.process.ExecResult;

import java.io.File;


public class YarnExecRunner extends ExecRunner {

	public YarnExecRunner(final Project project) {
		super(project);
	}

	@Override
	protected ExecResult doExecute() {
		return run(this.variant.getYarnExec(), this.getArguments());
	}

	@Override
	protected String computeAdditionalBinPath() {
		if (this.ext.getDownload()) {
			String yarnBinDir = this.variant.getYarnBinDir().getAbsolutePath();
			String npmBinDir = this.variant.getNpmBinDir().getAbsolutePath();
			String nodeBinDir = this.variant.getNodeBinDir().getAbsolutePath();
			return yarnBinDir + File.pathSeparator + npmBinDir + File.pathSeparator + nodeBinDir;
		}

		return null;
	}
}
