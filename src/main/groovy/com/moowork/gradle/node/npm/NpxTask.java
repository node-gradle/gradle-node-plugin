package com.moowork.gradle.node.npm;

import com.moowork.gradle.node.NodePlugin;
import groovy.lang.Closure;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecResult;
import org.gradle.process.ExecSpec;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class NpxTask extends DefaultTask {

	protected NpxExecRunner runner;

	private List<String> args = new ArrayList<>();
	private ExecResult result;
	private String command;

	public NpxTask() {
		this.setGroup(NodePlugin.NODE_GROUP);
		this.runner = new NpxExecRunner(this.getProject());
		dependsOn(NpmSetupTask.NAME);
	}

	public void setArgs(final List<String> value) {
		this.args = value;
	}

	@Input
	public String getCommand() {
		return this.command;
	}

	public void setCommand(String cmd) {
		this.command = cmd;
	}

	@Input
	public List<String> getArgs() {
		return this.args;
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

	@Nested
	public NpxExecRunner getRunner() {
		return this.runner;
	}

	@Internal
	public ExecResult getResult() {
		return this.result;
	}

	@TaskAction
	public void exec() {
		if (this.command != null) {
			this.runner.getArguments().add(this.command);
		}

		this.runner.getArguments().addAll(this.args);
		this.result = this.runner.execute();
	}
}
