package com.github.gradle.node.npm.proxy

import java.net.URLEncoder
import java.util.stream.Collectors.toList
import java.util.stream.Stream
import kotlin.text.Charsets.UTF_8

internal class NpmProxy {
    companion object {
        fun computeNpmProxyCliArgs(): List<String> {
            val proxyArgs = computeProxyUrlArgs()
            if (proxyArgs.isNotEmpty()) {
                computeProxyIgnoredHostsArgs(proxyArgs)
            }
            return proxyArgs.toList()
        }

        private fun computeProxyUrlArgs(): MutableList<String> {
            val proxyArgs = mutableListOf<String>()
            for ((proxyProto, proxyParam) in
            listOf(arrayOf("http", "--proxy"), arrayOf("https", "--https-proxy"))) {
                var proxyHost = System.getProperty("$proxyProto.proxyHost")
                val proxyPort = System.getProperty("$proxyProto.proxyPort")
                if (proxyHost != null && proxyPort != null) {
                    proxyHost = proxyHost.replace("^https?://".toRegex(), "")
                    val proxyUser = System.getProperty("$proxyProto.proxyUser")
                    val proxyPassword = System.getProperty("$proxyProto.proxyPassword")
                    proxyArgs.add(proxyParam)
                    if (proxyUser != null && proxyPassword != null) {
                        proxyArgs.add("$proxyProto://${encode(proxyUser)}:${encode(proxyPassword)}@$proxyHost:$proxyPort")
                    } else {
                        proxyArgs.add("$proxyProto://$proxyHost:$proxyPort")
                    }
                }
            }
            return proxyArgs
        }

        private fun encode(value: String): String {
            return URLEncoder.encode(value, UTF_8.toString())
        }

        private fun computeProxyIgnoredHosts(): List<String> {
            return Stream.of(Pair("http.nonProxyHosts", 80), Pair("https.nonProxyHosts", 443))
                    .map { (property, port) ->
                        val propertyValue = System.getProperty(property)
                        if (propertyValue != null) {
                            val hosts = propertyValue.split("|")
                            return@map hosts.map { host ->
                                if (host.contains(":")) host
                                else "$host:$port"
                            }
                        }
                        return@map listOf<String>()
                    }
                    .flatMap(List<String>::stream)
                    .distinct()
                    .collect(toList())
        }

        private fun computeProxyIgnoredHostsArgs(proxyArgs: MutableList<String>) {
            val proxyIgnoredHosts = computeProxyIgnoredHosts()
            if (proxyIgnoredHosts.isNotEmpty()) {
                proxyArgs.add("-c")
                proxyArgs.add("noproxy=" + proxyIgnoredHosts.joinToString(","))
            }
        }
    }
}
