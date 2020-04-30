package com.github.gradle.node.npm.proxy

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class NpmProxyTest {
    @AfterEach
    internal fun tearDown() {
        System.clearProperty("http.proxyHost")
        System.clearProperty("http.proxyPort")
        System.clearProperty("http.proxyUser")
        System.clearProperty("http.proxyPassword")
        System.clearProperty("https.proxyHost")
        System.clearProperty("https.proxyPort")
        System.clearProperty("https.proxyUser")
        System.clearProperty("https.proxyPassword")
    }

    @Test
    internal fun shouldComputeTheProxyArgsWhenNoProxyIsConfigured() {
        val result = NpmProxy.computeNpmProxyCliArgs()

        assertThat(result).isEmpty()
    }

    @Test
    internal fun shouldComputeTheProxyArgsWhenAnHttpProxyIsConfiguredWithoutUserPassword() {
        System.setProperty("http.proxyHost", "1.2.3.4")
        System.setProperty("http.proxyPort", "8123")

        val result = NpmProxy.computeNpmProxyCliArgs()

        assertThat(result).containsExactly("--proxy", "http://1.2.3.4:8123")
    }

    @Test
    internal fun shouldComputeTheProxyArgsWhenAnHttpProxyIsConfiguredWithUserPassword() {
        System.setProperty("http.proxyHost", "4.3.2.1")
        System.setProperty("http.proxyPort", "1234")
        System.setProperty("http.proxyUser", "me/you")
        System.setProperty("http.proxyPassword", "p@ssword")

        val result = NpmProxy.computeNpmProxyCliArgs()

        assertThat(result).containsExactly("--proxy", "http://me%2Fyou:p%40ssword@4.3.2.1:1234")
    }

    @Test
    internal fun shouldComputeTheProxyArgsWhenAnHttpsProxyIsConfiguredWithoutUserPassword() {
        System.setProperty("https.proxyHost", "1.2.3.4")
        System.setProperty("https.proxyPort", "8123")

        val result = NpmProxy.computeNpmProxyCliArgs()

        assertThat(result).containsExactly("--https-proxy", "https://1.2.3.4:8123")
    }

    @Test
    internal fun shouldComputeTheProxyArgsWhenAnHttpsProxyIsConfiguredWithUserPassword() {
        System.setProperty("https.proxyHost", "4.3.2.1")
        System.setProperty("https.proxyPort", "1234")
        System.setProperty("https.proxyUser", "me/you")
        System.setProperty("https.proxyPassword", "p@ssword")

        val result = NpmProxy.computeNpmProxyCliArgs()

        assertThat(result).containsExactly("--https-proxy", "https://me%2Fyou:p%40ssword@4.3.2.1:1234")
    }

    @Test
    internal fun shouldComputeTheProxyArgsWhenBothAnHttpAndHttpsProxyAreConfigured() {
        System.setProperty("http.proxyHost", "4.3.2.1")
        System.setProperty("http.proxyPort", "1234")
        System.setProperty("https.proxyHost", "4.3.2.1")
        System.setProperty("https.proxyPort", "1234")
        System.setProperty("https.proxyUser", "me/you")
        System.setProperty("https.proxyPassword", "p@ssword")

        val result = NpmProxy.computeNpmProxyCliArgs()

        assertThat(result).containsExactly("--proxy", "http://4.3.2.1:1234",
                "--https-proxy", "https://me%2Fyou:p%40ssword@4.3.2.1:1234")
    }

    @Test
    internal fun shouldComputeTheProxyArgsWhenThePortIsNotDefined() {
        System.setProperty("http.proxyHost", "4.3.2.1")
        System.setProperty("https.proxyHost", "4.3.2.1")

        val result = NpmProxy.computeNpmProxyCliArgs()

        assertThat(result).isEmpty()
    }

    @Test
    internal fun shouldComputeTheProxyArgsWhenThePassword() {
        System.setProperty("http.proxyHost", "4.3.2.1")
        System.setProperty("http.proxyPort", "80")
        System.setProperty("http.proxyUser", "me")
        System.setProperty("https.proxyHost", "4.3.2.1")
        System.setProperty("https.proxyPort", "443")
        System.setProperty("https.proxyHost", "me")

        val result = NpmProxy.computeNpmProxyCliArgs()

        assertThat(result).containsExactly("--proxy", "http://4.3.2.1:80", "--https-proxy", "https://me:443")
    }
}
