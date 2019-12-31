package com.moowork.gradle.node.util

import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

/**
 * Tokenizes the given string into a list using the provided delimiter set.
 */
@Suppress("UNCHECKED_CAST")
fun String.tokenize(delimiters: String = " \t\n\r\u000C"): List<String> = StringTokenizer(this, delimiters).toList() as List<String>

/**
 * Executes the given command and returns its output.
 *
 * This is based on an aggregate of the answers provided here: [https://stackoverflow.com/questions/35421699/how-to-invoke-external-command-from-within-kotlin-code]
 */
fun execute(vararg args: String, timeout: Long = 60): String {
    return ProcessBuilder(args.toList())
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()
            .apply { waitFor(timeout, TimeUnit.SECONDS) }
            .inputStream.bufferedReader().readText().trim()
}

/**
 * Transforms the receiver with the given [transform] if the provided [predicate] evaluates to `true`. Otherwise, the receiver is returned.
 */
inline fun <T : Any?> T.mapIf(predicate: (T) -> Boolean, transform: (T) -> T): T = if (predicate(this)) transform(this) else this

/**
 * A property delegate which delegates property accessors and mutators to the given target property. This is very similar to providing custom getters and setters.
 *
 * Example usage:
 * ```
 * val person: Person
 * val personName by Alias { person::name }
 *
 * fun testChangePersonName(name: String) {
 *     this.person.name = name
 *     assert this.personName == name // true
 * }
 * ```
 *
 * *Note*: This delegator uses a property supplier function so that delegation can occur through non-final properties. For example, if the `person` field in the example above were
 * not read-only (i.e., `var`) then initializing the alias with `person::name` would reference the `name` property for the existing `Person` instance. A change to the `person`
 * field would not propagate to the property delegate, and subsequent accesses through the property delegate would continue to reference the original `person` value rather than the
 * new one. Without using the property supplier, we would initialize with a property on an existing instance and would not be able to change to another instance.
 */
class Alias<R, T>(private val propertySupplier: (R) -> KProperty0<T>) : ReadOnlyProperty<R, T> {
    override operator fun getValue(thisRef: R, property: KProperty<*>): T = propertySupplier(thisRef).get()
}

/**
 * The [Alias] type for mutable properties. This property delegate allows for write operations to the target property.
 *
 * @see Alias
 */
class MutableAlias<R, T>(private val propertySupplier: (R) -> KMutableProperty0<T>) : ReadWriteProperty<R, T> {
    override operator fun getValue(thisRef: R, property: KProperty<*>): T = propertySupplier(thisRef).get()
    override operator fun setValue(thisRef: R, property: KProperty<*>, value: T) {
        propertySupplier(thisRef).set(value)
    }
}
