package com.moowork.gradle.node.npm;

import com.moowork.gradle.node.NodePlugin;
import groovy.lang.Closure;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecResult;
import org.gradle.process.ExecSpec;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class NpmTask extends DefaultTask {

	protected NpmExecRunner runner;

	private List<String> args = new ArrayList<>();
	private ExecResult result;
	private String[] npmCommand;

	public NpmTask() {
		this.setGroup(NodePlugin.NODE_GROUP);
		this.runner = new NpmExecRunner(this.getProject());
		dependsOn(NpmSetupTask.NAME);
	}

	public void setArgs(final List<String> value) {
		this.args = value;
	}

	@Input
	@Optional
	public String[] getNpmCommand() {
		return this.npmCommand;
	}

	public void setNpmCommand(String[] cmd) {
		this.npmCommand = cmd;
	}

	@Input
	@Optional
	public List<String> getArgs() {
		return this.args;
	}

	@Nested
	public NpmExecRunner getExecRunner() {
		return this.runner;
	}

	public void setEnvironment(final Map<String, String> value) {
		this.runner.getEnvironment().putAll(value);
	}

	public void setWorkingDir(final File workingDir) {
		this.runner.setWorkingDir(workingDir);
	}

	public void setIgnoreExitValue(final boolean value) {
		this.runner.setIgnoreExitValue(value);
	}

	public void setExecOverrides(final Closure<ExecSpec> closure) {
		this.runner.setExecOverrides(closure);
	}

	@Internal
	public ExecResult getResult() {
		return this.result;
	}

	@TaskAction
	public void exec() {
		if (this.npmCommand != null) {
			this.runner.getArguments().addAll(Arrays.asList(this.npmCommand));
		}

		this.runner.getArguments().addAll(this.args);
		this.result = this.runner.execute();
	}
}
