package com.moowork.gradle.node.npm;

import com.moowork.gradle.node.NodeExtension;
import com.moowork.gradle.node.NodePlugin;
import com.moowork.gradle.node.task.SetupTask;
import com.moowork.gradle.node.variant.Variant;
import groovy.lang.Closure;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecResult;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * npm install that only gets executed if gradle decides so.
 **/
public class NpmSetupTask extends DefaultTask {

	public static final String NAME = "npmSetup";
	private NpmExecRunner runner;
	private NodeExtension config;
	protected List<Object> args = new ArrayList<>();
	private ExecResult result;

	public NpmSetupTask() {
		dependsOn(SetupTask.NAME);

		this.setGroup(NodePlugin.NODE_GROUP);
		this.setDescription("Setup a specific version of npm to be used by the build.");
		this.setEnabled(false);

		this.runner = new NpmExecRunner(this.getProject());
	}

	@Input
	public Set<Object> getInput() {
		Set<Object> set = new HashSet<>();
		set.add(getConfig().getDownload());
		set.add(getConfig().getNpmVersion());
		set.add(getConfig().getNpmWorkDir());
		return set;
	}

	@OutputDirectory
	public File getNpmDir() {
		return getVariant().getNpmDir();
	}

	@Internal
	public ExecResult getResult() {
		return this.result;
	}

	@Internal
	protected NodeExtension getConfig() {
		if (this.config != null) {
			return this.config;
		}

		this.config = NodeExtension.get(this.getProject());
		return this.config;
	}

	@Internal
	protected Variant getVariant() {
		return getConfig().getVariant();
	}

	@Input
	public List<Object> getArgs() {
		return this.args;
	}

	public void setArgs(final Iterable<?> value) {
		this.args = DefaultGroovyMethods.toList((Iterable<Object>) value);
	}

	public void setIgnoreExitValue(final boolean value) {
		this.runner.setIgnoreExitValue(value);
	}

	public void setExecOverrides(final Closure closure) {
		this.runner.setExecOverrides(closure);
	}

	@Nested
	public NpmExecRunner getRunner() {
		return this.runner;
	}

	@TaskAction
	public void exec() {
		this.runner.getArguments().addAll(this.args);
		this.result = this.runner.execute();
	}

	public void configureVersion(final String npmVersion) {
		if (!npmVersion.isEmpty()) {
			getLogger().debug("Setting npmVersion to {}", npmVersion);
			setArgs(DefaultGroovyMethods.plus(DefaultGroovyMethods.plus(new ArrayList<String>(Arrays.asList("install", "--global", "--no-save")), proxySettings()), new ArrayList<>(Arrays.asList("--prefix", getVariant().getNpmDir().getAbsolutePath(), "npm@" + npmVersion))));
			setEnabled(true);
		}
	}

	public static List<String> proxySettings() {
		for (String[] proxySettings : Arrays.asList(new String[]{"http", "--proxy"}, new String[]{"https", "--https-proxy"})) {
			String proxyHost = System.getProperty(proxySettings[0] + ".proxyHost");
			String proxyPort = System.getProperty(proxySettings[0] + ".proxyPort");
			if (proxyHost != null && proxyPort != null) {
				proxyHost = proxyHost.replaceAll("^https?://", "");
				String proxyUser = System.getProperty(proxySettings[0] + ".proxyUser");
				String proxyPassword = System.getProperty(proxySettings[0] + ".proxyPassword");
				if (proxyUser != null && proxyPassword != null) {
					return Collections.singletonList(proxySettings[1] + " " + proxySettings[0] + "://" + proxyUser + ":" + proxyPassword + "@" + proxyHost + ":" + proxyPort);
				} else {
					return Collections.singletonList(proxySettings[1] + " " + proxySettings[0] + "://" + proxyHost + ":" + proxyPort);
				}
			}
		}

		return new ArrayList<>();
	}
}
