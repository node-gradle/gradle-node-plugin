package com.github.gradle.node.npm.task

import com.github.gradle.AbstractIntegTest
import com.github.gradle.node.ProxyTestHelper
import org.gradle.testkit.runner.TaskOutcome
import org.mockserver.integration.ClientAndServer
import org.mockserver.socket.PortFactory

import static org.mockserver.integration.ClientAndServer.startClientAndServer
import static org.mockserver.model.HttpRequest.request
import static org.mockserver.verify.VerificationTimes.exactly

class NpmProxy_integTest extends AbstractIntegTest {
    private ClientAndServer proxyMockServer

    def setup() {
        proxyMockServer = startClientAndServer(PortFactory.findFreePort())
    }

    void cleanup() {
        proxyMockServer.stop()
    }

    def 'install packages using proxy (#gv.version)'() {
        given:
        gradleVersion = gv
        boolean ignoreHost = false
        // Does not work with HTTPS for now, protocol issue
        def secure = false

        copyResources("fixtures/npm-proxy/")
        copyResources("fixtures/proxy/")
        def proxyTestHelper = new ProxyTestHelper(projectDir)
        def port = secure ? 443 : 80
        proxyTestHelper.writeGradleProperties(secure, ignoreHost, proxyMockServer.localPort,
                "registry.npmjs.org:${port}")
        proxyTestHelper.writeNpmConfiguration(secure)

        when:
        def result = build("npmInstall")

        then:
        result.task(":npmInstall").outcome == TaskOutcome.SUCCESS
        !result.output.contains("npm WARN registry Using stale data from https://registry.npmjs.org/ due " +
                "to a request error during revalidation.")
        createFile("node_modules/case/package.json").exists()
        if (ignoreHost) {
            proxyMockServer.verifyZeroInteractions()
        } else {
            proxyMockServer.verify(request()
                    .withMethod("GET")
                    .withPath("/case")
                    .withHeader("Host", "registry.npmjs.org.*"),
                    exactly(1))
        }

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

    def 'install packages using proxy ignoring host (#gv.version)'() {
        given:
        gradleVersion = gv
        // Does not work with HTTPS for now, protocol issue
        def secure = false
        boolean ignoreHost = true

        copyResources("fixtures/npm-proxy/")
        copyResources("fixtures/proxy/")
        def proxyTestHelper = new ProxyTestHelper(projectDir)
        def port = secure ? 443 : 80
        proxyTestHelper.writeGradleProperties(secure, ignoreHost, proxyMockServer.localPort,
                "registry.npmjs.org:${port}")
        proxyTestHelper.writeNpmConfiguration(secure)

        when:
        def result = build("npmInstall")

        then:
        result.task(":npmInstall").outcome == TaskOutcome.SUCCESS
        !result.output.contains("npm WARN registry Using stale data from https://registry.npmjs.org/ due " +
                "to a request error during revalidation.")
        createFile("node_modules/case/package.json").exists()
        if (ignoreHost) {
            proxyMockServer.verifyZeroInteractions()
        } else {
            proxyMockServer.verify(request()
                    .withMethod("GET")
                    .withPath("/case")
                    .withHeader("Host", "registry.npmjs.org.*"),
                    exactly(1))
        }

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

    def 'install packages using pre-configured proxy (#gv.version)'() {
        given:
        gradleVersion = gv

        copyResources("fixtures/npm-proxy/")
        copyResources("fixtures/proxy/")
        def proxyTestHelper = new ProxyTestHelper(projectDir)
        def port = 80
        // Intentionally write the wrong port to the file
        proxyTestHelper.writeGradleProperties(false, false, proxyMockServer.localPort+5,
                null)
        proxyTestHelper.writeNpmConfiguration(false)
        def proxyAddress = "http://localhost:${proxyMockServer.localPort}".toString()

        when:

        Map<String, String> env = new HashMap<>()
        env.putAll(System.getenv())
        env.putAll(["HTTP_PROXY": proxyAddress, "HTTPS_PROXY": proxyAddress])
        def result = buildWithEnvironment(env
                                          ,"npmInstall")

        then:
        result.task(":npmInstall").outcome == TaskOutcome.SUCCESS
        !result.output.contains("npm WARN registry Using stale data from https://registry.npmjs.org/ due " +
                "to a request error during revalidation.")
        createFile("node_modules/case/package.json").exists()
        proxyMockServer.verify(request()
                .withMethod("GET")
                .withPath("/case")
                .withHeader("Host", "registry.npmjs.org.*"),
                exactly(1))

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }
}
