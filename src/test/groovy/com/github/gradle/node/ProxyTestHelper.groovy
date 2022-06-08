package com.github.gradle.node

import org.mockserver.configuration.ConfigurationProperties

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
        def protocol = secure ? "https" : "http"
        def file = createFile(".yarnrc")
        def configuration = "registry \"${protocol}://localhost:${port}/\""
        if (secure) {
            def certificateFile = createFile("certificate.pem")
            certificateFile.text = readMockServerCertificate()
            configuration = """${configuration}
cafile \"${projectDirectory}/certificate.pem\""""
        }
        file.text = configuration
    }

    private def readMockServerCertificate() {
        def stream = getClass().getClassLoader()
                .getResourceAsStream(ConfigurationProperties.certificateAuthorityCertificate())
        return stream.text
    }

    private File createFile(String name) {
        return new File(projectDirectory, name)
    }
}
