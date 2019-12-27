package com.moowork.gradle.node.exec;

import com.moowork.gradle.node.NodeExtension;
import com.moowork.gradle.node.variant.Variant;
import groovy.lang.Closure;
import org.gradle.api.Project;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.process.ExecResult;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public abstract class ExecRunner {

	protected Project project;
	protected NodeExtension ext;
	protected Variant variant;
	private Map<String, ?> environment = new LinkedHashMap<>();
	private File workingDir;
	@Internal
	private List<Object> arguments = new ArrayList<>();
	private boolean ignoreExitValue = false;
	@Internal
	private Closure execOverrides;

	public ExecRunner(final Project project) {
		this.project = project;
	}

	@Internal
	public File getWorkingDir() {
		return this.workingDir;
	}

	@Input
	public Map<String, ?> getEnvironment() {
		return this.environment;
	}

	@Input
	public boolean getIgnoreExitValue() {
		return this.ignoreExitValue;
	}

	protected final ExecResult run(final String exec, final List<?> args) {
		final String realExec = exec;
		final List<?> realArgs = args;
		final Map<Object, Object> execEnvironment = computeExecEnvironment();
		final File execWorkingDir = computeWorkingDir();
		return this.project.exec(execSpec -> {
			execSpec.setExecutable(realExec);
			execSpec.setArgs((List<String>) realArgs);
			execSpec.setEnvironment((Map<String, ?>) (Map<?, ?>) execEnvironment);
			execSpec.setIgnoreExitValue(ExecRunner.this.getIgnoreExitValue());
			execSpec.setWorkingDir(execWorkingDir);

			if (ExecRunner.this.getExecOverrides() != null) {
				ExecRunner.this.getExecOverrides().call(execSpec);
			}
		});
	}

	private File computeWorkingDir() {
		File workingDir = this.workingDir != null ? this.workingDir : this.project.getExtensions().getByType(NodeExtension.class).getNodeModulesDir();
		if (!workingDir.exists()) {
			workingDir.mkdirs();
		}

		return workingDir;
	}

	private Map<Object, Object> computeExecEnvironment() {
		Map<Object, Object> environment = new LinkedHashMap<>();
		environment.putAll(System.getenv());
		environment.putAll(this.environment);
		String path = computeAdditionalBinPath();
		if (path != null) {
			// Take care of Windows environments that may contain "Path" OR "PATH" - both existing
			// possibly (but not in parallel as of now)
			if (environment.get("Path") != null) {
				environment.put("Path", path + File.pathSeparator + environment.get("Path"));
			} else {
				environment.put("PATH", path + File.pathSeparator + environment.get("PATH"));
			}
		}

		return environment;
	}

	protected abstract String computeAdditionalBinPath();

	public final ExecResult execute() {
		this.ext = NodeExtension.get(this.project);
		this.variant = this.ext.getVariant();
		return doExecute();
	}

	protected abstract ExecResult doExecute();

	public void setEnvironment(Map<String, ?> environment) {
		this.environment = environment;
	}

	public void setWorkingDir(File workingDir) {
		this.workingDir = workingDir;
	}

	public List<Object> getArguments() {
		return this.arguments;
	}

	public void setArguments(List<Object> arguments) {
		this.arguments = arguments;
	}

	public void setIgnoreExitValue(boolean ignoreExitValue) {
		this.ignoreExitValue = ignoreExitValue;
	}

	public Closure getExecOverrides() {
		return this.execOverrides;
	}

	public void setExecOverrides(Closure execOverrides) {
		this.execOverrides = execOverrides;
	}
}
