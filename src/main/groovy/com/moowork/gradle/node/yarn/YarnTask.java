package com.moowork.gradle.node.yarn;

import com.moowork.gradle.node.NodePlugin;
import groovy.lang.Closure;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecResult;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;


public class YarnTask extends DefaultTask {

	public YarnTask() {
		this.setGroup(NodePlugin.NODE_GROUP);
		this.runner = new YarnExecRunner(this.getProject());
		dependsOn(YarnSetupTask.NAME);
	}

	public void setArgs(final Iterable<?> value) {
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
	public Iterable<?> getArgs() {
		return this.args;
	}

	@Nested
	public YarnExecRunner getExecRunner() {
		return this.runner;
	}

	public void setEnvironment(final Map<String, ?> value) {
		DefaultGroovyMethods.leftShift((Map<String, Object>) this.runner.getEnvironment(), (Map<String, Object>) value);
	}

	public void setWorkingDir(final File workingDir) {
		this.runner.setWorkingDir(workingDir);
	}

	public void setIgnoreExitValue(final boolean value) {
		this.runner.setIgnoreExitValue(value);
	}

	public void setExecOverrides(final Closure closure) {
		this.runner.setExecOverrides(closure);
	}

	@Internal
	public ExecResult getResult() {
		return this.result;
	}

	@TaskAction
	public void exec() {
		if (this.yarnCommand != null) {
			DefaultGroovyMethods.addAll(this.runner.getArguments(), this.yarnCommand);
		}

		DefaultGroovyMethods.addAll(this.runner.getArguments(), this.args);
		this.result = this.runner.execute();
	}

	protected YarnExecRunner runner;
	private Iterable<?> args = new ArrayList<>();
	private ExecResult result;
	private String[] yarnCommand;
}
