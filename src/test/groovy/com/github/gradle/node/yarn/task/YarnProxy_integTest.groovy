package com.github.gradle.node.yarn.task

import com.github.gradle.AbstractIntegTest
import com.github.gradle.node.ProxyTestHelper
import org.gradle.testkit.runner.TaskOutcome
import org.mockserver.integration.ClientAndServer
import org.mockserver.socket.PortFactory

import static java.nio.charset.StandardCharsets.UTF_8
import static org.mockserver.integration.ClientAndServer.startClientAndServer
import static org.mockserver.model.HttpRequest.request
import static org.mockserver.model.SocketAddress.Scheme.HTTPS
import static org.mockserver.verify.VerificationTimes.exactly

class YarnProxy_integTest extends AbstractIntegTest {
    private ClientAndServer proxyMockServer
    // We have to configure a second proxy otherwise Yarn does not want to use its own repository using HTTP,
    // it enforces HTTPS (see https://github.com/yarnpkg/yarn/blob/v1.22.4/src/registries/npm-registry.js#L122)
    // This proxy is the configured yarn registry
    private ClientAndServer registryMockServer

    def setup() {
        proxyMockServer = startClientAndServer(PortFactory.findFreePort())
        registryMockServer = startClientAndServer(PortFactory.findFreePort())
    }

    void cleanup() {
        proxyMockServer.stop()
        registryMockServer.stop()
    }

