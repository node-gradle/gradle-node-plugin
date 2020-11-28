package com.github.gradle.node.npm.proxy

class GradleProxyHelper {
    companion object {
        @JvmStatic
        fun setHttpProxyHost(host: String) {
            System.setProperty("http.proxyHost", host)
        }

        @JvmStatic
        fun setHttpProxyPort(port: Int) {
            System.setProperty("http.proxyPort", port.toString())
        }

        @JvmStatic
        fun setHttpProxyUser(user: String) {
            System.setProperty("http.proxyUser", user)
        }

        @JvmStatic
        fun setHttpProxyPassword(password: String) {
            System.setProperty("http.proxyPassword", password)
        }

        @JvmStatic
        fun setHttpProxyIgnoredHosts(vararg hosts: String) {
            System.setProperty("http.nonProxyHosts", hosts.joinToString("|"))
        }

        @JvmStatic
        fun setHttpsProxyHost(host: String) {
            System.setProperty("https.proxyHost", host)
        }

        @JvmStatic
        fun setHttpsProxyPort(port: Int) {
            System.setProperty("https.proxyPort", port.toString())
        }

        @JvmStatic
        fun setHttpsProxyUser(user: String) {
            System.setProperty("https.proxyUser", user)
        }

        @JvmStatic
        fun setHttpsProxyPassword(password: String) {
            System.setProperty("https.proxyPassword", password)
        }

        @JvmStatic
        fun setHttpsProxyIgnoredHosts(vararg hosts: String) {
            val value = hosts.joinToString("|")
            System.setProperty("https.nonProxyHosts", value)
        }

        @JvmStatic
        fun resetProxy() {
            System.clearProperty("http.proxyHost")
            System.clearProperty("http.proxyPort")
            System.clearProperty("http.proxyUser")
            System.clearProperty("http.proxyPassword")
            System.clearProperty("http.nonProxyHosts")
            System.clearProperty("https.proxyHost")
            System.clearProperty("https.proxyPort")
            System.clearProperty("https.proxyUser")
            System.clearProperty("https.proxyPassword")
            System.clearProperty("https.nonProxyHosts")
        }
    }
}
