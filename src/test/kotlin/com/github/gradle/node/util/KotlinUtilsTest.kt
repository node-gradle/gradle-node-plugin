package com.github.gradle.node.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class KotlinUtilsTest {
    @Test
    fun shouldAliasWorkDelegateRead() {
        val user = User(5, "Alice")
        val immutableUserWrapper = ImmutableUserWrapper(user)

        val name = immutableUserWrapper.name

        assertThat(name).isEqualTo(user.name)
    }

    @Test
    fun shouldMutableAliasDelegateReadAndWrite() {
        val userWrapper = MutableUserWrapper()

        userWrapper.id = 2
        userWrapper.name = "Bob"

        val expectedUser = User(2, "Bob")
        assertThat(userWrapper.user).isEqualTo(expectedUser)
    }

    @Test
    fun shouldOverrideMapAliasDelegateReadAndWrite() {
        val configuration = Configuration(mutableMapOf("debug" to "false"))
        val configurationWrapper = ConfigurationWrapper(configuration)

        configurationWrapper.properties = mapOf("verbose" to "true", "mode" to "basic")

        assertThat(configurationWrapper.properties)
                .isEqualTo(mapOf("debug" to "false", "verbose" to "true", "mode" to "basic"))

        configurationWrapper.properties = mapOf("in-memory" to "false", "mode" to "advanced")

        val expectedConfiguration = Configuration(mutableMapOf("debug" to "false", "verbose" to "true",
                "mode" to "advanced", "in-memory" to "false"))
        assertThat(configuration).isEqualTo(expectedConfiguration)
    }

    class MutableUserWrapper {
        val user: User = User(0, "")
        var id by MutableAlias { user::id }
        var name by MutableAlias { user::name }
    }

    class ImmutableUserWrapper(private val user: User) {
        val name by Alias { user::name }
    }

    data class User(var id: Int, var name: String)

    data class ConfigurationWrapper(private var configuration: Configuration) {
        var properties by OverrideMapAlias { configuration::properties }
    }

    data class Configuration(var properties: MutableMap<String, String>)
}
