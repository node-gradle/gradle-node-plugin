package com.moowork.gradle.node.yarn;

import com.moowork.gradle.node.NodeExtension;
import com.moowork.gradle.node.NodePlugin;
import groovy.lang.Closure;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFiles;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;

import java.io.File;


/**
 * yarn install that only gets executed if gradle decides so.
 */
public class YarnInstallTask extends YarnTask {

	public static final String NAME = "yarn";

	private Closure<ConfigurableFileTree> nodeModulesOutputFilter;

	public YarnInstallTask() {
		this.setGroup(NodePlugin.NODE_GROUP);
		this.setDescription("Install node packages using Yarn.");
		setYarnCommand(new String[]{""});
		dependsOn(YarnSetupTask.NAME);
	}

	@InputFile
	@Optional
	@PathSensitive(PathSensitivity.RELATIVE)
	protected File getPackageJsonFile() {
		File packageJsonFile = new File(this.getProject().getExtensions().getByType(NodeExtension.class).getNodeModulesDir(), "package.json");
		return packageJsonFile.exists() ? packageJsonFile : null;
	}

	@InputFile
	@Optional
	@PathSensitive(PathSensitivity.RELATIVE)
	protected File getYarnLockFile() {
		File lockFile = new File(this.getProject().getExtensions().getByType(NodeExtension.class).getNodeModulesDir(), "yarn.lock");
		return lockFile.exists() ? lockFile : null;
	}

	@OutputFiles
	protected ConfigurableFileTree getNodeModulesDir() {
		File nodeModulesDirectory = new File(this.getProject().getExtensions().getByType(NodeExtension.class).getNodeModulesDir(), "node_modules");
		ConfigurableFileTree nodeModulesFileTree = getProject().fileTree(nodeModulesDirectory);
		if (DefaultGroovyMethods.asBoolean(this.nodeModulesOutputFilter)) {
			this.nodeModulesOutputFilter.call(nodeModulesFileTree);
		}

		return nodeModulesFileTree;
	}

	public void setNodeModulesOutputFilter(Closure<ConfigurableFileTree> nodeModulesOutputFilter) {
		this.nodeModulesOutputFilter = nodeModulesOutputFilter;
	}
}
