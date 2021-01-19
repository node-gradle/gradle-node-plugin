package com.github.gradle.node.npm.proxy

import java.net.URLEncoder
import java.util.stream.Collectors.toList
import java.util.stream.Stream
import kotlin.text.Charsets.UTF_8

internal class NpmProxy {

    companion object {
        // These are the environment variables that HTTPing applications checks, proxy is on and off.
        // FTP skipped in hopes of a better future.
        private val proxyVariables = listOf(
                "HTTP_PROXY", "HTTPS_PROXY", "NO_PROXY", "PROXY"
        )

        // And since npm also takes settings in the form of environment variables with the
        // NPM_CONFIG_<setting> format, we should check those. Hopefully nobody does this.
        private val npmProxyVariables = listOf(
                "NPM_CONFIG_PROXY", "NPM_CONFIG_HTTPS-PROXY", "NPM_CONFIG_NOPROXY"
        )

        fun computeNpmProxyEnvironmentVariables(): Map<String, String> {
            val proxyEnvironmentVariables = computeProxyUrlEnvironmentVariables()
            if (proxyEnvironmentVariables.isNotEmpty()) {
                addProxyIgnoredHostsEnvironmentVariable(proxyEnvironmentVariables)
            }
            return proxyEnvironmentVariables.toMap()
        }

        fun shouldConfigureProxy(env: Map<String, String>, settings: ProxySettings): Boolean {
            if (settings == ProxySettings.FORCED) {
                return true
            } else if (settings == ProxySettings.SMART) {
                return !hasProxyConfiguration(env)
            }

            return false
        }

        /**
         * Returns true if the given map of environment variables already has
         * proxy settings configured.
         *
         * @param env map of environment variables
         */
        fun hasProxyConfiguration(env: Map<String, String>): Boolean {
            return env.keys.any {
                proxyVariables.contains(it.toUpperCase()) || npmProxyVariables.contains(it.toUpperCase())
            }
        }

        private fun computeProxyUrlEnvironmentVariables(): MutableMap<String, String> {
            val proxyArgs = mutableMapOf<String, String>()
            for ((proxyProto, proxyParam) in
            listOf(arrayOf("http", "HTTP_PROXY"), arrayOf("https", "HTTPS_PROXY"))) {
                var proxyHost = System.getProperty("$proxyProto.proxyHost")
                val proxyPort = System.getProperty("$proxyProto.proxyPort")
                if (proxyHost != null && proxyPort != null) {
                    proxyHost = proxyHost.replace("^https?://".toRegex(), "")
                    val proxyUser = System.getProperty("$proxyProto.proxyUser")
                    val proxyPassword = System.getProperty("$proxyProto.proxyPassword")
                    if (proxyUser != null && proxyPassword != null) {
                        proxyArgs[proxyParam] =
                                "http://${encode(proxyUser)}:${encode(proxyPassword)}@$proxyHost:$proxyPort"
                    } else {
                        proxyArgs[proxyParam] = "http://$proxyHost:$proxyPort"
                    }
                }
            }
            return proxyArgs
        }

        private fun encode(value: String): String {
            return URLEncoder.encode(value, UTF_8.toString())
        }

        private fun addProxyIgnoredHostsEnvironmentVariable(proxyEnvironmentVariables: MutableMap<String, String>) {
            val proxyIgnoredHosts = computeProxyIgnoredHosts()
            if (proxyIgnoredHosts.isNotEmpty()) {
                proxyEnvironmentVariables["NO_PROXY"] = proxyIgnoredHosts.joinToString(", ")
            }
        }

        private fun computeProxyIgnoredHosts(): List<String> {
            return Stream.of("http.nonProxyHosts", "https.nonProxyHosts")
                    .map { property ->
                        val propertyValue = System.getProperty(property)
                        if (propertyValue != null) {
                            val hosts = propertyValue.split("|")
                            return@map hosts
                                    .map { host ->
                                        if (host.contains(":")) host.split(":")[0]
                                        else host
                                    }
                        }
                        return@map listOf<String>()
                    }
                    .flatMap(List<String>::stream)
                    .distinct()
                    .collect(toList())
        }
    }
}
