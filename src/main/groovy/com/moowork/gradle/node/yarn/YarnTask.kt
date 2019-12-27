package com.moowork.gradle.node.yarn;

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


public class YarnTask extends DefaultTask {

	protected YarnExecRunner runner;

	private List<String> args = new ArrayList<>();
	private ExecResult result;
	private String[] yarnCommand;

	public YarnTask() {
		this.setGroup(NodePlugin.NODE_GROUP);
		this.runner = new YarnExecRunner(this.getProject());
		dependsOn(YarnSetupTask.NAME);
	}

	public void setArgs(final List<String> value) {
		this.args = value;
	}

	public void setYarnCommand(String[] cmd) {
		this.yarnCommand = cmd;
	}

	@Input
	@Optional
	public String[] getYarnCommand() {
		return this.yarnCommand;
	}

	@Input
	@Optional
	public Iterable<String> getArgs() {
		return this.args;
	}

	@Nested
	public YarnExecRunner getExecRunner() {
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
		if (this.yarnCommand != null) {
			this.runner.getArguments().addAll(Arrays.asList(this.yarnCommand));
		}

		this.args.forEach(this.runner.getArguments()::add);
		this.result = this.runner.execute();
	}
}
