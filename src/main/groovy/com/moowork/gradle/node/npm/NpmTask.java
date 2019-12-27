package com.moowork.gradle.node.npm;

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
import java.util.List;
import java.util.Map;


public class NpmTask extends DefaultTask {

	public NpmTask() {
		this.setGroup(NodePlugin.NODE_GROUP);
		this.runner = new NpmExecRunner(this.getProject());
		dependsOn(NpmSetupTask.NAME);
	}

	public void setArgs(final Iterable<?> value) {
		this.args = DefaultGroovyMethods.asList((Iterable<?>) value);
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
	public List<?> getArgs() {
		return this.args;
	}

	@Nested
	public NpmExecRunner getExecRunner() {
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
		if (this.npmCommand != null) {
			DefaultGroovyMethods.addAll(this.runner.getArguments(), this.npmCommand);
		}

		this.runner.getArguments().addAll(this.args);
		this.result = this.runner.execute();
	}

	protected NpmExecRunner runner;
	private List<?> args = new ArrayList<Object>();
	private ExecResult result;
	private String[] npmCommand;
}
