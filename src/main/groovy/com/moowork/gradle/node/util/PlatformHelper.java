package com.moowork.gradle.node.util;

import org.codehaus.groovy.runtime.ProcessGroovyMethods;

import java.util.Properties;


public class PlatformHelper {

	public PlatformHelper() {
		this(System.getProperties());
	}

	public PlatformHelper(final Properties props) {
		this.props = props;
	}

	private String property(final String name) {
		String value = this.props.getProperty(name);
		return value != null ? value : System.getProperty(name);
	}

	public String getOsName() {
		final String name = property("os.name").toLowerCase();
		if (name.contains("windows")) {
			return "win";
		}

		if (name.contains("mac")) {
			return "darwin";
		}

		if (name.contains("linux")) {
			return "linux";
		}

		if (name.contains("freebsd")) {
			return "linux";
		}

		if (name.contains("sunos")) {
			return "sunos";
		}

		throw new IllegalArgumentException("Unsupported OS: " + name);
	}

	public String getOsArch() {
		try {
			final String arch = property("os.arch").toLowerCase();
			//as Java just returns "arm" on all ARM variants, we need a system call to determine the exact arch
			//unfortunately some JVMs say aarch32/64, so we need an additional conditional
			if (arch.equals("arm") || arch.startsWith("aarch")) {
				String systemArch = ProcessGroovyMethods.getText(ProcessGroovyMethods.execute("uname -m")).trim();
				//the node binaries for 'armv8l' are called 'arm64', so we need to distinguish here
				if (systemArch.equals("armv8l")) {
					return "arm64";
				} else {
					return systemArch;
				}
			} else if (arch.contains("64")) {
				return "x64";
			}

			return "x86";
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isWindows() {
		return getOsName().equals("win");
	}

	public static PlatformHelper getINSTANCE() {
		return INSTANCE;
	}

	public static void setINSTANCE(PlatformHelper INSTANCE) {
		PlatformHelper.INSTANCE = INSTANCE;
	}

	private static PlatformHelper INSTANCE = new PlatformHelper();
	private final Properties props;
}
