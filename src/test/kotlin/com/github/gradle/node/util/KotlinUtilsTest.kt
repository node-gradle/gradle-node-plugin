package com.github.gradle.node.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KotlinUtilsTest {
    @Test
    fun shouldAliasWorkDelegateRead() {
        val user = User(5, "Alice")
        val immutableUserWrapper = UserWrapper(user)

        val name = immutableUserWrapper.name

        assertThat(name).isEqualTo(user.name)
    }

    class UserWrapper(private val user: User) {
        val name by Alias { user::name }
    }

    data class User(var id: Int, var name: String)
}
