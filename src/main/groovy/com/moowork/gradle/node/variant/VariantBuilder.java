package com.moowork.gradle.node.variant;

import com.moowork.gradle.node.NodeExtension;
import com.moowork.gradle.node.util.PlatformHelper;
import org.codehaus.groovy.runtime.StringGroovyMethods;

import java.io.File;
import java.util.List;


public class VariantBuilder {

	private final NodeExtension ext;
	private PlatformHelper platformHelper;

	public VariantBuilder(final NodeExtension ext) {
		this.ext = ext;
		this.platformHelper = PlatformHelper.getINSTANCE();
	}

	public VariantBuilder(final NodeExtension ext, PlatformHelper platformHelper) {
		this(ext);
		this.platformHelper = platformHelper;
	}

	public Variant build() {
		String osName = this.platformHelper.getOsName();
		String osArch = this.platformHelper.getOsArch();

		Variant variant = new Variant();
		variant.setWindows(this.platformHelper.isWindows());

		variant.setNodeDir(getNodeDir(osName, osArch));
		variant.setNpmDir(StringGroovyMethods.asBoolean(this.ext.getNpmVersion()) ? getNpmDir() : variant.getNodeDir());
		variant.setYarnDir(getYarnDir());

		variant.setNodeBinDir(variant.getNodeDir());
		variant.setNpmBinDir(variant.getNpmDir());
		variant.setYarnBinDir(variant.getYarnDir());

		variant.setNodeExec("node");
		variant.setNpmExec(this.ext.getNpmCommand());
		variant.setNpxExec(this.ext.getNpxCommand());
		variant.setYarnExec(this.ext.getYarnCommand());

		if (variant.getWindows()) {
			if (variant.getNpmExec().equals("npm")) {
				variant.setNpmExec("npm.cmd");
			}

			if (variant.getNpxExec().equals("npx")) {
				variant.setNpxExec("npx.cmd");
			}

			if (variant.getYarnExec().equals("yarn")) {
				variant.setYarnExec("yarn.cmd");
			}

			if (hasWindowsZip()) {
				variant.setArchiveDependency(getArchiveDependency(osName, osArch, "zip"));
			} else {
				variant.setArchiveDependency(getArchiveDependency("linux", "x86", "tar.gz"));
				variant.setExeDependency(getExeDependency());
			}

			variant.setNpmScriptFile(new File(variant.getNodeDir(), "node_modules/npm/bin/npm-cli.js").getPath());
			variant.setNpxScriptFile(new File(variant.getNodeDir(), "node_modules/npm/bin/npx-cli.js").getPath());
		} else {
			variant.setNodeBinDir(new File(variant.getNodeBinDir(), "bin"));
			variant.setNpmBinDir(new File(variant.getNpmBinDir(), "bin"));
			variant.setYarnBinDir(new File(variant.getYarnBinDir(), "bin"));
			variant.setArchiveDependency(getArchiveDependency(osName, osArch, "tar.gz"));
			variant.setNpmScriptFile(new File(variant.getNodeDir(), "lib/node_modules/npm/bin/npm-cli.js").getPath());
			variant.setNpxScriptFile(new File(variant.getNodeDir(), "lib/node_modules/npm/bin/npx-cli.js").getPath());
		}

		if (this.ext.getDownload()) {
			if (variant.getNodeExec().equals("node") && variant.getWindows()) {
				variant.setNodeExec("node.exe");
			}

			variant.setNodeExec(new File(variant.getNodeBinDir(), variant.getNodeExec()).getAbsolutePath());
			variant.setNpmExec(new File(variant.getNpmBinDir(), variant.getNpmExec()).getAbsolutePath());
			variant.setNpxExec(new File(variant.getNpmBinDir(), variant.getNpxExec()).getAbsolutePath());
			variant.setYarnExec(new File(variant.getYarnBinDir(), variant.getYarnExec()).getAbsolutePath());
		}

		return variant;
	}

	private String getArchiveDependency(final String osName, final String osArch, final String type) {
		final String version = this.ext.getVersion();
		return "org.nodejs:node:" + version + ":" + osName + "-" + osArch + "@" + type;
	}

	private String getExeDependency() {
		final String version = this.ext.getVersion();
		String osArch = this.platformHelper.getOsArch();
		Integer majorVersion = StringGroovyMethods.toInteger(StringGroovyMethods.tokenize(version, ".").get(0));
		if (majorVersion > 3) {
			if (osArch.equals("x86")) {
				return "org.nodejs:win-x86/node:" + version + "@exe";
			} else {
				return "org.nodejs:win-x64/node:" + version + "@exe";
			}
		} else {
			if (osArch.equals("x86")) {
				return "org.nodejs:node:" + version + "@exe";
			} else {
				return "org.nodejs:x64/node:" + version + "@exe";
			}
		}
	}

	private boolean hasWindowsZip() {
		String version = this.ext.getVersion();
		List<String> tokens = StringGroovyMethods.tokenize(version, ".");
		Integer majorVersion = StringGroovyMethods.toInteger(tokens.get(0));
		Integer minorVersion = StringGroovyMethods.toInteger(tokens.get(1));
		Integer microVersion = StringGroovyMethods.toInteger(tokens.get(2));
		if ((majorVersion == 4 && minorVersion >= 5) || (majorVersion == 6 && (minorVersion > 2 || (minorVersion == 2 && microVersion >= 1))) || majorVersion > 6) {
			return true;
		}

		return false;
	}

	private File getNodeDir(final String osName, final String osArch) {
		final String version = this.ext.getVersion();
		String dirName = "node-v" + version + "-" + osName + "-" + osArch;
		return new File(this.ext.getWorkDir(), dirName);
	}

	private File getNpmDir() {
		final String version = this.ext.getNpmVersion();
		return new File(this.ext.getNpmWorkDir(), "npm-v" + version);
	}

	private File getYarnDir() {
		String dirname = "yarn";
		if (StringGroovyMethods.asBoolean(this.ext.getYarnVersion())) {
			dirname += "-v" + this.ext.getYarnVersion();
		} else {
			dirname += "-latest";
		}

		return new File(this.ext.getYarnWorkDir(), dirname);
	}
}
