package com.github.gradle.node.npm.task

import com.github.gradle.AbstractIntegTest
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.mockserver.junit.MockServerRule

import static java.util.stream.Collectors.joining
import static org.mockserver.model.HttpRequest.request
import static org.mockserver.verify.VerificationTimes.exactly

class NpmProxy_integTest extends AbstractIntegTest {
    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this)

    void cleanup() {
        mockServerRule.client.reset()
    }

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
                    .withPath("/case")
                    .withHeader("Host", "registry.npmjs.org:${secure ? 443 : 80}"),
                    exactly(1))
            mockServerRule.client.verify(request()
                    .withMethod("POST")
                    .withSecure(secure)
                    .withPath("/-/npm/v1/security/audits/quick")
                    .withHeader("Host", "registry.npmjs.org:${secure ? 443 : 80}"),
                    exactly(1))
        }

        where:
        secure | ignoreHost
        false  | false
//        false  | true
        // Does not work with HTTPS for now, certificate issue
        // true   | false
        // true   | true
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
                .collect(joining("\n"))
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