    def 'install packages using proxy (#gv.version)'() {
        given:
        gradleVersion = gv
        boolean ignoreHost = false
        // Does not work with HTTPS for now, certificate issue
        boolean secure = false

        copyResources("fixtures/yarn-proxy/")
        copyResources("fixtures/proxy/")
        // Install yarn before setting the proxy (npm does not work with HTTPS proxy)
        build("yarnSetup")
        def proxyTestHelper = new ProxyTestHelper(projectDir)
        proxyTestHelper.writeGradleProperties(secure, ignoreHost, proxyMockServer.localPort,
                "localhost:${registryMockServer.localPort}")
        proxyTestHelper.writeYarnConfiguration(secure, registryMockServer.localPort)
        registryMockServer.when(request())
                .forward({ request ->
                    request.removeHeader("host")
                    def targetHost = "registry.npmjs.org"
                    request.withHeader("host", targetHost)
                    return request.withSocketAddress(targetHost, 443, HTTPS).withSecure(true)
                }, { request, response ->
                    if (response.getBody().contentType == "application/vnd.npm.install-v1+json") {
                        // Let's rewrite download URLs in the JSON to make them target to the proxied registry
                        def body = response.getBodyAsString()
                        def updatedBody = body.replaceAll("https://registry.npmjs.org/",
                                "http://localhost:${registryMockServer.localPort}/")
                        response.withBody(updatedBody)
                                .removeHeader("Content-Length")
                                .withHeader("Content-Length",
                                        updatedBody.getBytes(UTF_8).length.toString())
                    }
                    return response
                })

        when:
        def result = build("yarn")

        then:
        result.task(":cleanCaseCache").outcome == TaskOutcome.SUCCESS
        result.task(":yarn").outcome == TaskOutcome.SUCCESS
        createFile("node_modules/case/package.json").exists()
        if (ignoreHost) {
            proxyMockServer.verifyZeroInteractions()
        } else {
            proxyMockServer.verify(request()
                    .withMethod("GET")
                    .withSecure(secure)
                    .withPath("/case")
                    .withHeader("Host", "localhost:${registryMockServer.localPort}"),
                    exactly(1))
            proxyMockServer.verify(request()
                    .withMethod("GET")
                    .withSecure(secure)
                    .withPath("/case/-/case-1.6.3.tgz")
                    .withHeader("Host", "localhost:${registryMockServer.localPort}"),
                    exactly(1))
        }

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

    def 'install packages using proxy ignoring host (#gv.version)'() {
        given:
        gradleVersion = gv
        boolean ignoreHost = true
        // Does not work with HTTPS for now, certificate issue
        boolean secure = false

        copyResources("fixtures/yarn-proxy/")
        copyResources("fixtures/proxy/")
        // Install yarn before setting the proxy (npm does not work with HTTPS proxy)
        build("yarnSetup")
        def proxyTestHelper = new ProxyTestHelper(projectDir)
        proxyTestHelper.writeGradleProperties(secure, ignoreHost, proxyMockServer.localPort,
                "localhost:${registryMockServer.localPort}")
        proxyTestHelper.writeYarnConfiguration(secure, registryMockServer.localPort)
        registryMockServer.when(request())
                .forward({ request ->
                    request.removeHeader("host")
                    def targetHost = "registry.npmjs.org"
                    request.withHeader("host", targetHost)
                    return request.withSocketAddress(targetHost, 443, HTTPS).withSecure(true)
                }, { request, response ->
                    if (response.getBody().contentType == "application/vnd.npm.install-v1+json") {
                        // Let's rewrite download URLs in the JSON to make them target to the proxied registry
                        def body = response.getBodyAsString()
                        def updatedBody = body.replaceAll("https://registry.npmjs.org/",
                                "http://localhost:${registryMockServer.localPort}/")
                        response.withBody(updatedBody)
                                .removeHeader("Content-Length")
                                .withHeader("Content-Length",
                                        updatedBody.getBytes(UTF_8).length.toString())
                    }
                    return response
                })

        when:
        def result = build("yarn")

        then:
        result.task(":cleanCaseCache").outcome == TaskOutcome.SUCCESS
        result.task(":yarn").outcome == TaskOutcome.SUCCESS
        createFile("node_modules/case/package.json").exists()
        if (ignoreHost) {
            proxyMockServer.verifyZeroInteractions()
        } else {
            proxyMockServer.verify(request()
                    .withMethod("GET")
                    .withSecure(secure)
                    .withPath("/case")
                    .withHeader("Host", "localhost:${registryMockServer.localPort}"),
                    exactly(1))
            proxyMockServer.verify(request()
                    .withMethod("GET")
                    .withSecure(secure)
                    .withPath("/case/-/case-1.6.3.tgz")
                    .withHeader("Host", "localhost:${registryMockServer.localPort}"),
                    exactly(1))
        }

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }

    def 'install packages using pre-configured proxy (#gv.version)'() {
        given:
        gradleVersion = gv

        copyResources("fixtures/yarn-proxy/")
        copyResources("fixtures/proxy/")
        // Install yarn before setting the proxy (npm does not work with HTTPS proxy)
        build("yarnSetup")
        def proxyTestHelper = new ProxyTestHelper(projectDir)
        // Intentionally write the wrong port to the file
        proxyTestHelper.writeGradleProperties(false, false, proxyMockServer.localPort+5,
                "localhost:${registryMockServer.localPort}")
        proxyTestHelper.writeYarnConfiguration(false, registryMockServer.localPort)
        def proxyAddress = "http://localhost:${proxyMockServer.localPort}".toString()
        registryMockServer.when(request())
                .forward({ request ->
                    request.removeHeader("host")
                    def targetHost = "registry.npmjs.org"
                    request.withHeader("host", targetHost)
                    return request.withSocketAddress(targetHost, 443, HTTPS).withSecure(true)
                }, { request, response ->
                    if (response.getBody().contentType == "application/vnd.npm.install-v1+json") {
                        // Let's rewrite download URLs in the JSON to make them target to the proxied registry
                        def body = response.getBodyAsString()
                        def updatedBody = body.replaceAll("https://registry.npmjs.org/",
                                "http://localhost:${registryMockServer.localPort}/")
                        response.withBody(updatedBody)
                                .removeHeader("Content-Length")
                                .withHeader("Content-Length",
                                        updatedBody.getBytes(UTF_8).length.toString())
                    }
                    return response
                })

        when:
        Map<String, String> env = new HashMap<>()
        env.putAll(System.getenv())
        env.putAll(["HTTP_PROXY": proxyAddress, "HTTPS_PROXY": proxyAddress])
        def result = buildWithEnvironment(env, "yarn")

        then:
        result.task(":cleanCaseCache").outcome == TaskOutcome.SUCCESS
        result.task(":yarn").outcome == TaskOutcome.SUCCESS
        createFile("node_modules/case/package.json").exists()
        proxyMockServer.verify(request()
                .withMethod("GET")
                .withPath("/case")
                .withHeader("Host", "localhost:${registryMockServer.localPort}"),
                exactly(1))
        proxyMockServer.verify(request()
                .withMethod("GET")
                .withPath("/case/-/case-1.6.3.tgz")
                .withHeader("Host", "localhost:${registryMockServer.localPort}"),
                exactly(1))

        where:
        gv << GRADLE_VERSIONS_UNDER_TEST
    }
}
