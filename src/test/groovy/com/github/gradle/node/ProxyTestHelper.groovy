package com.github.gradle.node

import static java.util.stream.Collectors.joining

class ProxyTestHelper {
    private final File projectDirectory

    ProxyTestHelper(File projectDirectory) {
        this.projectDirectory = projectDirectory
    }

    void writeGradleProperties(boolean secure, boolean ignoreHost, int proxyPort, String ignoredHosts) {
        def prefix = secure ? "https" : "http"
        def properties = [
                "proxyHost": "localhost",
                "proxyPort": "${proxyPort}"
        ]
        if (ignoreHost) {
            properties["nonProxyHosts"] = ignoredHosts
        }
        def gradlePropertiesFile = createFile("gradle.properties")
        def gradlePropertiesFileContents = properties.entrySet().stream()
                .map { entry -> "systemProp.${prefix}.${entry.key}=${entry.value}" }
                .collect(joining("\n"))
        gradlePropertiesFile.text = gradlePropertiesFileContents
    }

    void writeNpmConfiguration(boolean secure) {
        def file = createFile(".npmrc")
        if (secure) {
            def certificate = readMockServerCertificate()
                    .replace("\n", "\\n")
            file.text = "ca=\"${certificate}\""
        } else {
            file.text = "registry=http://registry.npmjs.org/"
        }
    }

    void writeYarnConfiguration(boolean secure, int port) {
        def file = createFile(".yarnrc")
        if (secure) {
            def certificateFile = createFile("certificate.pem")
            certificateFile.text = readMockServerCertificate()
            file.text = """cafile \"${projectDirectory}/certificate.pem\"
strict-ssl false"""
        } else {
            file.text = "registry \"http://localhost:${port}/\""
        }
    }

    private def readMockServerCertificate() {
        def stream = getClass().getClassLoader()
                .getResourceAsStream("org/mockserver/socket/CertificateAuthorityCertificate.pem")
        return stream.text
    }

    private File createFile(String name) {
        return new File(projectDirectory, name)
    }
}
