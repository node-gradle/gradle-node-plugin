package com.moowork.gradle.node.task;

import com.moowork.gradle.node.NodeExtension;
import com.moowork.gradle.node.NodePlugin;
import com.moowork.gradle.node.variant.Variant;
import groovy.lang.Closure;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.repositories.IvyArtifactRepository;
import org.gradle.api.file.CopySpec;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SetupTask extends DefaultTask {

	public static final String NAME = "nodeSetup";
	private NodeExtension config;
	protected Variant variant;

	public SetupTask() {

		this.setGroup(NodePlugin.NODE_GROUP);
		this.setDescription("Download and install a local node/npm version.");
		this.setEnabled(false);
	}

	@Input
	public Set<String> getInput() {
		configureIfNeeded();

		Set<String> set = new HashSet<>();
		set.add(Boolean.toString(this.config.getDownload()));
		set.add(this.variant.getArchiveDependency());
		set.add(this.variant.getExeDependency());
		return set;
	}

	@OutputDirectory
	public File getNodeDir() {
		configureIfNeeded();
		return this.variant.getNodeDir();
	}

	private void configureIfNeeded() {
		if (this.config != null) {
			return;
		}

		this.config = NodeExtension.get(this.getProject());
		this.variant = this.config.getVariant();
	}

	@TaskAction
	public void exec() {
		configureIfNeeded();
		addRepositoryIfNeeded();

		if (StringGroovyMethods.asBoolean(this.variant.getExeDependency())) {
			copyNodeExe();
		}

		deleteExistingNode();
		unpackNodeArchive();
		setExecutableFlag();
	}

	private void copyNodeExe() {
		this.getProject().copy(new Closure<Object>(this, this) {
			public Object doCall(CopySpec it) {
				invokeMethod("from", new Object[]{getNodeExeFile()});
				invokeMethod("into", new Object[]{SetupTask.this.variant.getNodeBinDir()});
				return invokeMethod("rename", new Object[]{"node.+\\.exe", "node.exe"});
			}

			public Object doCall() {
				return doCall(null);
			}
		});
	}

	private void deleteExistingNode() {
		this.getProject().delete(getNodeDir().getParent());
	}

	private void unpackNodeArchive() {
		if (getNodeArchiveFile().getName().endsWith("zip")) {
			this.getProject().copy(new Closure<Object>(this, this) {
				public Object doCall(CopySpec it) {
					invokeMethod("from", new Object[]{SetupTask.this.getProject().zipTree(getNodeArchiveFile())});
					return invokeMethod("into", new Object[]{getNodeDir().getParent()});
				}

				public Object doCall() {
					return doCall(null);
				}
			});
		} else if (StringGroovyMethods.asBoolean(this.variant.getExeDependency())) {
			//Remap lib/node_modules to node_modules (the same directory as node.exe) because that's how the zip dist does it
			this.getProject().copy(copySpec -> {
				copySpec.from(SetupTask.this.getProject().tarTree(getNodeArchiveFile()));
				copySpec.into(SetupTask.this.variant.getNodeBinDir());
				Pattern nodeModulesPattern = Pattern.compile("^.*?[\\\\/]lib[\\\\/](node_modules.*$)");
				copySpec.eachFile(file -> {
					Matcher m = nodeModulesPattern.matcher(file.getPath());
					if (m.matches()) {
						// remap the file to the root
						file.setPath(m.group(1));
					} else {
						file.exclude();
					}
				});
				copySpec.setIncludeEmptyDirs(false);
			});
		} else {
			this.getProject().copy(copySpec -> {
				copySpec.from(this.getProject().tarTree(getNodeArchiveFile()));
				copySpec.into(getNodeDir().getParent());
			});
			try {
				// Fix broken symlink
				Path npm = Paths.get(this.variant.getNodeBinDir().getPath(), "npm");
				if (Files.deleteIfExists(npm)) {
					Files.createSymbolicLink(npm, Paths.get(this.variant.getNpmScriptFile()));
				}
				Path npx = Paths.get(this.variant.getNodeBinDir().getPath(), "npx");
				if (Files.deleteIfExists(npx)) {
					Files.createSymbolicLink(npx, Paths.get(this.variant.getNpxScriptFile()));
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void setExecutableFlag() {
		if (!this.variant.getWindows()) {
			new File(this.variant.getNodeExec()).setExecutable(true);
		}
	}

	@Internal
	protected File getNodeExeFile() {
		return resolveSingle(this.variant.getExeDependency());
	}

	@Internal
	protected File getNodeArchiveFile() {
		return resolveSingle(this.variant.getArchiveDependency());
	}

	private File resolveSingle(String name) {
		Dependency dep = this.getProject().getDependencies().create(name);
		Configuration conf = this.getProject().getConfigurations().detachedConfiguration(dep);
		conf.setTransitive(false);
		return conf.resolve().iterator().next();
	}

	private void addRepositoryIfNeeded() {
		if (this.config.getDistBaseUrl() != null) {
			addRepository(this.config.getDistBaseUrl());
		}
	}

	private void addRepository(final String distUrl) {
		this.getProject().getRepositories().ivy(ivyRepository -> {
			ivyRepository.setUrl(distUrl);
			ivyRepository.patternLayout(ivyPatternLayout -> {
				ivyPatternLayout.artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]");
				ivyPatternLayout.ivy("v[revision]/ivy.xml");
			});
			ivyRepository.metadataSources(IvyArtifactRepository.MetadataSources::artifact);
		});
	}
}
