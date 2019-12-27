package com.moowork.gradle.node.exec;

import com.moowork.gradle.node.NodeExtension;
import com.moowork.gradle.node.variant.Variant;
import groovy.lang.Closure;
import org.gradle.api.Project;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.process.ExecResult;
import org.gradle.process.ExecSpec;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public abstract class ExecRunner {

	protected Project project;
	protected NodeExtension ext;
	protected Variant variant;

	private Map<String, String> environment = new LinkedHashMap<>();
	private File workingDir;
	@Internal
	private List<String> arguments = new ArrayList<>();
	private boolean ignoreExitValue = false;
	@Internal
	private Closure<ExecSpec> execOverrides;

	public ExecRunner(final Project project) {
		this.project = project;
	}

	@Internal
	public File getWorkingDir() {
		return this.workingDir;
	}

	@Input
	public Map<String, String> getEnvironment() {
		return this.environment;
	}

	@Input
	public boolean getIgnoreExitValue() {
		return this.ignoreExitValue;
	}

	protected final ExecResult run(final String exec, final List<String> args) {
		final Map<String, String> execEnvironment = computeExecEnvironment();
		final File execWorkingDir = computeWorkingDir();
		return this.project.exec(execSpec -> {
			execSpec.setExecutable(exec);
			execSpec.setArgs(args);
			execSpec.setEnvironment(execEnvironment);
			execSpec.setIgnoreExitValue(getIgnoreExitValue());
			execSpec.setWorkingDir(execWorkingDir);

			if (getExecOverrides() != null) {
				getExecOverrides().call(execSpec);
			}
		});
	}

	private File computeWorkingDir() {
		File computedWorkingDir = this.workingDir != null ? this.workingDir : this.project.getExtensions().getByType(NodeExtension.class).getNodeModulesDir();
		if (!computedWorkingDir.exists()) {
			computedWorkingDir.mkdirs();
		}

		return computedWorkingDir;
	}

	private Map<String, String> computeExecEnvironment() {
		Map<String, String> execEnvironment = new LinkedHashMap<>();
		execEnvironment.putAll(System.getenv());
		execEnvironment.putAll(this.environment);
		String path = computeAdditionalBinPath();
		if (path != null) {
			// Take care of Windows environments that may contain "Path" OR "PATH" - both existing
			// possibly (but not in parallel as of now)
			if (execEnvironment.get("Path") != null) {
				execEnvironment.put("Path", path + File.pathSeparator + execEnvironment.get("Path"));
			} else {
				execEnvironment.put("PATH", path + File.pathSeparator + execEnvironment.get("PATH"));
			}
		}

		return execEnvironment;
	}

	protected abstract String computeAdditionalBinPath();

	public final ExecResult execute() {
		this.ext = NodeExtension.get(this.project);
		this.variant = this.ext.getVariant();
		return doExecute();
	}

	protected abstract ExecResult doExecute();

	public void setEnvironment(Map<String, String> environment) {
		this.environment = environment;
	}

	public void setWorkingDir(File workingDir) {
		this.workingDir = workingDir;
	}

	public List<String> getArguments() {
		return this.arguments;
	}

	public void setArguments(List<String> arguments) {
		this.arguments = arguments;
	}

	public void setIgnoreExitValue(boolean ignoreExitValue) {
		this.ignoreExitValue = ignoreExitValue;
	}

	public Closure<ExecSpec> getExecOverrides() {
		return this.execOverrides;
	}

	public void setExecOverrides(Closure<ExecSpec> execOverrides) {
		this.execOverrides = execOverrides;
	}
}
