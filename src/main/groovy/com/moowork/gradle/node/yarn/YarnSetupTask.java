package com.moowork.gradle.node.yarn;

import com.moowork.gradle.node.NodePlugin;
import com.moowork.gradle.node.npm.NpmSetupTask;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 * Setup a specific version of Yarn to be used by the build.
 **/
public class YarnSetupTask extends NpmSetupTask {

	public YarnSetupTask() {
		this.setGroup(NodePlugin.NODE_GROUP);
		this.setDescription("Setup a specific version of Yarn to be used by the build.");

		this.setEnabled(false);
	}

	@Override
	@Input
	public Set<Object> getInput() {
		Set<Object> set = new HashSet<>();
		set.add(this.getConfig().getDownload());
		set.add(this.getConfig().getYarnVersion());
		return set;
	}

	@OutputDirectory
	public File getYarnDir() {
		return this.getVariant().getYarnDir();
	}

	@Override
	public void configureVersion(final String yarnVersion) {
		String pkg = "yarn";

		if (!yarnVersion.isEmpty()) {
			getLogger().debug("Setting yarnVersion to " + yarnVersion);
			pkg += "@" + yarnVersion;
		}

		this.setArgs(DefaultGroovyMethods.plus(DefaultGroovyMethods.plus(new ArrayList<String>(Arrays.asList("install", "--global", "--no-save")), NpmSetupTask.proxySettings()), new ArrayList<>(Arrays.asList("--prefix", this.getVariant().getYarnDir().getAbsolutePath(), pkg))));
		setEnabled(true);
	}

	public static final String NAME = "yarnSetup";
}
