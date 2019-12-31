package com.moowork.gradle.node.npm;

import com.moowork.gradle.node.exec.ExecRunner;
import org.gradle.api.Project;
import org.gradle.api.tasks.Internal;
import org.gradle.process.ExecResult;

import java.io.File;
import java.util.List;


public class NpmExecRunner extends ExecRunner {

	public NpmExecRunner(final Project project) {
		super(project);
	}

	@Override
	protected ExecResult doExecute() {

		String exec = getCommand();
		List<String> arguments = this.getArguments();

		if (this.ext.getDownload()) {
			File localNpm = getLocalCommandScript();
			if (localNpm.exists()) {
				exec = this.variant.getNodeExec();
				arguments.add(0, localNpm.getAbsolutePath());
			} else if (!new File(exec).exists()) {
				exec = this.variant.getNodeExec();
				arguments.add(0, getCommandScript());
			}
		}

		return run(exec, arguments);
	}

	@Internal
	protected String getCommand() {
		return this.variant.getNpmExec();
	}

	@Internal
	protected File getLocalCommandScript() {
		return this.project.file(new File(this.ext.getNodeModulesDir(), "node_modules/npm/bin/npm-cli.js"));
	}

	@Internal
	protected String getCommandScript() {
		return this.variant.getNpmScriptFile();
	}

	@Override
	protected String computeAdditionalBinPath() {
		if (this.ext.getDownload()) {
			String npmBinDir = this.variant.getNpmBinDir().getAbsolutePath();
			String nodeBinDir = this.variant.getNodeBinDir().getAbsolutePath();
			return npmBinDir + File.pathSeparator + nodeBinDir;
		}

		return null;
	}
}
