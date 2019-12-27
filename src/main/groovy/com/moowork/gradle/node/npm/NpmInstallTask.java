package com.moowork.gradle.node.npm;

import com.moowork.gradle.node.NodeExtension;
import com.moowork.gradle.node.NodePlugin;
import groovy.lang.Closure;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.OutputFiles;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;

import java.io.File;


/**
 * npm install that only gets executed if gradle decides so.
 */
public class NpmInstallTask extends NpmTask {

	public static final String NAME = "npmInstall";

	private Closure<ConfigurableFileTree> nodeModulesOutputFilter;

	public NpmInstallTask() {
		setGroup(NodePlugin.NODE_GROUP);
		setDescription("Install node packages from package.json.");
		dependsOn(NpmSetupTask.NAME);

		getProject().afterEvaluate(it -> {
			NodeExtension ext = getProject().getExtensions().getByType(NodeExtension.class);
			setNpmCommand(new String[]{ext.getNpmInstallCommand()});
		});
	}

	public void setNodeModulesOutputFilter(Closure<ConfigurableFileTree> nodeModulesOutputFilter) {
		this.nodeModulesOutputFilter = nodeModulesOutputFilter;
	}

	@InputFile
	@PathSensitive(PathSensitivity.RELATIVE)
	protected File getPackageJsonFile() {
		File file = new File(getProject().getExtensions().getByType(NodeExtension.class).getNodeModulesDir(), "package.json");
		return file.exists() ? file : null;
	}

	@InputFile
	@Optional
	@PathSensitive(PathSensitivity.RELATIVE)
	protected File getNpmShrinkwrap() {
		File file = new File(getProject().getExtensions().getByType(NodeExtension.class).getNodeModulesDir(), "npm-shrinkwrap.json");
		return file.exists() ? file : null;
	}

	@InputFile
	@Optional
	@PathSensitive(PathSensitivity.RELATIVE)
	protected File getPackageLockFileAsInput() {
		File lockFile = new File(getProject().getExtensions().getByType(NodeExtension.class).getNodeModulesDir(), "package-lock.json");
		if (getNpmCommand()[0].equals("ci")) {
			return lockFile.exists() ? lockFile : null;
		}

		return null;
	}

	@OutputFile
	@Optional
	protected File getPackageLockFileAsOutput() {
		if (getNpmCommand()[0].equals("install")) {
			File file = new File(getProject().getExtensions().getByType(NodeExtension.class).getNodeModulesDir(), "package-lock.json");
			return file.exists() ? file : null;
		}

		return null;
	}

	@OutputFiles
	protected ConfigurableFileTree getNodeModulesDir() {
		File nodeModulesDirectory = new File(getProject().getExtensions().getByType(NodeExtension.class).getNodeModulesDir(), "node_modules");
		ConfigurableFileTree nodeModulesFileTree = getProject().fileTree(nodeModulesDirectory);
		if (DefaultGroovyMethods.asBoolean(this.nodeModulesOutputFilter)) {
			this.nodeModulesOutputFilter.call(nodeModulesFileTree);
		}

		return nodeModulesFileTree;
	}
}
