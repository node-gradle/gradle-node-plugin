package com.github.gradle.node.npm.proxy

import com.github.gradle.node.exec.NodeExecConfiguration
import java.net.URLEncoder
import kotlin.text.Charsets.UTF_8

object NpmProxy {

    //    companion object {
    // These are the environment variables that HTTPing applications checks, proxy is on and off.
    // FTP skipped in hopes of a better future.
    private val proxyVariables = listOf(
        "HTTP_PROXY", "HTTPS_PROXY", "NO_PROXY", "PROXY"
    )

    // And since npm also takes settings in the form of environment variables with the
    // NPM_CONFIG_<setting> format, we should check those. Hopefully nobody does this.
    // Windows will let you set environment variables with hyphens in them, but shells
    // on Linux will fight you. So you'll have to be pretty sneaky to do this.
    // I'm adding both here "just in case".
    private val npmProxyVariables = listOf(
        "NPM_CONFIG_PROXY", "NPM_CONFIG_HTTPS-PROXY", "NPM_CONFIG_HTTPS_PROXY", "NPM_CONFIG_NOPROXY"
    )

    /**
     * Creates a map of environment variables with proxy settings.
     *
     * Will return an empty map if none are set.
     */
    fun computeNpmProxyEnvironmentVariables(): Map<String, String> {
        val proxyEnvironmentVariables = computeProxyUrlEnvironmentVariables()
        if (proxyEnvironmentVariables.isNotEmpty()) {
            addProxyIgnoredHostsEnvironmentVariable(proxyEnvironmentVariables)
        }
        return proxyEnvironmentVariables.toMap()
    }

    /**
     * Helper function for deciding whether proxy settings need to be set or not.
     */
    fun shouldConfigureProxy(env: Map<String, String>, settings: ProxySettings): Boolean {
        if (settings == ProxySettings.FORCED) {
            return true
        } else if (settings == ProxySettings.SMART) {
            return !hasProxyConfiguration(env)
        }

        return false
    }

    /**
     * Returns true if the given map of environment variables has any
     * proxy settings configured.
     *
     * @param env map of environment variables
     */
    fun hasProxyConfiguration(env: Map<String, String>): Boolean {
        return env.keys.any {
            proxyVariables.contains(it.toUpperCase()) || npmProxyVariables.contains(it.toUpperCase())
        }
    }

    /**
     * Get a list of all known keys that affect the proxy configuration
     */
    fun getKnownProxyConfigurationKeys(): Set<String> {
        return proxyVariables.plus(npmProxyVariables).toSet()
    }

    /**
     * Creates a new NodeExecConfiguration with the proxy environment variables configured
     */
    fun addProxyEnvironmentVariables(
        proxySettings: ProxySettings,
        nodeExecConfiguration: NodeExecConfiguration,
        environment: Map<String, String> = System.getenv()
    ): NodeExecConfiguration {
        val environmentVariables = createProxyEnvironmentVariables(
            proxySettings,
            nodeExecConfiguration.environment,
            environment,
        )
        return nodeExecConfiguration.copy(environment = environmentVariables)
    }

    fun createProxyEnvironmentVariables(
        proxySettings: ProxySettings,
        nodeExecConfigurationEnvironment: Map<String, String>,
        environment: Map<String, String> = System.getenv()
    ): Map<String, String> {
        if (shouldConfigureProxy(environment, proxySettings)) {
            val npmProxyEnvironmentVariables = computeNpmProxyEnvironmentVariables()
            val environmentVariablesToUnset =
                if (proxySettings == ProxySettings.FORCED) getKnownProxyConfigurationKeys()
                else emptySet()
            if (npmProxyEnvironmentVariables.isNotEmpty()) {
                val environmentVariables =
                    nodeExecConfigurationEnvironment
                        .minus(environmentVariablesToUnset)
                        .plus(npmProxyEnvironmentVariables)
                return environmentVariables
            }
        }
        return emptyMap()
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

    private fun addProxyIgnoredHostsEnvironmentVariable(
        proxyEnvironmentVariables: MutableMap<String, String>,
    ) {
        val proxyIgnoredHosts = computeProxyIgnoredHosts()
        if (proxyIgnoredHosts.isNotEmpty()) {
            proxyEnvironmentVariables["NO_PROXY"] = proxyIgnoredHosts.joinToString(", ")
        }
    }

    private fun computeProxyIgnoredHosts(): List<String> {
        return listOf("http.nonProxyHosts", "https.nonProxyHosts")
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
                return@map listOf()
            }
            .flatten()
            .distinct()
//            .collect(toList())
    }

    //    }
    @Deprecated(
        "Replace with regular object",
        ReplaceWith("NpmProxy", imports = ["com.github.gradle.node.npm.proxy.NpmProxy"])
    )
    object Companion

}
