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

	public boolean getWindows() {
		return windows;
	}

	public boolean isWindows() {
		return windows;
	}

	public void setWindows(boolean windows) {
		this.windows = windows;
	}

	public String getNodeExec() {
		return nodeExec;
	}

	public void setNodeExec(String nodeExec) {
		this.nodeExec = nodeExec;
	}

	public String getNpmScriptFile() {
		return npmScriptFile;
	}

	public void setNpmScriptFile(String npmScriptFile) {
		this.npmScriptFile = npmScriptFile;
	}

	public String getNpxScriptFile() {
		return npxScriptFile;
	}

	public void setNpxScriptFile(String npxScriptFile) {
		this.npxScriptFile = npxScriptFile;
	}

	public File getNodeDir() {
		return nodeDir;
	}

	public void setNodeDir(File nodeDir) {
		this.nodeDir = nodeDir;
	}

	public File getNodeBinDir() {
		return nodeBinDir;
	}

	public void setNodeBinDir(File nodeBinDir) {
		this.nodeBinDir = nodeBinDir;
	}

	public String getNpmExec() {
		return npmExec;
	}

	public void setNpmExec(String npmExec) {
		this.npmExec = npmExec;
	}

	public String getNpxExec() {
		return npxExec;
	}

	public void setNpxExec(String npxExec) {
		this.npxExec = npxExec;
	}

	public File getNpmDir() {
		return npmDir;
	}

	public void setNpmDir(File npmDir) {
		this.npmDir = npmDir;
	}

	public File getNpmBinDir() {
		return npmBinDir;
	}

	public void setNpmBinDir(File npmBinDir) {
		this.npmBinDir = npmBinDir;
	}

	public String getYarnExec() {
		return yarnExec;
	}

	public void setYarnExec(String yarnExec) {
		this.yarnExec = yarnExec;
	}

	public File getYarnDir() {
		return yarnDir;
	}

	public void setYarnDir(File yarnDir) {
		this.yarnDir = yarnDir;
	}

	public File getYarnBinDir() {
		return yarnBinDir;
	}

	public void setYarnBinDir(File yarnBinDir) {
		this.yarnBinDir = yarnBinDir;
	}

	public String getArchiveDependency() {
		return archiveDependency;
	}

	public void setArchiveDependency(String archiveDependency) {
		this.archiveDependency = archiveDependency;
	}

	public String getExeDependency() {
		return exeDependency;
	}

	public void setExeDependency(String exeDependency) {
		this.exeDependency = exeDependency;
	}
}
