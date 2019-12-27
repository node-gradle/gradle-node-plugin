package com.moowork.gradle.node.task;

import com.moowork.gradle.node.NodePlugin;
import com.moowork.gradle.node.exec.NodeExecRunner;
import groovy.lang.Closure;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecResult;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.gradle.api.tasks.PathSensitivity.RELATIVE;


public class NodeTask extends DefaultTask {

	public NodeTask() {
		this.setGroup(NodePlugin.NODE_GROUP);
		this.runner = new NodeExecRunner(this.getProject());
		dependsOn(SetupTask.NAME);
	}

	public void setScript(final File value) {
		this.script = value;
	}

	public void setArgs(final Iterable<?> value) {
		this.args = DefaultGroovyMethods.asList((Iterable<Object>) value);
	}

	public void addArgs(final Object... args) {
		DefaultGroovyMethods.addAll(this.args, args);
	}

	public void setOptions(final Iterable<?> value) {
		this.options = value;
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

	@InputFile
	@PathSensitive(RELATIVE)
	public File getScript() {
		if (this.script != null && this.script.isDirectory()) {
			getLogger().warn("Using the NodeTask with a script directory ({}) is deprecated. " + "It will no longer be supported in the next major version.", this.script);
			return new File(this.script, "index.js");
		}

		return this.script;
	}

	@Input
	public List<?> getArgs() {
		return this.args;
	}

	@Input
	public Iterable<?> getOptions() {
		return this.options;
	}

	@Nested
	public NodeExecRunner getRunner() {
		return this.runner;
	}

	@TaskAction
	public void exec() {
		if (this.script == null) {
			throw new IllegalStateException("Required script property is not set.");
		}

		List<Object> execArgs = new ArrayList();
		execArgs.addAll(DefaultGroovyMethods.asType(this.options, List.class));
		execArgs.add(this.script.getAbsolutePath());
		execArgs.addAll(DefaultGroovyMethods.asType(this.args, List.class));

		this.runner.setArguments(execArgs);
		this.result = this.runner.execute();
	}

	protected NodeExecRunner runner;
	private File script;
	private List<Object> args = new ArrayList<>();
	private Iterable<?> options = new ArrayList<>();
	private ExecResult result;
}
