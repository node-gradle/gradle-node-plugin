package com.moowork.gradle.node;

import com.moowork.gradle.node.variant.Variant;
import org.gradle.api.Project;

import java.io.File;


public class NodeExtension {

	public static final String NAME = "node";

	private File workDir;
	private File npmWorkDir;
	private File yarnWorkDir;
	private File nodeModulesDir;
	private String version = "10.14.0";
	private String npmVersion = "";
	private String yarnVersion = "";
	private String distBaseUrl = "https://nodejs.org/dist";
	private String npmCommand = "npm";
	private String npxCommand = "npx";
	private String npmInstallCommand = "install";
	private String yarnCommand = "yarn";
	private boolean download = false;
	private Variant variant;

	public NodeExtension(final Project project) {
		File cacheDir = new File(project.getProjectDir(), ".gradle");
		this.workDir = new File(cacheDir, "nodejs");
		this.npmWorkDir = new File(cacheDir, "npm");
		this.yarnWorkDir = new File(cacheDir, "yarn");
		this.nodeModulesDir = project.getProjectDir();
	}

	public static NodeExtension get(final Project project) {
		return project.getExtensions().getByType(NodeExtension.class);
	}

	public static NodeExtension create(final Project project) {
		return project.getExtensions().create(NAME, NodeExtension.class, project);
	}

	public File getWorkDir() {
		return this.workDir;
	}

	public void setWorkDir(File workDir) {
		this.workDir = workDir;
	}

	public File getNpmWorkDir() {
		return this.npmWorkDir;
	}

	public void setNpmWorkDir(File npmWorkDir) {
		this.npmWorkDir = npmWorkDir;
	}

	public File getYarnWorkDir() {
		return this.yarnWorkDir;
	}

	public void setYarnWorkDir(File yarnWorkDir) {
		this.yarnWorkDir = yarnWorkDir;
	}

	public File getNodeModulesDir() {
		return this.nodeModulesDir;
	}

	public void setNodeModulesDir(File nodeModulesDir) {
		this.nodeModulesDir = nodeModulesDir;
	}

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getNpmVersion() {
		return this.npmVersion;
	}

	public void setNpmVersion(String npmVersion) {
		this.npmVersion = npmVersion;
	}

	public String getYarnVersion() {
		return this.yarnVersion;
	}

	public void setYarnVersion(String yarnVersion) {
		this.yarnVersion = yarnVersion;
	}

	public String getDistBaseUrl() {
		return this.distBaseUrl;
	}

	public void setDistBaseUrl(String distBaseUrl) {
		this.distBaseUrl = distBaseUrl;
	}

	public String getNpmCommand() {
		return this.npmCommand;
	}

	public void setNpmCommand(String npmCommand) {
		this.npmCommand = npmCommand;
	}

	public String getNpxCommand() {
		return this.npxCommand;
	}

	public void setNpxCommand(String npxCommand) {
		this.npxCommand = npxCommand;
	}

	public String getNpmInstallCommand() {
		return this.npmInstallCommand;
	}

	public void setNpmInstallCommand(String npmInstallCommand) {
		this.npmInstallCommand = npmInstallCommand;
	}

	public String getYarnCommand() {
		return this.yarnCommand;
	}

	public void setYarnCommand(String yarnCommand) {
		this.yarnCommand = yarnCommand;
	}

	public boolean getDownload() {
		return this.download;
	}

	public boolean isDownload() {
		return this.download;
	}

	public void setDownload(boolean download) {
		this.download = download;
	}

	public Variant getVariant() {
		return this.variant;
	}

	public void setVariant(Variant variant) {
		this.variant = variant;
	}
}
