package com.github.gradle.node.npm.task

import com.github.gradle.AbstractIntegTest
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.mockserver.junit.MockServerRule

import java.util.stream.Collectors

import static org.mockserver.model.HttpRequest.request
import static org.mockserver.verify.VerificationTimes.once

class NpmProxy_integTest extends AbstractIntegTest {
    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this)

    def 'install packages using proxy'(boolean secure, boolean ignoreHost) {
        given:
        copyResources("fixtures/npm-proxy/")
        writeGradleProperties(secure, ignoreHost)
        writeNpmConfiguration(secure)

        when:
        def result = build("npmInstall")

        then:
        result.task(":npmInstall").outcome == TaskOutcome.SUCCESS
        !result.output.contains("npm WARN registry Using stale data from https://registry.npmjs.org/ due " +
                "to a request error during revalidation.")
        createFile("node_modules/case/package.json").exists()
        if (ignoreHost) {
            mockServerRule.client.verifyZeroInteractions()
        } else {
            mockServerRule.client.verify(request()
                    .withMethod("GET")
                    .withSecure(secure)
                    .withHeader("Host", "registry.npmjs.org:${secure ? 443 : 80}")
                    .withPath("/case"), once())
        }

        where:
        secure | ignoreHost
        false  | false
        // -c noproxy=registry.npmjs.org:80 is not recognized by npm
        // false  | true
        // Does not work with HTTPS for now, certificate issue
        // true   | true
        // true   | false
    }

    private def writeGradleProperties(boolean secure, boolean ignoreHost) {
        def prefix = secure ? "https" : "http"
        def properties = [
                "proxyHost": "localhost",
                "proxyPort": "${mockServerRule.port}"
        ]
        if (ignoreHost) {
            properties["nonProxyHosts"] = "registry.npmjs.org"
        }
        def gradlePropertiesFile = createFile("gradle.properties")
        def gradlePropertiesFileContents = properties.entrySet().stream()
                .map { entry -> "systemProp.${prefix}.${entry.key}=${entry.value}" }
                .collect(Collectors.joining("\n"))
        gradlePropertiesFile.text = gradlePropertiesFileContents
    }

    private def writeNpmConfiguration(boolean secure) {
        def file = createFile(".npmrc")
        if (secure) {
            def certificate = readMockServerCertificate()
                    .replace("\n", "\\n")
            file.text = "ca=${certificate}"
        } else {
            file.text = "registry=http://registry.npmjs.org/"
        }
    }

    private def readMockServerCertificate() {
        def stream = getClass().getClassLoader()
                .getResourceAsStream("org/mockserver/socket/CertificateAuthorityCertificate.pem")
        return stream.text
    }
}
