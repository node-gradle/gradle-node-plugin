package com.moowork.gradle.node.variant;

import java.io.File;


public class Variant {

	private boolean windows;
	private String nodeExec;
	private String npmScriptFile;
	private String npxScriptFile;
	private File nodeDir;
	private File nodeBinDir;
	private String npmExec;
	private String npxExec;
	private File npmDir;
	private File npmBinDir;
	private String yarnExec;
	private File yarnDir;
	private File yarnBinDir;
	private String archiveDependency;
	private String exeDependency;

	public boolean isWindows() {
		return this.windows;
	}

	public void setWindows(boolean windows) {
		this.windows = windows;
	}

	public String getNodeExec() {
		return this.nodeExec;
	}

	public void setNodeExec(String nodeExec) {
		this.nodeExec = nodeExec;
	}

	public String getNpmScriptFile() {
		return this.npmScriptFile;
	}

	public void setNpmScriptFile(String npmScriptFile) {
		this.npmScriptFile = npmScriptFile;
	}

	public String getNpxScriptFile() {
		return this.npxScriptFile;
	}

	public void setNpxScriptFile(String npxScriptFile) {
		this.npxScriptFile = npxScriptFile;
	}

	public File getNodeDir() {
		return this.nodeDir;
	}

	public void setNodeDir(File nodeDir) {
		this.nodeDir = nodeDir;
	}

	public File getNodeBinDir() {
		return this.nodeBinDir;
	}

	public void setNodeBinDir(File nodeBinDir) {
		this.nodeBinDir = nodeBinDir;
	}

	public String getNpmExec() {
		return this.npmExec;
	}

	public void setNpmExec(String npmExec) {
		this.npmExec = npmExec;
	}

	public String getNpxExec() {
		return this.npxExec;
	}

	public void setNpxExec(String npxExec) {
		this.npxExec = npxExec;
	}

	public File getNpmDir() {
		return this.npmDir;
	}

	public void setNpmDir(File npmDir) {
		this.npmDir = npmDir;
	}

	public File getNpmBinDir() {
		return this.npmBinDir;
	}

	public void setNpmBinDir(File npmBinDir) {
		this.npmBinDir = npmBinDir;
	}

	public String getYarnExec() {
		return this.yarnExec;
	}

	public void setYarnExec(String yarnExec) {
		this.yarnExec = yarnExec;
	}

	public File getYarnDir() {
		return this.yarnDir;
	}

	public void setYarnDir(File yarnDir) {
		this.yarnDir = yarnDir;
	}

	public File getYarnBinDir() {
		return this.yarnBinDir;
	}

	public void setYarnBinDir(File yarnBinDir) {
		this.yarnBinDir = yarnBinDir;
	}

	public String getArchiveDependency() {
		return this.archiveDependency;
	}

	public void setArchiveDependency(String archiveDependency) {
		this.archiveDependency = archiveDependency;
	}

	public String getExeDependency() {
		return this.exeDependency;
	}

	public void setExeDependency(String exeDependency) {
		this.exeDependency = exeDependency;
	}
}
