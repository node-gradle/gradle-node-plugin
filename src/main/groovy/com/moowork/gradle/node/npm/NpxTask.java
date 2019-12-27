package com.moowork.gradle.node.npm;

import com.moowork.gradle.node.NodePlugin;
import groovy.lang.Closure;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecResult;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class NpxTask extends DefaultTask {

	public NpxTask() {
		this.setGroup(NodePlugin.NODE_GROUP);
		this.runner = new NpxExecRunner(this.getProject());
		dependsOn(NpmSetupTask.NAME);
	}

	public void setArgs(final Iterable<?> value) {
		this.args = DefaultGroovyMethods.asList((Iterable<?>) value);
	}

	@Input
	public String getCommand() {
		return command;
	}

	public void setCommand(String cmd) {
		this.command = cmd;
	}

	@Input
	public List<?> getArgs() {
		return this.args;
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

	@Nested
	public NpxExecRunner getRunner() {
		return runner;
	}

	@Internal
	public ExecResult getResult() {
		return this.result;
	}

	@TaskAction
	public void exec() {
		if (this.command != null) {
			DefaultGroovyMethods.addAll(this.runner.getArguments(), new Object[]{this.command});
		}

		this.runner.getArguments().addAll(this.args);
		this.result = this.runner.execute();
	}

	protected NpxExecRunner runner;
	private List<?> args = new ArrayList<Object>();
	private ExecResult result;
	private String command;
}
