package com.moowork.gradle.node.npm;

import com.moowork.gradle.node.NodeExtension;
import com.moowork.gradle.node.NodePlugin;
import groovy.lang.Closure;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.gradle.api.Project;
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

	public NpmInstallTask() {
		this.setGroup(NodePlugin.NODE_GROUP);
		this.setDescription("Install node packages from package.json.");
		dependsOn(NpmSetupTask.NAME);

		this.getProject().afterEvaluate(new Closure<Object>(this, this) {
			public void doCall(Project it) {
				NodeExtension ext = NpmInstallTask.this.getProject().getExtensions().getByType(NodeExtension.class);
				setNpmCommand(new String[]{ext.getNpmInstallCommand()});
			}

			public void doCall() {
				doCall(null);
			}
		});
	}

	public void setNodeModulesOutputFilter(Closure nodeModulesOutputFilter) {
		this.nodeModulesOutputFilter = nodeModulesOutputFilter;
	}

	@InputFile
	@PathSensitive(PathSensitivity.RELATIVE)
	protected File getPackageJsonFile() {
		File file = new File(this.getProject().getExtensions().getByType(NodeExtension.class).getNodeModulesDir(), "package.json");
		return file.exists() ? file : null;
	}

	@InputFile
	@Optional
	@PathSensitive(PathSensitivity.RELATIVE)
	protected File getNpmShrinkwrap() {
		File file = new File(this.getProject().getExtensions().getByType(NodeExtension.class).getNodeModulesDir(), "npm-shrinkwrap.json");
		return file.exists() ? file : null;
	}

	@InputFile
	@Optional
	@PathSensitive(PathSensitivity.RELATIVE)
	protected File getPackageLockFileAsInput() {
		File lockFile = new File(this.getProject().getExtensions().getByType(NodeExtension.class).getNodeModulesDir(), "package-lock.json");
		if (getNpmCommand()[0].equals("ci")) {
			return lockFile.exists() ? lockFile : null;
		}

		return null;
	}

	@OutputFile
	@Optional
	protected File getPackageLockFileAsOutput() {
		if (getNpmCommand()[0].equals("install")) {
			File file = new File(this.getProject().getExtensions().getByType(NodeExtension.class).getNodeModulesDir(), "package-lock.json");
			return file.exists() ? file : null;
		}

		return null;
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

	public static final String NAME = "npmInstall";
	private Closure nodeModulesOutputFilter;
}
