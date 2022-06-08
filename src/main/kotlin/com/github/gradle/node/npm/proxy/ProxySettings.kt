package com.github.gradle.node.npm.proxy

/**
 * @since 3.0
 */
enum class ProxySettings {
    /**
     * The default, this will set the proxy settings only if there's no configuration
     * present in the environment variables already.
     */
    SMART,

    /**
     * This will always set the proxy settings, overriding any settings already present.
     * This might cause incorrect settings.
     */
    FORCED,

    /**
     * Don't set any proxy settings.
     */
    OFF
}